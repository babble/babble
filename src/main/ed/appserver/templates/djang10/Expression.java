package ed.appserver.templates.djang10;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSMap;
import ed.js.JSNumericFunctions;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.js.func.JSFunctionCalls3;

public class Expression {
    public final static String GET_PROP = "getProp";
    public final static String LOOKUP = "lookup";
    public final static String CALL = "call";
    public final static String IS_TRUE = "isTrue";
    public final static String DEFAULT_VALUE = "defaultValue";

    public final static Object UNDEFINED_VALUE = new Object();

    private final String expression;
    private final boolean callLeaf;
    private final Node parsedExpression;

    public Expression(String expression) throws TemplateException {
        this(expression, true);
    }

    public Expression(String expression, boolean callLeaf) throws TemplateException {
        this.expression = expression;
        this.callLeaf = callLeaf;

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

    public String toJavascript() throws TemplateException {
        StringBuilder buffer = new StringBuilder();
        Node child = parsedExpression.getFirstChild();
        toJavascript(buffer, child, callLeaf);
        return buffer.toString();
    }

    private static void toJavascript(StringBuilder buffer, Node node, boolean autoCall) throws TemplateException {
        switch (node.getType()) {
        case Token.GETELEM:
        case Token.GETPROP:
            buffer.append(JSHelper.NS + "." + GET_PROP + "(");
            toJavascript(buffer, node.getFirstChild(), true);
            buffer.append(", ");
            toJavascript(buffer, node.getLastChild(), true);
            buffer.append(", " + autoCall + ")");
            break;

        case Token.CALL:
            buffer.append(JSHelper.NS + "." + CALL + "(");

            Node callArgs = node.getFirstChild();

            if (callArgs.getType() == Token.GETELEM || callArgs.getType() == Token.GETPROP) {
                toJavascript(buffer, callArgs.getFirstChild(), true);
                buffer.append(", ");
                toJavascript(buffer, callArgs.getLastChild(), false);
            } else {
                buffer.append("null");
                buffer.append(", ");
                toJavascript(buffer, callArgs, false);
            }

            // arguments
            callArgs = callArgs.getNext();
            while (callArgs != null) {
                buffer.append(", ");
                toJavascript(buffer, callArgs, true);

                callArgs = callArgs.getNext();
            }
            buffer.append(")");
            break;

        case Token.NAME:
            buffer.append(JSHelper.NS + "." + LOOKUP + "(\"" + node.getString() + "\", " + autoCall + ")");
            break;

        case Token.ARRAYLIT:
            buffer.append("[");
            Node arrElem = node.getFirstChild();
            while (arrElem != null) {
                toJavascript(buffer, arrElem, true);

                arrElem = arrElem.getNext();
                if (arrElem != null)
                    buffer.append(",");
            }
            buffer.append("]");
            break;

        case Token.STRING:
            buffer.append("\"" + node.getString() + "\"");
            break;

        case Token.NUMBER:
            double n = node.getDouble();
            if (JSNumericFunctions.couldBeInt(n))
                buffer.append(Integer.toString((int) n));
            else
                buffer.append(n);

            break;

        case Token.NULL:
            buffer.append("null");
            break;

        case Token.TRUE:
            buffer.append("true");
            break;

        case Token.FALSE:
            buffer.append("false");
            break;

        default:
            throw new TemplateException("Invalid token: " + node);
        }
    }

    public String toString() {
        return expression;
    }

    public static Map<String, JSFunction> getHelpers() {
        HashMap<String, JSFunction> helpers = new HashMap<String, JSFunction>();

        helpers.put(LOOKUP, new JSFunctionCalls2() {
            public Object call(Scope scope, Object name, Object autoCall, Object[] extra) {
                if (!(name instanceof JSString))
                    return UNDEFINED_VALUE;

                return lookupSymbol(scope, name.toString(), autoCall == Boolean.TRUE);
            }
        });
        helpers.put(GET_PROP, new JSFunctionCalls3() {
            public Object call(Scope scope, Object obj, Object prop, Object autoCall, Object[] extra) {
                if (!(obj instanceof JSObject))
                    return UNDEFINED_VALUE;

                return getProp(scope, (JSObject) obj, prop, autoCall == Boolean.TRUE);
            }
        });
        helpers.put(CALL, new JSFunctionCalls2() {
            public Object call(Scope scope, Object thisObj, Object method, Object[] extra) {
                if (thisObj != null && !(thisObj instanceof JSObject))
                    return UNDEFINED_VALUE;

                return Expression.call(scope, (JSObject) thisObj, method, extra);
            }
        });
        helpers.put(IS_TRUE, new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return isTrue(p0);
            }
        });
        helpers.put(DEFAULT_VALUE, new JSFunctionCalls2() {
            public Object call(Scope scope, Object value, Object defaultValue, Object[] extra) {
                return defaultValue(value, defaultValue);
            }
        });

        return helpers;
    }

    public static Object lookupSymbol(Scope scope, String symbol, boolean autoCall) {
        Object varValue;
        try {
            if (UNDEFINED_VALUE == symbol || symbol == null)
                return UNDEFINED_VALUE;

            Context contextStack = (Context) scope.get(JSWriter.CONTEXT_STACK_VAR);
            varValue = contextStack.get(symbol);

            if (varValue == null)
                varValue = contextStack.containsKey(symbol.toString()) ? null : UNDEFINED_VALUE;

            // XXX: fallback on scope look ups
            if (varValue == UNDEFINED_VALUE) {
                varValue = scope.get(symbol);

                if (varValue == null) {
                    varValue = scope.keySet().contains(symbol.toString()) ? null : UNDEFINED_VALUE;
                }
            }

            if (autoCall && varValue instanceof JSFunction && !(varValue instanceof JSCompiledScript))
                varValue = ((JSFunction) varValue).call(scope.child());

        } catch (Throwable t) {
            varValue = UNDEFINED_VALUE;
        }
        return varValue;
    }

    public static Object getProp(Scope scope, JSObject obj, Object propName, boolean autoCall) {
        if (obj == null || propName == null)
            return UNDEFINED_VALUE;

        Object ret;

        if (obj instanceof JSArray && propName instanceof Number) {
            if (((JSArray) obj).size() <= ((Number) propName).longValue())
                return UNDEFINED_VALUE;
        } else if (!obj.keySet().contains(propName))
            return UNDEFINED_VALUE;

        ret = obj.get(propName);

        if (autoCall && ret instanceof JSFunction && !(ret instanceof JSCompiledScript))
            ret = ((JSFunction) ret).callAndSetThis(scope.child(), obj, null);

        return ret;
    }

    public static Object call(Scope scope, JSObject thisObj, Object method, Object[] params) {
        if (method == null || method == UNDEFINED_VALUE)
            return UNDEFINED_VALUE;

        JSFunction func;
        if (thisObj == null) {
            if (!(method instanceof JSFunction))
                method = scope.get(method);

            if (!(method instanceof JSFunction))
                return UNDEFINED_VALUE;

            func = (JSFunction) method;
        } else {
            if (!(thisObj instanceof JSObject))
                return UNDEFINED_VALUE;

            Object temp = ((JSObject) thisObj).get(method);

            if (!(temp instanceof JSFunction))
                return UNDEFINED_VALUE;

            func = (JSFunction) temp;
        }

        if (thisObj != null) {
            return func.callAndSetThis(scope.child(), thisObj, params);
        } else {
            return func.call(scope.child(), params);
        }
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
}
