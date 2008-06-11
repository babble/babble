package ed.appserver.templates.djang10.tagHandlers.loadTag;

import java.util.ArrayList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class JSCompilationPhaseParser extends JSObjectBase {
    private Parser parser;
    private List<Node> parsedNodes;

    public JSCompilationPhaseParser(Parser parser) {
        super(CONSTRUCTOR);
        this.parser = parser;
        this.parsedNodes = new ArrayList<Node>();
    }

    private JSArray parse(JSArray parse_until) throws TemplateException {
        String[] tags = new String[parse_until == null? 0 : parse_until.size()];
        for(int i=0; i< tags.length; i++)
            tags[i] = parse_until.getAsString(i);
        
        List<Node> nodes = parser.parse(tags);
        this.parsedNodes.addAll(nodes);
        
        JSArray nodelist = create_nodelist();
        nodelist.addAll(nodes);
        
        return nodelist;
    }
    
    private Token next_token() {
        return parser.nextToken();
    }

    private JSArray create_nodelist() {
        JSArray nodelist = new JSArray();
        nodelist.set("render", new JSFunctionCalls1() {
            public Object call(Scope scope, Object cxtObj, Object[] extra) {
                throw new NotImplementedException();
            }
        });
        
        return nodelist;
    }
    
    List<Node> getAndClearNodes() {
        ArrayList<Node> parsedNodes = new ArrayList<Node>(this.parsedNodes);
        this.parsedNodes.clear();
        return parsedNodes;
    }
    
    public static JSFunction CONSTRUCTOR = new JSFunctionCalls0() {
        public Object call(Scope scope, Object[] extra) {
            throw new NotImplementedException();
        }
        public JSObject newOne() {
            throw new NotImplementedException();
        }
        protected void init() {
            _prototype.set("parse", new JSFunctionCalls1() {
                public Object call(Scope scope, Object parse_untilObj, Object[] extra) {
                    JSCompilationPhaseParser thisObj = (JSCompilationPhaseParser)scope.getThis();
                    JSArray parse_until = (JSArray)parse_untilObj;
                    try {
                        return thisObj.parse(parse_until);
                    } catch(TemplateException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            _prototype.set("next_token", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    JSCompilationPhaseParser thisObj = (JSCompilationPhaseParser)scope.getThis();
                    return thisObj.next_token();
                }
            });
            _prototype.set("create_nodelist", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    JSCompilationPhaseParser thisObj = (JSCompilationPhaseParser)scope.getThis();
                    return thisObj.create_nodelist();
                }
            });
        }
    };
}
