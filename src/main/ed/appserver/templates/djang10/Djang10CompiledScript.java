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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.StackTraceHolder;

public class Djang10CompiledScript extends JSFunctionCalls1 {
    private final NodeList nodes;
    
    
    public Djang10CompiledScript(NodeList nodes, Collection<Library> loadedLibraries) {
        super();
        this.nodes = nodes;
        
        JSArray arr = new JSArray();
        arr.addAll(loadedLibraries);
        set("loadedLibraries", new JSArray( arr));
        
        set("nodelist", nodes);
    }


    public Object call(Scope scope, Object contextObj, Object[] extra) {
        
        //init context
        Context context = (contextObj instanceof JSObject)? new Context((JSObject)contextObj) : new Context(scope);
        context.push();
        
        //render
        try {
            nodes.__render(scope, context, (JSFunction)scope.get("print"));
        } catch(Exception e) {
            StackTraceHolder.getInstance().fix(e);
            fix(e);
            throw new RenderException(e.getMessage(), context.getRenderStack(),  e);
        } 
        return null;
    }
    
    
    private static void fix(Throwable t) {
        StackTraceElement[] oldTrace = t.getStackTrace();
        ArrayList<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
        
        for(int i=0; i<oldTrace.length; i++) {
            StackTraceElement element = oldTrace[i];
            
            if(element.getClassName().startsWith(NodeWrapper.class.getName())) {
                if(i+1 < oldTrace.length 
                        && JSFunction.class.getName().equals(oldTrace[i+1].getClassName()) 
                        && "callAndSetThis".equals(oldTrace[i+1].getMethodName())) 
                    i++;
                
                continue;
            }
            
            if(i+1 < oldTrace.length) {
                String proxyName = oldTrace[i].getClassName() + "$" + oldTrace[i].getMethodName() + "Func";
                if(proxyName.startsWith("ed.appserver.templates.djang10") && proxyName.equals(oldTrace[i+1].getClassName())) {
                    newTrace.add(element);
                    i++;
                    continue;
                }
            }            
            
            newTrace.add(element);
        }
        t.setStackTrace(newTrace.toArray(new StackTraceElement[0]));
    }
    
    static class RenderException extends RuntimeException {
        public RenderException(String msg, Stack<JSObject> renderStack, Throwable cause) {
            super(msg, cause);
            StackTraceElement[] stackTrace = new StackTraceElement[renderStack.size()];
            
            int i=stackTrace.length - 1;
            for(JSObject node : renderStack) {
                String repr = node.toString();
                Token token = (Token)node.get("__token");
                
                stackTrace[i--] = new StackTraceElement("", repr, token.getOrigin(), token.getStartLine());
            }
            
            setStackTrace(stackTrace);
        }
    }
}
