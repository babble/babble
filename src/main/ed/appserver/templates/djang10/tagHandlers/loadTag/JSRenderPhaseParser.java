package ed.appserver.templates.djang10.tagHandlers.loadTag;

import java.util.LinkedList;
import java.util.Queue;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls3;

public class JSRenderPhaseParser extends JSObjectBase {
    public static String NAME = "JSRenderPhaseParser";
    private Queue<JSRenderPhaseNode> compiledNodes;
    
    private JSRenderPhaseParser() {
        super(CONSTRUCTOR);
        
        compiledNodes = new LinkedList<JSRenderPhaseNode>();
    }
    private JSArray parse(JSArray parse_until) {
        if(parse_until == null)
            parse_until = new JSArray();
        
        JSArray nodelist = create_nodelist();
        
        while(!compiledNodes.isEmpty()) {
            if(parse_until.contains(new JSString(compiledNodes.peek().getTagName())))
                return nodelist;

            nodelist.add(compiledNodes.remove());
        }
        
        return nodelist;
    }
    private Token next_token() {
        return compiledNodes.remove().getToken();
    }
    
    private JSArray create_nodelist() {
        JSArray nodelist = new JSArray();
        nodelist.set("render", new JSFunctionCalls0() {
            public Object call(Scope scope, Object[] extra) {
                StringBuilder buffer = new StringBuilder();
                JSArray thisObj = (JSArray)scope.getThis();
                                
                for(Object nodeObj : thisObj) {
                    JSObject node = (JSObject)nodeObj;
                    JSFunction renderFunc = ((JSFunction)node.get("render")); 
                    
                    buffer.append(renderFunc.callAndSetThis(scope.child(), node, extra));
                }
                return buffer.toString();
            }
        });
        return nodelist;
    }
    
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls1() {
        public Object call(Scope scope, Object compiledNodesObj, Object[] extra) {
            JSRenderPhaseParser thisObj = (JSRenderPhaseParser)scope.getThis();
            JSArray compiledNodes = (JSArray)compiledNodesObj;
            
            for(Object nodeObj : compiledNodes) {
                thisObj.compiledNodes.add((JSRenderPhaseNode)nodeObj);
            }
            return null;
        }
        public JSObject newOne() {
            return new JSRenderPhaseParser();
        }
        protected void init() {
            _prototype.set("parse", new JSFunctionCalls1() {
                public Object call(Scope scope, Object parse_untilObj, Object[] extra) {
                    JSRenderPhaseParser thisObj = (JSRenderPhaseParser)scope.getThis();
                    JSArray parse_until = (JSArray)parse_untilObj;
                    return thisObj.parse(parse_until);
                }
            });
            _prototype.set("next_token", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    JSRenderPhaseParser thisObj = (JSRenderPhaseParser)scope.getThis();
                    return thisObj.next_token();
                }
            });
            _prototype.set("create_nodelist", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    JSRenderPhaseParser thisObj = (JSRenderPhaseParser)scope.getThis();
                    return thisObj.create_nodelist();
                }
            });
        }
    };
}


class JSRenderPhaseNode extends JSObjectBase {
    public static String NAME = "JSRenderPhaseNode";
    private JSFunction renderFunc;
    private Token token;
    private String tag;
    
    public JSRenderPhaseNode() {
        super(CONSTRUCTOR);
    }
    String getTagName() {
        return tag;
    }
    
    Token getToken() {
        return token;
    }
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls3() {
        public Object call(Scope scope, Object tagObj, Object tokenObj, Object renderFuncObj, Object[] extra) {
            JSRenderPhaseNode thisObj = (JSRenderPhaseNode)scope.getThis();
            thisObj.tag = tagObj.toString();
            thisObj.token = (Token)tokenObj;
            thisObj.renderFunc = (JSFunction)renderFuncObj;

            return null;
        }
        public JSObject newOne() {
            return new JSRenderPhaseNode();
        };
        protected void init() {
            _prototype.set("render", new JSFunctionCalls1() {
                public Object call(Scope scope, Object cxtObj, Object[] extra) {
                    JSRenderPhaseNode thisObj = (JSRenderPhaseNode)scope.getThis();
                    Scope callScope = scope.child();
                    PrintRedirector print = new PrintRedirector();
                    
                    thisObj.renderFunc.call(callScope, print);
                    return print.buffer.toString();
                };
            });
        }
    };
}

class PrintRedirector extends JSFunctionCalls1 {
    public final StringBuilder buffer = new StringBuilder();
    
    public Object call(Scope scope, Object p0, Object[] extra) {
        buffer.append(p0);
        return null;
    }
}