/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.lang.ruby;

import java.io.*;
import java.util.*;

import org.jruby.*;
import org.jruby.ast.Node;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.KCode;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RubyJxpSource extends JxpSource {

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final Map<Ruby, Set<IRubyObject>> _requiredJSFileLibFiles = new WeakHashMap<Ruby, Set<IRubyObject>>();

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");
    static final boolean SKIP_REQUIRED_LIBS = Boolean.getBoolean("DEBUG.RB.SKIP.REQ.LIBS");
    /** Scope top-level functions to avoid loading. */
    static final Collection<String> DO_NOT_LOAD_FUNCS;
    static final RubyInstanceConfig config = new RubyInstanceConfig();

    static {
	if (!SKIP_REQUIRED_LIBS)
	    config.requiredLibraries().add("xgen_internals");
	DO_NOT_LOAD_FUNCS = new ArrayList<String>();
	DO_NOT_LOAD_FUNCS.add("print");
	DO_NOT_LOAD_FUNCS.add("sleep");
	DO_NOT_LOAD_FUNCS.add("fork");
	DO_NOT_LOAD_FUNCS.add("eval");
    }

    /** Determines what major version of Ruby to compile: 1.8 (false) or YARV/1.9 (true). **/
    public static final boolean YARV_COMPILE = false;

    public RubyJxpSource(File f , JSFileLibrary lib) {
        _file = f;
        _lib = lib;
	_runtime = Ruby.newInstance(config);
    }

    /** For testing. */
    protected RubyJxpSource(Ruby runtime) {
	_file = null;
	_lib = null;
	_runtime = runtime;
    }

    public Ruby getRuntime() { return _runtime; }

    protected String getContent() throws IOException {
	return StreamUtil.readFully(_file);
    }

    protected InputStream getInputStream() throws FileNotFoundException {
	return new FileInputStream(_file);
    }

    public long lastUpdated(Set<Dependency> visitedDeps) {
        return _file.lastModified();
    }

    public String getName() {
        return _file.toString();
    }

    public File getFile() {
        return _file;
    }

    public JSFunction getFunction() throws IOException {
        final Node code = _getCode(); // Parsed Ruby code
        return new ed.js.func.JSFunctionCalls0() {
            public Object call(Scope s, Object unused[]) { return _doCall(code, s, unused); }
        };
    }

    protected Object _doCall(Node code, Scope s, Object unused[]) {
	_addSiteRootToPath(s);

	if (_runtime.getGlobalVariables() instanceof ScopeGlobalVariables)
	    _runtime.setGlobalVariables(((ScopeGlobalVariables)_runtime.getGlobalVariables()).getOldGlobalVariables());
	_setOutput(s);
	_runtime.setGlobalVariables(new ScopeGlobalVariables(s, _runtime));
	_exposeScopeFunctions(s);
	_patchRequireAndLoad(s);

	// See the second part of JRuby's Ruby.executeScript(String, String)
	ThreadContext context = _runtime.getCurrentContext();

	String oldFile = context.getFile();
	int oldLine = context.getLine();
	try {
	    context.setFileAndLine(code.getPosition().getFile(), code.getPosition().getStartLine());
	    return _runtime.runNormally(code, YARV_COMPILE);
	} finally {
	    context.setFile(oldFile);
	    context.setLine(oldLine);
	}
    }

    protected synchronized Node _getCode() throws IOException {
	final long lastModified = _file.lastModified();
        if (_code == null || _lastCompile < lastModified) {
	    _code = _parseContent(_file.getPath());
	    _lastCompile = lastModified;
	}
        return _code;
    }

    protected Node _parseContent(String filePath) throws IOException {
	// See the first part of JRuby's Ruby.executeScript(String, String)
	String script = getContent();
	byte[] bytes;
	try {
	    bytes = script.getBytes(KCode.NONE.getKCode());
	} catch (UnsupportedEncodingException e) {
	    bytes = script.getBytes();
	}
	return _runtime.parseInline(new ByteArrayInputStream(bytes), filePath, null);
    }

    protected void _addSiteRootToPath(Scope s) {
	Object appContext = s.get("__instance__");
	if (appContext != null) {
	    RubyString siteRoot = RubyString.newString(_runtime, appContext.toString().replace('\\', '/'));
	    RubyArray loadPath = (RubyArray)_runtime.getLoadService().getLoadPath();
	    if (loadPath.include_p(_runtime.getCurrentContext(), siteRoot).isFalse()) {
		if (DEBUG)
		    System.err.println("adding site root " + siteRoot + " to Ruby load path");
		loadPath.append(siteRoot);
	    }
	}
    }

    /**
     * Set Ruby's $stdout so that print/puts statements output to the right
     * place. If we have no HttpResponse (for example, we're being run outside
     * the app server), then nothing happens.
     */
    protected void _setOutput(Scope s) {
	HttpResponse response = (HttpResponse)s.get("response");
	if (response != null)
	    _runtime.getGlobalVariables().set("$stdout", new RubyIO(_runtime, new RubyJxpOutputStream(response.getJxpWriter())));
    }

    /**
     * Creates the $scope global object and a method_missing method for the
     * top-level object.
     */
    protected void _exposeScopeFunctions(Scope scope) {
	_runtime.getGlobalVariables().set("$scope", toRuby(scope, _runtime, scope));
	_addTopLevelMethodsToObjectClass(scope);
    }

    // TODO add to object class, or add to kernel?
    protected void _addTopLevelMethodsToObjectClass(final Scope scope) {
	RubyClass klazz = _runtime.getObject();
	Set<String> alreadySeen = new HashSet<String>();
	Scope s = scope;
	while (s != null) {
	    for (String key : s.keySet()) {
		if (alreadySeen.contains(key) || DO_NOT_LOAD_FUNCS.contains(key))
		    continue;
		final Object obj = s.get(key);
		if (isCallableJSFunction(obj)) {
		    if (DEBUG)
			System.err.println("adding top-level method " + key);
		    alreadySeen.add(key);
		    // Creates method and attaches to klazz. Also creates a new Ruby class if appropriate.
		    new RubyJSFunctionWrapper(scope, _runtime, (JSFunction)obj, key, klazz);
		}
	    }
	    s = s.getParent();
	}
    }

    protected void _patchRequireAndLoad(final Scope scope) {
	RubyModule kernel = _runtime.getKernel();
	kernel.addMethod("require", new JavaMethod(kernel, PUBLIC) {
		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    Ruby runtime = self.getRuntime();
		    String arg = args[0].toString();
		    try {
			return runtime.getLoadService().require(arg) ? runtime.getTrue() : runtime.getFalse();
		    }
		    catch (RaiseException re) {
			if (_notAlreadyRequired(runtime, args[0])) {
			    loadLibraryFile(scope, runtime, self, arg, re);
			    _rememberAlreadyRequired(runtime, args[0]);
			}
			return runtime.getTrue();
		    }
		}
	    });
	kernel.addMethod("load", new JavaMethod(kernel, PUBLIC) {
		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    Ruby runtime = self.getRuntime();
		    RubyString file = args[0].convertToString();
		    boolean wrap = args.length == 2 ? args[1].isTrue() : false;

		    try {
			runtime.getLoadService().load(file.getByteList().toString(), wrap);
			return runtime.getTrue();
		    }
		    catch (RaiseException re) {
			return loadLibraryFile(scope, runtime, self, file.toString(), re);
		    }
		}
	    });
    }

    protected IRubyObject loadLibraryFile(Scope scope, Ruby runtime, IRubyObject recv, String file, RaiseException re) {
	if (DEBUG)
	    System.err.println("going to compile and run library file " + file);
	try {
	    JSFileLibrary local = (JSFileLibrary)scope.get("local");
	    Object o = local.getFromPath(file);
	    if (isCallableJSFunction(o)) {
		try {
		    ((JSFunction)o).call(scope, EMPTY_OBJECT_ARRAY);
		}
		catch (Exception e) {
		    recv.callMethod(runtime.getCurrentContext(), "raise", new IRubyObject[] {RubyString.newString(runtime, e.toString())}, Block.NULL_BLOCK);
		}
		return runtime.getTrue();
	    }
	    else if (DEBUG)
		System.err.println("file library object is not a callable function");
	}
	catch (Exception e) {
	    if (DEBUG)
		System.err.println("problem loading file " + file + "; exception seen is " + e + "; falling through to throw original Ruby error");
	    /* fall through to throw re */
	}
	if (DEBUG)
	    System.err.println("problem loading file " + file + "; throwing original Ruby error " + re);
	throw re;
    }

    protected boolean _notAlreadyRequired(Ruby runtime, IRubyObject arg) {
	synchronized (_requiredJSFileLibFiles) {
	    Set<IRubyObject> reqs = _requiredJSFileLibFiles.get(runtime);
	    return reqs == null || !reqs.contains(arg);
	}
    }

    protected void _rememberAlreadyRequired(Ruby runtime, IRubyObject arg) {
	synchronized (_requiredJSFileLibFiles) {
	    Set<IRubyObject> reqs = _requiredJSFileLibFiles.get(runtime);
	    if (reqs == null) {
		reqs = new HashSet<IRubyObject>();
		_requiredJSFileLibFiles.put(runtime, reqs);
	    }
	    reqs.add(arg);
	}
    }

    protected final File _file;
    protected final JSFileLibrary _lib;
    protected final Ruby _runtime;

    protected Node _code;
    protected long _lastCompile;
}
