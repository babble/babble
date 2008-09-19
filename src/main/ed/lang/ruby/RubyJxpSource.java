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
import org.jruby.util.IdUtil;
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
    static final Map<Ruby, RubyModule> xgenModuleDefs = new WeakHashMap<Ruby, RubyModule>();

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");
    static final boolean SKIP_REQUIRED_LIBS = Boolean.getBoolean("DEBUG.RB.SKIP.REQ.LIBS");
    /** Scope top-level functions to avoid loading. */
    static final Collection<String> DO_NOT_LOAD_FUNCS;
    // TODO for now, we just add the local site dir to the load path
//     static final String[] BUILTIN_JS_FILE_LIBRARIES = {"local", "core", "external"};
    static final String[] BUILTIN_JS_FILE_LIBRARIES = {"local"};
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

    public static synchronized RubyModule xgenModule(Ruby runtime) {
	RubyModule xgen = xgenModuleDefs.get(runtime);
	if (xgen == null) {
	    xgen = runtime.defineModule("XGen");
	    xgenModuleDefs.put(runtime, xgen);
	}
	return xgen;
    }

    /**
     * Creates Ruby classes from any new JavaScript classes found in the top
     * level of <var>scope</var>. Called immediately after loading a file
     * using a JSFileLibrary.
     */
    public static void createNewClasses(Scope scope, Ruby runtime) {
	if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
	    System.err.println("about to create newly-defined classes");
	for (Object key : RubyScopeWrapper.jsKeySet(scope)) {
	    String skey = key.toString();
	    if (IdUtil.isConstant(skey) && runtime.getClass(skey) == null) {
		Object o = scope.get(key);
		if (isCallableJSFunction(o)) {
		    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL || RubyObjectWrapper.DEBUG_CREATE)
			System.err.println("creating newly-defined class " + skey);
		    toRuby(scope, runtime, (JSFunction)o, skey);
		}
	    }
	}
    }

    public RubyJxpSource(File f , JSFileLibrary lib) {
        _file = f;
        _lib = lib;
	_runtime = Ruby.newInstance(config);
    }

    /** For testing and {@link RubyLanguage} use. */
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
	_addJSFileLibrariesToPath(s);

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

    protected void _addJSFileLibrariesToPath(Scope s) {
	RubyArray loadPath = (RubyArray)_runtime.getLoadService().getLoadPath();
	for (String libName : BUILTIN_JS_FILE_LIBRARIES) {
	    Object val = s.get(libName);
	    if (!(val instanceof JSFileLibrary))
		continue;
	    File root = ((JSFileLibrary)val).getRoot();
	    RubyString rubyRoot = RubyString.newString(_runtime, root.getPath().replace('\\', '/'));
	    if (loadPath.include_p(_runtime.getCurrentContext(), rubyRoot).isFalse()) {
		if (DEBUG)
		    System.err.println("adding file library " + val.toString() + " root " + rubyRoot);
		loadPath.append(rubyRoot);
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

    /**
     * Creates a module named XGen, includes it in the Object class (just like
     * Kernel), and adds all top-level JavaScript methods to the module.
     */
    protected void _addTopLevelMethodsToObjectClass(final Scope scope) {
	RubyModule xgen = xgenModule(_runtime);
	_runtime.getObject().includeModule(xgen);

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
		    // Creates method and attaches to xgen. Also creates a new Ruby class if appropriate.
		    RubyObjectWrapper.createRubyMethod(scope, _runtime, (JSFunction)obj, key, xgen, null);
		}
	    }
	    s = s.getParent();
	}
    }

    protected void _patchRequireAndLoad(final Scope scope) {
	RubyModule kernel = _runtime.getKernel();
	kernel.addMethod("require", new JavaMethod(kernel, PUBLIC) {
		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
		    Ruby runtime = self.getRuntime();
		    String file = args[0].toString();

		    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
			System.err.println("require " + file);
		    try {
			return runtime.getLoadService().require(file) ? runtime.getTrue() : runtime.getFalse();
		    }
		    catch (RaiseException re) {
			if (_notAlreadyRequired(runtime, args[0])) {
			    loadLibraryFile(scope, runtime, self, file, re);
			    _rememberAlreadyRequired(runtime, args[0]);
			}
			return runtime.getTrue();
		    }
		}
	    });
	kernel.addMethod("load", new JavaMethod(kernel, PUBLIC) {
		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
		    Ruby runtime = self.getRuntime();
		    RubyString file = args[0].convertToString();
		    boolean wrap = args.length == 2 ? args[1].isTrue() : false;

		    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
			System.err.println("load " + file);
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

    protected IRubyObject loadLibraryFile(Scope scope, Ruby runtime, IRubyObject recv, String path, RaiseException re) {
	if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
	    System.err.println("going to compile and run library file " + path + "; runtime = " + runtime);

	JSFileLibrary lib = getLibFromPath(path, scope);
	if (lib == null) {
	    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
		System.err.println("can not find JSFileLibrary for " + path + "; re-raising Ruby exception");
	    throw re;
	}
	path = removeLibName(path);

	try {
	    Object o = lib.getFromPath(path);
	    if (isCallableJSFunction(o)) {
		try {
		    ((JSFunction)o).call(scope, EMPTY_OBJECT_ARRAY);
		    createNewClasses(scope, runtime);
		}
		catch (Exception e) {
		    if (DEBUG || RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
			System.err.println("problem loading JSFileLibrary file: " + e + "; going to raise Ruby error after printing the stack trace here");
			e.printStackTrace();
		    }
		    recv.callMethod(runtime.getCurrentContext(), "raise", new IRubyObject[] {RubyString.newString(runtime, e.toString())}, Block.NULL_BLOCK);
		}
		return runtime.getTrue();
	    }
	    else
		System.err.println("file library object " + o + " is not a callable function");
	}
	catch (Exception e) {
	    if (DEBUG || RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
		System.err.println("problem loading JSFileLibrary file: " + e + "; going to re-throw original Ruby RaiseException after printing the stack trace here");
		e.printStackTrace();
	    }
	    /* fall through to throw re */
	}
	if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
	    System.err.println("problem loading file " + path + " from lib " + lib + "; throwing original Ruby error " + re);
	throw re;
    }

    /**
     * Returns a JSFileLibrary named at the start of <var>path</var>, which is
     * something like "local/foo" or "/core/core/routes". The first word
     * ("local" or "core") must be the name of a JSFileLibrary that is in the
     * scope. Returns <code>null</code> if no library is found.
     */
    public JSFileLibrary getLibFromPath(String path, Scope scope) {
	String libName = libNameFromPath(path);
	return (JSFileLibrary)scope.get(libName);
    }

    public String libNameFromPath(String path) {
	if (path.startsWith("/"))
	    path = path.substring(1);
	int loc = path.indexOf("/");
	return (loc == -1) ? path : path.substring(0, loc);
    }

    /**
     * Returns a new copy of <var>path</var> with the first part of the path
     * stripped off.
     */
    public String removeLibName(String path) {
	if (path.startsWith("/"))
	    path = path.substring(1);
	int loc = path.indexOf("/");
	return (loc == -1) ? "" : path.substring(loc + 1);
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
