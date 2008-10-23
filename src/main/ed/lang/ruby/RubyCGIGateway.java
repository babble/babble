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

import java.lang.ref.WeakReference;
import java.io.*;
import java.util.*;

import org.jruby.*;
import org.jruby.ast.Node;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.IdUtil;
import org.jruby.util.KCode;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.AppContext;
import ed.appserver.AppRequest;
import ed.appserver.JSFileLibrary;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.lang.cgi.CGIGateway;
import ed.lang.cgi.EnvMap;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RubyCGIGateway extends CGIGateway {

    protected RuntimeEnvironment runenv;
    protected File _file;
    protected Node _node;
    protected long _lastCompile;

    public RubyCGIGateway(File f) {
        this(f, null);
    }

    /** For testing and {@link RubyLanguage} use. */
    protected RubyCGIGateway(File f, Ruby runtime) {
        _file = f;
        runenv = new RuntimeEnvironment(runtime);
    }

    protected String getContent() throws IOException {
        return StreamUtil.readFully(_file);
    }

    protected InputStream getInputStream() throws IOException {
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

    public void handle(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {
        Scope s = ar.getScope();
        runenv.addCGIEnv(s, env);
        runenv.commonSetup(s);
        _setIO(s, stdin, stdout);
        try {
            runenv.commonRun(_parseCode(), s);
        }
        catch (IOException e) {
            System.err.println("RubyCGIGateway.handle: " + e);
        }
    }

    protected synchronized Node _parseCode() throws IOException {
        final long lastModified = _file.lastModified();
        if (_node == null || _lastCompile < lastModified) {
            _node = _parseContent(_file.getPath());
            _lastCompile = lastModified;
        }
        return _node;
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
        return RuntimeEnvironment.PARSE_RUNTIME.parseFile(new ByteArrayInputStream(bytes), filePath, null);
    }

    /**
     * Set Ruby's $stdin and $stdout so that reading and writing go to the
     * right place. Called from {@link handle} which is called from the CGI
     * gateway.
     */
    protected void _setIO(Scope s, InputStream stdin, OutputStream stdout) {
        Ruby runtime = runenv.getRuntime(s);
        runtime.getGlobalVariables().set("$stdin", new RubyIO(runtime, stdin));
        runtime.getGlobalVariables().set("$stdout", new RubyIO(runtime, stdout));
    }
}
