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

import java.util.Collection;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

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
        nodes.__render(scope, context, (JSFunction)scope.get("print"));
        
        return null;
    }
}
