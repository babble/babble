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
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;

public class NodeList extends JSArray {

    public NodeList() {
        this.set("__render", __render);
        this.set("render", render);
        this.set("get_nodes_by_type", get_nodes_by_type);
    }

    public void __render(Scope scope, Context context, JSFunction printer) {
        for (Object nodeObj : this) {
            JSObject node = (JSObject) nodeObj;
            JSFunction fn = (JSFunction) node.get("__render");
            fn.callAndSetThis(scope.child(), node, new Object[] { context, printer });
        }
    }

    public static final JSFunction __render = new JSFunctionCalls2() {
        public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
            NodeList thisObj = (NodeList) scope.getThis();

            thisObj.__render(scope, (Context) contextObj, (JSFunction) printerObj);
            return null;
        }
    };

    public static final JSFunction render = new JSFunctionCalls1() {
        public Object call(Scope scope, Object contextObj, Object[] extra) {

            Printer.RedirectedPrinter printer = new Printer.RedirectedPrinter();
            
            NodeList thisObj = (NodeList) scope.getThis();
            ((JSFunction) thisObj.get("__render")).callAndSetThis(scope.child(), thisObj, new Object[] { contextObj, printer });
            return printer.getJSString();
        }
    };

    public static final JSFunction get_nodes_by_type = new JSFunctionCalls1() {
        public Object call(Scope scope, Object constructor, Object[] extra) {
            NodeList thisObj = (NodeList) scope.getThis();
            NodeList nodelist = new NodeList();

            for (Object nodeObj : thisObj) {
                JSObject node = (JSObject) nodeObj;
                JSFunction fn = (JSFunction) node.get("get_nodes_by_type");
                JSArray childNodes = (JSArray) fn.callAndSetThis(scope.child(), node, new Object[] { constructor });
                nodelist.addAll((List) childNodes);
            }

            return nodelist;
        }
    };
}
