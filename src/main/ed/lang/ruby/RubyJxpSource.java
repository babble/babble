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
import ed.appserver.jxp.JxpSource;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RubyJxpSource extends JxpSource.JxpFileSource {

    protected RuntimeEnvironment runenv;
    protected Node node;
    protected long lastCompile;

    public RubyJxpSource(File f) {
        this(f, null);
    }

    /** For testing and {@link RubyLanguage} use. */
    protected RubyJxpSource(File f, Ruby runtime) {
        super(f);
        runenv = new RuntimeEnvironment(runtime);
    }

    public Ruby getRuntime(Scope s) { return runenv.getRuntime(s); }

    public JSFunction getFunction() throws IOException {
        final Node node = parseCode();
        return new ed.js.func.JSFunctionCalls0() {
            public Object call(Scope s, Object unused[]) { return RubyObjectWrapper.toJS(s, _doCall(node, s, unused)); }
        };
    }

    protected IRubyObject _doCall(Node node, Scope s, Object unused[]) {
        runenv.commonSetup(s);
        setOutput(s);
        return runenv.commonRun(node, s);
    }

    protected synchronized Node parseCode() throws IOException {
        final long lastModified = getFile().lastModified();
        if (node == null || lastCompile < lastModified) {
            node = parseContent(getFile().getPath());
            lastCompile = lastModified;
        }
        return node;
    }

    protected Node parseContent(String filePath) throws IOException {
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
     * Set Ruby's $stdout so that print/puts statements output to the right
     * place. If we have no HttpResponse (for example, we're being run outside
     * the app server), then nothing happens.
     */
    protected void setOutput(Scope s) {
        HttpResponse response = (HttpResponse)s.get("response");
        if (response != null) {
            Ruby runtime = runenv.getRuntime(s);
            runtime.getGlobalVariables().set("$stdout", new RubyIO(runtime, new RubyJxpOutputStream(response.getJxpWriter())));
        }
    }
}
