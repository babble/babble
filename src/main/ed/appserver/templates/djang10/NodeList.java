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

import java.util.List;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;

public class NodeList extends JSArray {

    public NodeList() {
        this.set("__render", new __renderFunc());
        this.set("render", new renderFunc());
        this.set("get_nodes_by_type", new get_nodes_by_typeFunc());
    }

    public void __render(Scope scope, Context context, JSFunction printer) {
        for (Object nodeObj : this) {
            JSObject node = (JSObject) nodeObj;
            JSFunction fn = (JSFunction) node.get("__render");
            fn.callAndSetThis(scope.child(), node, new Object[] { context, printer });
        }
    }
    public JSString render(Scope scope, Context context) {
        Printer.RedirectedPrinter printer = new Printer.RedirectedPrinter();
        __render(scope, context, printer);
        
        return printer.getJSString();
    }
    public NodeList get_nodes_by_type(Scope scope, JSFunction constructor) {
        NodeList nodelist = new NodeList();

        for (Object nodeObj : this) {
            JSObject node = (JSObject) nodeObj;
            JSFunction fn = (JSFunction) node.get("get_nodes_by_type");
            NodeList childNodes = (NodeList) fn.callAndSetThis(scope.child(), node, new Object[] { constructor });
            nodelist.addAll((List) childNodes);
        }
        return nodelist;
    }
    
    
    //Functors
    public static class __renderFunc extends JSFunctionCalls2 {
        public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
            NodeList thisObj = (NodeList) scope.getThis();
            Context context = (Context)contextObj;
            JSFunction printer = (JSFunction)printerObj;
            
            thisObj.__render(scope, context, printer);
            return null;
        }
    };
    public static class renderFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object contextObj, Object[] extra) {
            NodeList thisObj = (NodeList) scope.getThis();
            Context context = (Context)contextObj;
            
            return thisObj.render(scope, context);
        }
    };
    public static class get_nodes_by_typeFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object constructorObj, Object[] extra) {
            NodeList thisObj = (NodeList) scope.getThis();
            JSFunction constructor = (JSFunction)constructorObj;
            
            return thisObj.get_nodes_by_type(scope, constructor);
        }
    };
}
