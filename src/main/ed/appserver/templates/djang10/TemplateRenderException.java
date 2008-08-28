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
