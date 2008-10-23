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
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.KCode;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.AppContext;
import ed.appserver.AppRequest;
import ed.appserver.JSFileLibrary;
import ed.appserver.adapter.cgi.EnvMap;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.appserver.adapter.cgi.CGIAdapter;
import ed.util.Dependency;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RubyCGIGateway extends CGIAdapter {

    protected RuntimeEnvironment runenv;
    protected File file;
    protected Node node;
    protected long lastCompile;

    public RubyCGIGateway(File f) {
        this(f, null);
    }

    /** For testing and {@link RubyLanguage} use. */
    protected RubyCGIGateway(File f, Ruby runtime) {
        file = f;
        runenv = new RuntimeEnvironment(runtime);
    }

    protected String getContent() throws IOException {
        return StreamUtil.readFully(file);
    }

    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public long lastUpdated(Set<Dependency> visitedDeps) {
        return file.lastModified();
    }

    public String getName() {
        return file.toString();
    }

    public File getFile() {
        return file;
    }

    public void handleCGI(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {
        Scope s = ar.getScope();
        runenv.addCGIEnv(s, env);
        runenv.commonSetup(s);
        setIO(s, stdin, stdout);
        try {
            runenv.commonRun(getAST(), s);
        }
        catch (IOException e) {
            System.err.println("RubyCGIGateway.handle: " + e);
        }
    }

    protected synchronized Node getAST() throws IOException {
        final long lastModified = file.lastModified();
        if (node == null || lastCompile < lastModified) {
            node = parseContent(file.getPath());
            lastCompile = lastModified;
        }
        return node;
    }

    protected Node parseContent(String filePath) throws IOException {
        return runenv.parse(getContent(), filePath);
    }

    /**
     * Set Ruby's $stdin and $stdout so that reading and writing go to the
     * right place. Called from {@link handleCGI} which is called from the CGI
     * gateway.
     */
    protected void setIO(Scope s, InputStream stdin, OutputStream stdout) {
        Ruby runtime = runenv.getRuntime(s);
        runtime.getGlobalVariables().set("$stdin", new RubyIO(runtime, stdin));
        runtime.getGlobalVariables().set("$stdout", new RubyIO(runtime, stdout));
    }
}
