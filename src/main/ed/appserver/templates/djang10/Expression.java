package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSMap;
import ed.js.JSNumericFunctions;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class Expression extends JSObjectBase {
    public final static Object UNDEFINED_VALUE = new Object() {
        public String toString() {
            return "UNDEFINED_VALUE";
        };
    };

    private String expression;
    private Node parsedExpression;

    protected Expression() {
        super(CONSTRUCTOR);
    }
    
    public Expression(String expression) throws TemplateException {
        this();
        this.expression = expression;
        init();
    }
    private void init() throws TemplateException {
        CompilerEnvirons ce = new CompilerEnvirons();
        org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(ce, ce.getErrorReporter());
        ScriptOrFnNode scriptNode;

        try {
            scriptNode = parser.parse(expression, "foo", 0);
        } catch (Throwable t) {
            throw new TemplateException("Failed to parse expression", t);
        }

        if (scriptNode.getFirstChild() != scriptNode.getLastChild())
            throw new TemplateException("Only one expression is allowed");

        parsedExpression = scriptNode.getFirstChild();

        if (parsedExpression.getType() != org.mozilla.javascript.Token.EXPR_RESULT)
            throw new TemplateException("Not an expression");
    }

    public boolean is_literal() {
        if(!(parsedExpression.getFirstChild() != parsedExpression.getLastChild()))
            return false;
        
        int childType = parsedExpression.getFirstChild().getType();
        if(childType == Token.STRING || childType == Token.NUMBER || childType == Token.NULL)
            return true;
        
        //FIXME: check array litrals
        return false;
    }
    public Object get_literal_value() {
        if(!is_literal())
            throw new IllegalStateException();
        
        return resolve(null, null);
    }
    
    
    public Object resolve(Scope scope, Context ctx) {
        try {
            Object obj = resolve(scope, ctx, parsedExpression.getFirstChild(), true);
            return JSNumericFunctions.fixType(obj);
            
        } catch(Throwable t) {
            return UNDEFINED_VALUE;
        }
    }
    private static Object resolve(Scope scope, Context ctx, Node node, boolean autoCall) throws TemplateException {
        Object temp;
        switch (node.getType()) {
        case Token.GETELEM:
        case Token.GETPROP:
            JSObject obj = (JSObject)resolve(scope, ctx, node.getFirstChild(), true);
            Object prop = resolve(scope, ctx, node.getLastChild(), true);
            
            Object val = obj.get(prop);
            if(val == null)
                val = ctx.containsKey(node.getString()) ? null : UNDEFINED_VALUE;
            
            if (autoCall && val instanceof JSFunction && !(val instanceof JSCompiledScript) && !(val instanceof Djang10CompiledScript))
                val = ((JSFunction)val).callAndSetThis(scope.child(), obj, new Object[0]);
            
            return val;

        case Token.CALL:
            JSObject callThisObj = null;
            JSFunction callMethodObj = null;
            Node callArgs = node.getFirstChild();
            
            if (callArgs.getType() == Token.GETELEM || callArgs.getType() == Token.GETPROP) {
                callThisObj = (JSObject)resolve(scope, ctx, callArgs.getFirstChild(), true);
                callMethodObj = (JSFunction)callThisObj.get(resolve(scope, ctx, callArgs.getLastChild(), false));
            } else {
                callMethodObj = (JSFunction)resolve(scope, ctx, callArgs, false);
            }

            // arguments
            List<Object> argList = new ArrayList<Object>();             
            
            for(callArgs = callArgs.getNext(); callArgs != null; callArgs = callArgs.getNext())
                argList.add(resolve(scope, ctx, callArgs, true));

            Scope callScope = Scope.getThreadLocal().child();
            callScope.setGlobal(true);
            
            if(callThisObj != null)
                return callMethodObj.callAndSetThis(callScope, callThisObj, argList.toArray());
            else
                return callMethodObj.call(callScope, argList.toArray());

        case Token.NAME:
            Object lookupValue = ctx.get(node.getString());
            if(lookupValue == null)
                lookupValue = ctx.containsKey(node.getString()) ? null : UNDEFINED_VALUE;
            
            // XXX: fallback on scope look ups
            if (lookupValue == UNDEFINED_VALUE) {
                lookupValue = Scope.getThreadLocal().get(node.getString());

                if (lookupValue == null) {
                    lookupValue = Scope.getThreadLocal().keySet().contains(node.getString()) ? null : UNDEFINED_VALUE;
                }
            }
            
            if (autoCall && lookupValue instanceof JSFunction && !(lookupValue instanceof JSCompiledScript))
                lookupValue = ((JSFunction) lookupValue).call(Scope.getThreadLocal().child());
            return lookupValue;

            
        case Token.ARRAYLIT:
            JSArray arrayLit = new JSArray();
            
            for(Node arrElem = node.getFirstChild(); arrElem != null; arrElem = arrElem.getNext())
                arrayLit.add(resolve(scope, ctx, arrElem, true));

            return arrayLit;


        case Token.STRING:
            return new JSString(node.getString());


        case Token.NUMBER:
            double n = node.getDouble();
            if (JSNumericFunctions.couldBeInt(n))
                return (int)n;
            else
                return n;

        case Token.NULL:
            return null;


        case Token.TRUE:
            return true;


        case Token.FALSE:
            return false;


        default:
            throw new TemplateException("Invalid token: " + node);
        }
    }
    
    public String toString() {
        return expression;
    }

    public static boolean isTrue(Object value) {
        if (value == null)
            return false;
        if (value == Boolean.FALSE)
            return false;
        if (value == UNDEFINED_VALUE)
            return false;
        if ((value instanceof Number) && ((Number) value).doubleValue() == 0)
            return false;
        if ((value instanceof JSArray) && ((JSArray) value).size() == 0)
            return false;
        if ((value instanceof JSMap) && ((JSMap) value).keys().size() == 0)
            return false;

        return true;
    }

    public static Object defaultValue(Object value, Object defaultValue) {
        if (value == null || value == UNDEFINED_VALUE)
            return defaultValue;
        return value;
    }
    
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls1() {
        public Object call(Scope scope, Object expressionObj, Object[] extra) {
            Expression thisObj = (Expression)scope.getThis();
            thisObj.expression = expressionObj.toString();
            try {
                thisObj.init();
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        public JSObject newOne() {
            return new Expression();
        }
        protected void init() {
            set("UNDEFINED_VALUE", UNDEFINED_VALUE);
            set("is_true", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    return isTrue(p0);
                }
            });
            set("resolve", new JSFunctionCalls1() {
                public Object call(Scope scope, Object contextObj, Object[] extra) {
                    Expression thisObj = (Expression)scope.getThis();
                    return thisObj.resolve(scope, (Context)contextObj);
                };
            });
            set("toString", new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    Expression thisObj = (Expression)scope.getThis();
                    return thisObj.toString();
                };
            });
        }
    };
}
