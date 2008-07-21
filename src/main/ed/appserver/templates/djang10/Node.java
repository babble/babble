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

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;

public class Node extends JSObjectBase {

    public Node() {
        super(CONSTRUCTOR);
    }
    protected Node(Constructor constructor) {
        super(constructor);
    }
    
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
            _prototype.set("render", new JSFunctionCalls1() {
                public Object call(Scope scope, Object contextObj, Object[] extra) {
                    
                    final StringBuilder buffer = new StringBuilder();
                    JSFunctionCalls1 printer = new JSFunctionCalls1() {
                        public Object call(Scope scope, Object str, Object[] extra) {
                            buffer.append(str);
                            return null;
                        }
                    };
                    
                    JSObject thisObj = (JSObject)scope.getThis();
                    ((JSFunction)thisObj.get("__render")).call(scope.child(), contextObj, printer);
                    return new JSString( buffer.toString() );
                }
            });
            _prototype.set("__render", new JSFunctionCalls2() {
                public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                    JSObject thisObj = (JSObject)scope.getThis();
                    String result = ((JSFunction)thisObj.get("render")).call(scope.child(), contextObj).toString();
                    ((JSFunction)printerObj).call(scope, result);
                    return null;
                }
            });
            _prototype.set("get_nodes_by_type", new JSFunctionCalls1() {
                public Object call(Scope scope, Object constructorObj, Object[] extra) {
                    JSArray nodeList = new JSArray();
                    
                    //check prototype chain
                    JSFunction constructor = (JSFunction)constructorObj;
                    Object obj = scope.getThis();
                    while(obj instanceof JSObject) {
                        if(obj == constructor.getPrototype()) {
                            nodeList.add(scope.getThis());
                            break;
                        }
                        obj = ((JSObject)obj).get("__proto__");
                    }
                    
                    //recurse through children
                    NodeList children = (NodeList) this.get("nodelist");
                    if (children != null) {
                        for (Object childObj : children) {
                            JSFunction child_fn = (JSFunction)((JSObject)childObj).get("get_nodes_by_type");
                            JSArray childNodes = (JSArray)child_fn.callAndSetThis(scope.child(), childObj, new Object[] { constructorObj });
                            nodeList.addAll(childNodes);
                        }
                    }
                    return nodeList;
                }
            });
            _prototype.set("toString", new JSFunctionCalls0(Scope.newGlobal().child(), "toString") {
                public Object call(Scope scope, Object[] extra) {
                    return "<Node>";
                } 
            });
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
        public VariableNode(FilterExpression expression) {
            super(CONSTRUCTOR);
            this.set("expression", expression);
        }
        
        public static final Node.Constructor CONSTRUCTOR = new Constructor() {
            public Object call(Scope scope, Object[] extra) {
                JSObject thisObj = (JSObject)scope.getThis();
                thisObj.set("expression", extra[0]);
                return null;
            }
            protected void init() {
                super.init();
                _prototype.set("__proto__", Node.CONSTRUCTOR.getPrototype());
                _prototype.set("__render", new JSFunctionCalls2() {
                    public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                        JSObject thisObj = (JSObject)scope.getThis();
                        FilterExpression expr = (FilterExpression)thisObj.get("expression");
                        JSFunction printer = (JSFunction)printerObj;
                        
                        scope = scope.child();
                        scope.setGlobal(true);
                        scope.set("print", printer);
                        
                        Object result = expr.resolve(scope, (Context)contextObj);
                        if(result != null && result != Expression.UNDEFINED_VALUE)
                            printer.call(scope, result.toString());
                        
                        
                        return null;
                    }
                });
                _prototype.set("toString", new JSFunctionCalls0(Scope.newGlobal().child(), "toString") {
                    public Object call(Scope scope, Object[] extra) {
                        JSObject thisObj = (JSObject)scope.getThis();
                        FilterExpression expr = (FilterExpression)thisObj.get("expression");
                        
                        return "<Variable Node: \"" + expr.toString() + "\">";
                    }
                });
            }
            public JSObject newOne() {
                return new VariableNode(null);
            }
        };
    }
}
