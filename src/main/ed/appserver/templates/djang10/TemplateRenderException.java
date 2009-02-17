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

package ed.appserver.templates.djang10;

import java.util.Stack;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSObject;

public class TemplateRenderException extends Djang10Exception {
    public TemplateRenderException(Context context, Exception e) {
        super("Failed to render: " + context.getRenderStack().peek(), e);

        Stack<JSObject> renderedNodes = context.getRenderStack();

        StackTraceElement[] trace = new StackTraceElement[renderedNodes.size()];
        int i = trace.length - 1;
        for(JSObject node : renderedNodes) {
            String repr = node.toString();
            Token token = (Token)node.get("__token");

            trace[i--] = new StackTraceElement("", repr, token.getOrigin(), token.getStartLine());
        }

        setStackTrace(trace);
    }
}
