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

public class RubyJxpSource extends JxpSource {

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
	_runtime = org.jruby.Ruby.newInstance(config);
    }

    /** For testing. */
    protected RubyJxpSource(org.jruby.Ruby runtime) {
	_file = null;
	_lib = null;
	_runtime = runtime;
    }

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

    protected void _addTopLevelMethodsToObjectClass(final Scope scope) {
	RubyClass objectKlass = _runtime.getObject();
	Set<String> alreadySeen = new HashSet<String>();
	Scope s = scope;
	while (s != null) {
	    for (String key : s.keySet()) {
		if (alreadySeen.contains(key) || DO_NOT_LOAD_FUNCS.contains(key))
		    continue;
		final Object obj = s.get(key);
		if (!(obj instanceof JSFunction))
		    continue;
		alreadySeen.add(key);
		objectKlass.addMethod(key, new JavaMethod(objectKlass, PUBLIC) {
			public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
			    Object[] jargs = RubyObjectWrapper.toJSFunctionArgs(scope, _runtime, args, 0, block);
			    return toRuby(scope, _runtime, ((JSFunction)obj).call(scope, jargs));
			}
		    });
	    }
	    s = s.getParent();
	}
    }

    protected final File _file;
    protected final JSFileLibrary _lib;
    protected final org.jruby.Ruby _runtime;

    protected Node _code;
    protected long _lastCompile;
}
