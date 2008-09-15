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

import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.log.Logger;

public class Node extends JSObjectBase {

    public Node() {
        super(CONSTRUCTOR);
    }
    protected Node(Constructor constructor) {
        super(constructor);
    }
    
    private static JSString render(Scope scope, JSObject thisObj, Context context) {
        Printer.RedirectedPrinter printer = new Printer.RedirectedPrinter();
        
        ((JSFunction)thisObj.get("__render")).call(scope.child(), context, printer);
        
        return printer.getJSString();
    }
    private static void __render(Scope scope, JSObject thisObj, Context context, JSFunction printer) {
        JSString result = (JSString) ((JSFunction)thisObj.get("render")).call(scope.child(), context);
        
        printer.call(scope.child(), result);
    }
    private static NodeList get_nodes_by_type(Scope scope, JSObject thisObj, JSFunction constructor) {
        NodeList nodelist = new NodeList();
        
        Object obj = thisObj;
        while(obj instanceof JSObject) {
            if(obj == constructor.getPrototype()) {
                nodelist.add(thisObj);
                break;
            }
            obj = ((JSObject)obj).get("__proto__");
        }
        
        //recurse through children
        JSArray children = (JSArray) thisObj.get("nodelist");
        if (children != null) {
            for (Object childObj : children) {
                JSFunction child_fn = (JSFunction)((JSObject)childObj).get("get_nodes_by_type");
                JSArray childNodes = (JSArray)child_fn.callAndSetThis(scope.child(), childObj, new Object[] { constructor });
                nodelist.addAll(childNodes);
            }
        }
        
        return nodelist;
    }
    private static String toString_() {
        return "<Node>";
    }
    
    //Functors
    private static class renderFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object contextObj, Object[] extra) {                    
            JSObject thisObj = (JSObject)scope.getThis();
            Context context = (Context) contextObj;
            
            return render(scope, thisObj, context);
        }
    }
    private static class __renderFunc extends JSFunctionCalls2 {
        public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            Context context = (Context)contextObj;
            JSFunction printer = (JSFunction)printerObj;
            
            __render(scope, thisObj, context, printer);
            return null;
        }
    }
    private static class get_nodes_by_typeFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object constructorObj, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            JSFunction constructor = (JSFunction)constructorObj;
            
            return get_nodes_by_type(scope, thisObj, constructor);
        }
    }
    private static class toString_Func extends JSFunctionCalls0 {
        public toString_Func(Scope scope, String name) {
            super(scope, name);
        }

        public Object call(Scope scope, Object[] extra) {
            return toString_();
        } 
    }
    
    //Constructor
    public static Constructor CONSTRUCTOR = new Constructor(); 
    public static class Constructor  extends JSFunctionCalls0 {
        public Object call(Scope scope, Object[] extra) {
            //noop
            return null;
        }
        public JSObject newOne() {
            return new Node();
        };
        protected void init() {
            _prototype.set("render", new renderFunc());
            _prototype.set("__render", new __renderFunc());
            _prototype.set("get_nodes_by_type", new get_nodes_by_typeFunc());
            _prototype.set("toString", new toString_Func(Scope.newGlobal().child(), "toString"));
        }
    };
    

    
    
    //Text Node
    public static class TextNode extends Node {
        public TextNode(JSString text) {
            super(CONSTRUCTOR);
            this.set("text", text);
        }
        
        public static Node.Constructor CONSTRUCTOR = new Constructor() {
            public Object call(Scope scope, Object[] extra) {
                JSObject node = (JSObject)scope.getThis();
                node.set("text", extra[0]);
                return null;
            }
            protected void init() {
                super.init();
                _prototype.set("__proto__", Node.CONSTRUCTOR.getPrototype());
                _prototype.set("__render", new JSFunctionCalls2() {
                    public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                        JSObject thisObj = (JSObject)scope.getThis();
                        ((JSFunction)printerObj).call(scope.child(), thisObj.get("text"));
                        return null;
                    }
                });
                _prototype.set("toString", new JSFunctionCalls0(Scope.newGlobal().child(), "toString") {
                    public Object call(Scope scope, Object[] extra) {
                        JSObject thisObj = (JSObject)scope.getThis();
                        String str = String.valueOf(thisObj.get("text"));
                        str = str.replaceAll("\\s+", " ");
                        str = str.substring(0, Math.min(20, str.length()));
                        return "<Text Node: \"" + str + "\">";
                    };
                });
            }
            public JSObject newOne() {
                return new TextNode(null);
            }
        };
    }

    
    
    
    public static class VariableNode extends Node {
        private final Logger log;
        
        public VariableNode(FilterExpression expression) {
            super(CONSTRUCTOR);
            log = Logger.getRoot().getChild("djang10").getChild("VariableNode");

            this.set("expression", expression);
        }
        
        private static void __render(Scope scope, JSObject thisObj, Context context, JSFunction oldPrinter) {
            FilterExpression expr = (FilterExpression)thisObj.get("expression");
            boolean isAutoEscape = context.get("autoescape") != Boolean.FALSE;

            Printer.RedirectedPrinter printer = new Printer.RedirectedPrinter();                        
            scope = scope.child();
            scope.setGlobal(true);
            scope.set("print", printer);
            
            Object result = expr.resolve(scope, context);
            
            if(result != null && !"".equals(result))
                printer.call(scope, result);
            
            String output = printer.getJSString().toString();

            boolean needsEscape = isAutoEscape && (printer.is_safe() != Boolean.TRUE);
            needsEscape = needsEscape || (printer.is_safe() == Boolean.FALSE);
            
            if(needsEscape)
                output = Encoding._escapeHTML(output);                                

            oldPrinter.call(scope.child(), new JSString(output));
        }
        
        private static String toString(JSObject thisObj) {
            FilterExpression expr = (FilterExpression)thisObj.get("expression");
            
            return "<Variable Node: \"" + expr.toString() + "\">";
        }
        
        
        //Functors
        private static class __renderFunc extends JSFunctionCalls2 {
            public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                JSObject thisObj = (JSObject)scope.getThis();
                Context context = (Context)contextObj;
                JSFunction printer = (JSFunction) printerObj;
                
                __render(scope, thisObj, context, printer);
                return null;
            }
        };
        private static class toStringFunc extends JSFunctionCalls0 {
            public toStringFunc(Scope scope, String name) {
                super(scope, name);
            }
            public Object call(Scope scope, Object[] extra) {
                JSObject thisObj = (JSObject)scope.getThis();
                
                return VariableNode.toString(thisObj);
            }
        };
        
        //Constructor
        public static final Node.Constructor CONSTRUCTOR = new Constructor() {
            public Object call(Scope scope, Object[] extra) {
                JSObject thisObj = (JSObject)scope.getThis();
                thisObj.set("expression", extra[0]);
                return null;
            }
            protected void init() {
                super.init();
                _prototype.set("__proto__", Node.CONSTRUCTOR.getPrototype());
                _prototype.set("__render", new __renderFunc());
                _prototype.set("toString", new toStringFunc(Scope.newGlobal().child(), "toString"));
            }
            public JSObject newOne() {
                return new VariableNode(null);
            }
        };
    }
}
