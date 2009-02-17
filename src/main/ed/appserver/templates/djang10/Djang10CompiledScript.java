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

import java.util.ArrayList;
import java.util.Collection;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.StackTraceHolder;

public class Djang10CompiledScript extends JSFunctionCalls1 {
    public Djang10CompiledScript(NodeList nodes, Collection<Library> loadedLibraries) {
        super();

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
            ((NodeList)get("nodelist")).__render(scope, context, (JSFunction)scope.get("print"));
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
