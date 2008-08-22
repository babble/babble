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
import java.util.Set;
import java.util.HashSet;

import org.jruby.*;
import org.jruby.ast.Node;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.KCode;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;

public class RubyJxpSource extends JxpSource {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");
    static final boolean SKIP_XGEN_REQUIRED_LIBS = Boolean.getBoolean("DEBUG.RB.SKIPLIBS");
    static final RubyInstanceConfig config = new RubyInstanceConfig();

    static {
	if (!SKIP_XGEN_REQUIRED_LIBS)
	    config.requiredLibraries().add("xgen_internals");
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

    public synchronized JSFunction getFunction() throws IOException {
        final Node code = _getCode(); // Parsed Ruby code
        return new ed.js.func.JSFunctionCalls0() {
            public Object call(Scope s , Object unused[]) {
		_addSiteRootToPath(s);
		_setOutput(s);
		_exposeScope(s);

		// See the second part of JRuby's Ruby.executeScript(String, String)
		ThreadContext context = _runtime.getCurrentContext();
        
		String oldFile = context.getFile();
		int oldLine = context.getLine();
		try {
		    context.setFile(code.getPosition().getFile());
		    context.setLine(code.getPosition().getStartLine());
		    return _runtime.runNormally(code, YARV_COMPILE);
		} finally {
		    context.setFile(oldFile);
		    context.setLine(oldLine);
		}
            }
        };
    }

    protected Node _getCode() throws IOException {
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
	RubyString siteRoot = _runtime.newString(s.get("__instance__").toString().replace('\\', '/'));
	RubyArray loadPath = (RubyArray)_runtime.getLoadService().getLoadPath();
	if (loadPath.include_p(_runtime.getCurrentContext(), siteRoot).isFalse()) {
	    if (DEBUG)
		System.err.println("adding site root " + siteRoot + " to Ruby load path");
	    loadPath.append(siteRoot);
	}
    }

    /** Set Ruby's $stdout so that print/puts statements output to the right place. */
    protected void _setOutput(Scope s) {
	HttpResponse response = (HttpResponse)s.get("response");
	_runtime.getGlobalVariables().set("$stdout", new RubyIO(_runtime, new RubyJxpOutputStream(response.getWriter())));
    }

    /**
     * Creates the $scope global object and turns almost all scope variables
     * into Ruby top-level variables. The exception: the "print" function,
     * which is already handled by the Ruby Kernel class (aided by our
     * definition of $stdout).
     */
    protected void _exposeScope(Scope s) {
	_runtime.getGlobalVariables().set("$scope", JavaUtil.convertJavaToUsableRubyObject(_runtime, s));

	// Turn all JSObject scope variables into Ruby top-level variables
	Set<String> alreadySeen = new HashSet<String>();
	RubyObject top = (RubyObject)_runtime.getTopSelf();
	RubyClass eigenclass = top.getSingletonClass();
	while (s != null) {
	    for (String key : s.keySet()) {
		if (alreadySeen.contains(key)) // Use most "local" version of var
		    continue;
		Object val = s.get(key);
		if ("print".equals(key) && val instanceof JSFunction)
		    continue;
		if (DEBUG)
		    System.err.println("about to expose " + key + "; class = " + (val == null ? "<null>" : val.getClass().getName()));
		IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(_runtime, val);
		top.instance_variable_set(RubySymbol.newSymbol(_runtime, "@" + key), ro);
		if (!(val instanceof JSFunction))
		    eigenclass.attr_reader(_runtime.getCurrentContext(), new IRubyObject[] {RubySymbol.newSymbol(_runtime, key)});
		alreadySeen.add(key);
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
