package ed.appserver.templates.djang10;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
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
                    return buffer.toString();
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
            _prototype.set("toString", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    return scope.getThis().toString();
                } 
            });
        }
    };
    

    
    
    //Text Node
    public static class TextNode extends Node {
        private String text;
        
        public TextNode(String text) {
            super(CONSTRUCTOR);
            this.text = text;
        }

        public String toString() {
            String str = text.replaceAll("\\s+", " ");
            str = str.substring(0, Math.min(20, str.length()));
            return "<Text Node: \"" + str + "\">";
        }
        
        public static Node.Constructor CONSTRUCTOR = new Constructor() {
            public Object call(Scope scope, Object[] extra) {
                TextNode node = (TextNode)scope.getThis();
                node.text = extra[0].toString();
                return null;
            }
            protected void init() {
                super.init();
                _prototype.set("__proto__", Node.CONSTRUCTOR.getPrototype());
                _prototype.set("__render", new JSFunctionCalls2() {
                    public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                        TextNode thisObj = (TextNode)scope.getThis();
                        ((JSFunction)printerObj).call(scope.child(), thisObj.text);
                        return null;
                    }
                });
            }
            public JSObject newOne() {
                return new TextNode(null);
            }
        };
    }

    
    
    
    public static class VariableNode extends Node {
        private FilterExpression expression;

        public VariableNode(FilterExpression expression) {
            super(CONSTRUCTOR);
            this.expression = expression;
        }

        public String toString() {
            return "<Variable Node: \"" + expression.toString() + "\">";
        }
        
        public static final Node.Constructor CONSTRUCTOR = new Constructor() {
            public Object call(Scope scope, Object[] extra) {
                VariableNode thisObj = (VariableNode)scope.getThis();
                thisObj.expression = (FilterExpression)extra[0];
                return null;
            }
            protected void init() {
                super.init();
                _prototype.set("__proto__", Node.CONSTRUCTOR.getPrototype());
                _prototype.set("__render", new JSFunctionCalls2() {
                    public Object call(Scope scope, Object contextObj, Object printerObj, Object[] extra) {
                        VariableNode thisObj = (VariableNode)scope.getThis();
                        Object result = thisObj.expression.resolve(scope, (Context)contextObj);
                        if(result != null && result != Expression.UNDEFINED_VALUE)
                            ((JSFunction)printerObj).call(scope.child(), result.toString());
                        return null;
                    }
                });
            }
            public JSObject newOne() {
                return new VariableNode(null);
            }
        };
    }
}
