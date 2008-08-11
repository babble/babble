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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ed.ext.org.mozilla.javascript.CompilerEnvirons;
import ed.ext.org.mozilla.javascript.Node;
import ed.ext.org.mozilla.javascript.ScriptOrFnNode;
import ed.ext.org.mozilla.javascript.Token;

import ed.js.JSArray;
import ed.js.JSException;
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

    private static final List<Integer> SUPPORTED_TOKENS = Arrays.asList(
        Token.GETELEM,
        Token.GETPROP,
        Token.CALL,
        Token.NAME,
        Token.ARRAYLIT,
        Token.STRING,
        Token.POS,
        Token.NEG,
        Token.NUMBER,
        Token.NULL,
        Token.TRUE,
        Token.FALSE
    );
    
    private String expression;
    private Node parsedExpression;
    private Boolean isLiteral;

    protected Expression() {
        super(CONSTRUCTOR);
        isLiteral = null;
    }
    
    public Expression(String expression) {
        this();
        this.expression = expression;
        init();
    }
    private void init() {
        CompilerEnvirons ce = new CompilerEnvirons();
        ed.ext.org.mozilla.javascript.Parser parser = new ed.ext.org.mozilla.javascript.Parser(ce, ce.getErrorReporter());
        ScriptOrFnNode scriptNode;

        try {
            scriptNode = parser.parse(expression, "foo", 0);
        } catch (Exception t) {
            throw new TemplateException("Failed to parse expression: " + expression, t);
        }

        if (scriptNode.getFirstChild() != scriptNode.getLastChild())
            throw new TemplateException("Only one expression is allowed, got: "+expression);

        parsedExpression = scriptNode.getFirstChild();

        if (parsedExpression.getType() != ed.ext.org.mozilla.javascript.Token.EXPR_RESULT)
            throw new TemplateException("Not an expression: " + expression);
        
        //Verify the expression
        Queue<Node> workQueue = new LinkedList<Node>();
        workQueue.add(parsedExpression.getFirstChild());
        
        while(!workQueue.isEmpty()) {
            Node token = workQueue.remove();
            if(!SUPPORTED_TOKENS.contains(token.getType()))
                throw new TemplateException("Failed to parse expression: " + expression +". Unsupported token: " + token);
            
            for(token = token.getFirstChild(); token !=null; token = token.getNext())
                workQueue.add(token);
        }
        
    }

    public boolean is_literal() {
        if(isLiteral == null)
            isLiteral = is_literal(this.parsedExpression);
        
        return isLiteral;
    }
    private static boolean is_literal(Node node) {
        if(node.getType() == Token.NAME)
            return false;
        
        for(node = node.getFirstChild(); node != null; node = node.getNext())
            if(!is_literal(node))
                return false;
        
        return true;
    }
    public Object get_literal_value() {
        if(!is_literal())
            throw new IllegalStateException("Expression is not a literal: " + expression);
        
        return resolve(null, null);
    }
    
    
    public Object resolve(Scope scope, Context ctx) {

        Object obj = UNDEFINED_VALUE;
        try {
            obj = resolve(scope, ctx, parsedExpression.getFirstChild(), true);
        } finally {
            if(Djang10Source.DEBUG && obj == UNDEFINED_VALUE)
                System.out.println("Failed to resolve: " + this);
        }
        obj = JSNumericFunctions.fixType(obj);
        
        if(is_literal() && (obj instanceof JSObject))
            JSHelper.mark_safe((JSObject)obj);
        
        return obj;
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
            
            if (autoCall && val instanceof JSFunction && !(val instanceof JSCompiledScript) && !(val instanceof Djang10CompiledScript)) {
                try {
                    val = ((JSFunction)val).callAndSetThis(scope.child(), obj, new Object[0]);
                }
                catch(JSException e) {
                    if(isTrue(e.get("silent_variable_failure")))
                        return UNDEFINED_VALUE;
                    
                    temp = e.getObject();
                    if(temp instanceof JSObject && isTrue( ((JSObject)temp).get("silent_variable_failure") ))
                        return UNDEFINED_VALUE;
                    
                    throw e;
                }
            }
            
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
            
            try {
                if(callThisObj != null)
                    return callMethodObj.callAndSetThis(callScope, callThisObj, argList.toArray());
                else
                    return callMethodObj.call(callScope, argList.toArray());
            }
            catch(JSException e) {
                if(isTrue(e.get("silent_variable_failure")))
                    return UNDEFINED_VALUE;
                
                temp = e.getObject();
                if(temp instanceof JSObject && isTrue( ((JSObject)temp).get("silent_variable_failure") ))
                    return UNDEFINED_VALUE;
                
                throw e;
            }
            

        case Token.NAME:
            Object lookupValue = ctx.get(node.getString());
            if(lookupValue == null)
                lookupValue = ctx.containsKey(node.getString()) ? null : UNDEFINED_VALUE;

            boolean use_fallabck = true;
            if(ctx.get("__use_globals") instanceof Boolean) {
                use_fallabck = (Boolean)ctx.get("__use_globals");
            }
            else if(JSHelper.get(scope).get("ALLOW_GLOBAL_FALLBACK") instanceof Boolean) {
                use_fallabck = (Boolean)JSHelper.get(scope).get("ALLOW_GLOBAL_FALLBACK");
            }
            
            // XXX: fallback on scope look ups
            if (lookupValue == UNDEFINED_VALUE && use_fallabck) {
                lookupValue = scope.get(node.getString());

                if (lookupValue == null) {
                    lookupValue = scope.keySet().contains(node.getString()) ? null : UNDEFINED_VALUE;
                }
            }
            
            if (autoCall && lookupValue instanceof JSFunction 
                && ! ( lookupValue instanceof JSCompiledScript)
                && ! ( lookupValue instanceof ed.lang.python.JSPyObjectWrapper ) // TODO: this is wrong, but was needed otherwise it called everything
                )
                lookupValue = ((JSFunction) lookupValue).call(scope.child());
            return lookupValue;

            
        case Token.ARRAYLIT:
            JSArray arrayLit = new JSArray();
            
            for(Node arrElem = node.getFirstChild(); arrElem != null; arrElem = arrElem.getNext())
                arrayLit.add(resolve(scope, ctx, arrElem, true));

            return arrayLit;


        case Token.STRING:
            return new JSString(node.getString());

        case Token.POS:
            return resolve(scope, ctx, node.getFirstChild(), true);
            
        case Token.NEG:
            temp = resolve(scope, ctx, node.getFirstChild(), true);
            return (temp instanceof Double)?  -1 * ((Double)temp) :  -1 * ((Integer)temp);

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
            //Should never happen
            throw new IllegalStateException();
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
            thisObj.init();
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
