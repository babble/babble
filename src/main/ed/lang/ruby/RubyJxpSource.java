/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.ruby;

import java.io.*;

import org.jruby.ast.Node;
import org.jruby.runtime.builtin.IRubyObject;

import ed.appserver.AppContext;
import ed.appserver.jxp.JxpSource;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.net.httpserver.HttpRequest;
import ed.net.httpserver.HttpResponse;

/**
 * Handles .rb file compilation and execution.
 */
public class RubyJxpSource extends JxpSource.JxpFileSource {

    protected Node node;
    protected long lastCompile;

    public RubyJxpSource(File f) {
        super(f);
    }

    public JSFunction getFunction() throws IOException {
        final Node node = getAST();
        return new ed.js.func.JSFunctionCalls0() {
            public Object call(Scope s, Object unused[]) { return RubyObjectWrapper.toJS(s, _doCall(node, s, unused)); }
        };
    }

    protected IRubyObject _doCall(Node node, Scope s, Object unused[]) {
        HttpRequest request = (HttpRequest)s.get("request");
        HttpResponse response = (HttpResponse)s.get("response");
        RuntimeEnvironment runenv = RuntimeEnvironmentPool.getInstance((AppContext)s.get("__instance__"));
        try {
            runenv.setup(s, request == null ? null : request.getInputStream(),
                         response == null ? null : response.getOutputStream());
            return runenv.commonRun(node);
        }
        finally {
            RuntimeEnvironmentPool.releaseInstance(runenv);
        }
    }

    protected synchronized Node getAST() throws IOException {
        final long lastModified = getFile().lastModified();
        if (node == null || lastCompile < lastModified) {
            node = parseContent(getFile().getPath());
            lastCompile = lastModified;
        }
        return node;
    }

    protected Node parseContent(String filePath) throws IOException {
        return RuntimeEnvironment.parse(getContent(), filePath);
    }
}
