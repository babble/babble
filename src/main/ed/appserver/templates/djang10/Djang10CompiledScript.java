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
        } catch(RuntimeException e) {
            StackTraceHolder.getInstance().fix(e);
            fix(e);
            throw e;
        } 
        return null;
    }
    
    
    static void fix(Throwable t) {
        for(; t != null; t = t.getCause())
            _fix(t);
    }
    static void _fix(Throwable t) {
        StackTraceElement[] oldTrace = t.getStackTrace();
        ArrayList<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
        
        for(int i=0; i<oldTrace.length; i++) {
            StackTraceElement element = oldTrace[i];
            
            //hide NodeWrapper & the callAndSetThis call before it
            if(element.getClassName().startsWith(NodeWrapper.class.getName())) {
                if(i+1 < oldTrace.length 
                        && JSFunction.class.getName().equals(oldTrace[i+1].getClassName()) 
                        && "callAndSetThis".equals(oldTrace[i+1].getMethodName())) 
                    i++;
                
                continue;
            }
            
            //hide the functor wrappers
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
}
