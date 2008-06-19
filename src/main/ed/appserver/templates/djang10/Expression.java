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
    
    public Expression(String expression) {
        this();
        this.expression = expression;
        init();
    }
    private void init() {
        CompilerEnvirons ce = new CompilerEnvirons();
        org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(ce, ce.getErrorReporter());
        ScriptOrFnNode scriptNode;

        try {
            scriptNode = parser.parse(expression, "foo", 0);
        } catch (Throwable t) {
            throw new TemplateException("Failed to parse expression: " + expression, t);
        }

        if (scriptNode.getFirstChild() != scriptNode.getLastChild())
            throw new TemplateException("Only one expression is allowed, got: "+expression);

        parsedExpression = scriptNode.getFirstChild();

        if (parsedExpression.getType() != org.mozilla.javascript.Token.EXPR_RESULT)
            throw new TemplateException("Not an expression: " + expression);
    }

    public boolean is_literal() {
        if(parsedExpression.getFirstChild() != parsedExpression.getLastChild())
            return false;
        
        int childType = parsedExpression.getFirstChild().getType();
        if(childType == Token.STRING || childType == Token.NUMBER || childType == Token.NULL)
            return true;
        
        //FIXME: check array litrals
        return false;
    }
    public Object get_literal_value() {
        if(!is_literal())
            throw new IllegalStateException("Expression is not a literal: " + expression);
        
        return resolve(null, null);
    }
    
    
    public Object resolve(Scope scope, Context ctx) {

        Object obj = resolve(scope, ctx, parsedExpression.getFirstChild(), true);
        return JSNumericFunctions.fixType(obj);
    }
    private Object resolve(Scope scope, Context ctx, Node node, boolean autoCall) {
        Object temp;
        switch (node.getType()) {
        case Token.GETELEM:
        case Token.GETPROP:
            //get the object
            temp = resolve(scope, ctx, node.getFirstChild(), true);
            if(temp == null || temp == UNDEFINED_VALUE)
                return UNDEFINED_VALUE;
            if(!(temp instanceof JSObject))
                throw new TemplateException("Can't handle native objects of type:" + temp.getClass().getName() + ", expression: " + expression);
            JSObject obj = (JSObject)temp;
            
            //get the property
            Object prop = resolve(scope, ctx, node.getLastChild(), true);
            if(prop == null || prop == UNDEFINED_VALUE)
                return UNDEFINED_VALUE;
            
            Object val = obj.get(prop);
            if(val == null)
                val = obj.containsKey(prop.toString()) ? null : UNDEFINED_VALUE;
            
            if (autoCall && val instanceof JSFunction && !(val instanceof JSCompiledScript) && !(val instanceof Djang10CompiledScript))
                val = ((JSFunction)val).callAndSetThis(scope.child(), obj, new Object[0]);
            
            return val;

        case Token.CALL:
            JSObject callThisObj = null;
            JSFunction callMethodObj = null;
            Node callArgs = node.getFirstChild();
            
            if (callArgs.getType() == Token.GETELEM || callArgs.getType() == Token.GETPROP) {
                //get the method
                temp = resolve(scope, ctx, callArgs, false);
                if(temp == null || temp == UNDEFINED_VALUE)
                    return UNDEFINED_VALUE;
                if(!(temp instanceof JSFunction))
                    throw new TemplateException("Can only call functions, expression: " + expression);
                callMethodObj = (JSFunction)temp;
                
                //get this
                callThisObj = (JSObject)resolve(scope, ctx, callArgs.getFirstChild(), true);;
            } else {
                temp = resolve(scope, ctx, callArgs, false);
                if(!(temp instanceof JSFunction))
                    throw new TemplateException("Can only call functions, expression: " + expression);
                callMethodObj = (JSFunction)temp;
            }

            // arguments
            List<Object> argList = new ArrayList<Object>();             
            
            for(callArgs = callArgs.getNext(); callArgs != null; callArgs = callArgs.getNext())
                argList.add(resolve(scope, ctx, callArgs, true));

            Scope callScope = scope.child();
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
                lookupValue = scope.get(node.getString());

                if (lookupValue == null) {
                    lookupValue = scope.keySet().contains(node.getString()) ? null : UNDEFINED_VALUE;
                }
            }
            
            if (autoCall && lookupValue instanceof JSFunction && !(lookupValue instanceof JSCompiledScript))
                lookupValue = ((JSFunction) lookupValue).call(scope.child());
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
        if((value instanceof JSString) && value.equals(""))
            return false;
        if("".equals(value))
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
