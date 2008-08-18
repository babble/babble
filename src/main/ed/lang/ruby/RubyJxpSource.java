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

import org.jruby.Ruby;
import org.jruby.RubyIO;
import org.jruby.ast.Node;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.util.KCode;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.io.StreamUtil;
import ed.js.JSObject;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;

public class RubyJxpSource extends JxpSource {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");

    /** Determines what major version of Ruby to compile: 1.8 (false) or YARV/1.9 (true). **/
    public static final boolean YARV_COMPILE = false;

    public RubyJxpSource(File f , JSFileLibrary lib) {
        _file = f;
        _lib = lib;
	_runtime = Ruby.newInstance();
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
		_setOutput(s);
		_setGlobals(s);

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

    private Node _getCode() throws IOException {
	final long lastModified = _file.lastModified();
        if (_code == null || _lastCompile < lastModified) {
	    // See the first part of JRuby's Ruby.executeScript(String, String)
	    String script = getContent();
	    byte[] bytes;
	    try {
		bytes = script.getBytes(KCode.NONE.getKCode());
	    } catch (UnsupportedEncodingException e) {
		bytes = script.getBytes();
	    }

	    _code = _runtime.parseInline(new ByteArrayInputStream(bytes), _file.getPath(), null);
            _lastCompile = lastModified;
        }
        return _code;
    }

    /** Set Ruby's $stdout so that print/puts statements output to the right place. */
    private void _setOutput(Scope s) {
	HttpResponse response = (HttpResponse)s.get("response");
	_runtime.getGlobalVariables().set("$stdout", new RubyIO(_runtime, new RubyJxpOutputStream(response.getWriter())));
    }

    private void _setGlobals(Scope s) {
	GlobalVariables g = _runtime.getGlobalVariables();

	// Turn all JSObject scope variables into Ruby global variables
	Set<String> alreadySeen = new HashSet<String>();
	while (s != null) {
	    for (String key : s.keySet()) {
		if (alreadySeen.contains(key)) // Use most "local" version of var
		    continue;
		Object val = s.get(key);
		if (DEBUG)
		    System.err.println("about to set $" + key + "; class = " + (val == null ? "<null>" : val.getClass().getName()));
		String name = "$" + key;
		g.set(name, RubyObjectWrapper.toRuby(s, _runtime, val, name));
		alreadySeen.add(key);
	    }
	    s = s.getParent();
	}
    }

    private final File _file;
    private final JSFileLibrary _lib;
    private final Ruby _runtime;

    private Node _code;
    private long _lastCompile;
}
