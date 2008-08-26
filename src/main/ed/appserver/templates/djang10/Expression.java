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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import ed.appserver.JSFileLibrary;
import ed.ext.org.mozilla.javascript.CompilerEnvirons;
import ed.ext.org.mozilla.javascript.Node;
import ed.ext.org.mozilla.javascript.Token;
import ed.js.JSArray;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSMap;
import ed.js.JSNumericFunctions;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.lang.python.JSPyObjectWrapper;
import ed.log.Level;
import ed.log.Logger;

public class Expression extends JSObjectBase {
    private final Logger log;
    
    public final static Object UNDEFINED_VALUE = new Object() {
        public String toString() {
            return "UNDEFINED_VALUE";
        };
    };

    private static final Set<Integer> SUPPORTED_TOKENS = new HashSet<Integer>(Arrays.asList(
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
    ));
    
    private static final String[] JAVASCRIPT_RESERVED_WORDS = {
            //JavaScript Reserved Words
            "break",
            "case",
            "continue",
            "default",
            "delete",
            "do",
            "else",
            "export",
            "for",
            "function",
            "if",
            "in",   //TODO:implement
            "let",
            "label", 
            "new",  //TODO:implement
            "return",
            "switch",
            "this",
            "typeof", //TODO: implement this
            "var",
            "void",
            "while",
            "with",
            "yield",
            
            //Java Keywords (Reserved by JavaScript)
            "abstract",
            "boolean",
            "byte",
            "catch",
            "char",
            "class",
            "const",
            "debugger",
            "double",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "goto",
            "implements",
            "import",
            "instanceof",   //TODO implement
            "int",
            "interface",
            "long",
            "native",
            "package",
            "private",
            "protected",
            "public",
            "short",
            "static",
            "super",
            "synchronized",
            "throw",
            "throws",
            "transient",
            "try",
            "volatile"            
    };
    
    private String expression;
    private final ed.appserver.templates.djang10.Parser.Token token;
    private Node parsedExpression;
    private Boolean isLiteral;

    public Expression(String expression, ed.appserver.templates.djang10.Parser.Token token, boolean useLiteralEscapes) {
        super(CONSTRUCTOR);
        this.log = Logger.getRoot().getChild("djang10").getChild("expression");
        this.token = token;
        isLiteral = null;
        this.expression = expression;

        CompilerEnvirons ce = new CompilerEnvirons();
        ed.ext.org.mozilla.javascript.Parser parser = new ed.ext.org.mozilla.javascript.Parser(ce, ce.getErrorReporter());
        Node scriptNode;
        
        String processedExpr = preprocess(expression, useLiteralEscapes);
        try {
            scriptNode = parser.parse(processedExpr, "foo", 0);
            scriptNode = postprocess(scriptNode);
        } catch (Exception t) {
            String msg;
            
            if(log.getEffectiveLevel().compareTo(Level.DEBUG) <= 0)
                msg = "Failed to parse original expression [" + expression + "] Processed expr: [" + processedExpr + "]";
            else
                msg = "Failed to parse expression: [" + expression + "]";
            
            throw new TemplateSyntaxError(msg, token, t);
        }
        
        if (scriptNode.getFirstChild() != scriptNode.getLastChild())
            throw new TemplateSyntaxError("Only one expression is allowed. ["+expression + "]", token);

        parsedExpression = scriptNode.getFirstChild();

        if (parsedExpression.getType() != ed.ext.org.mozilla.javascript.Token.EXPR_RESULT)
            throw new TemplateSyntaxError("Not an expression [" + expression + "]", token);
        
        //Verify the expression
        Queue<Node> workQueue = new LinkedList<Node>();
        workQueue.add(parsedExpression.getFirstChild());
        
        while(!workQueue.isEmpty()) {
            Node jsToken = workQueue.remove();
            if(!SUPPORTED_TOKENS.contains(jsToken.getType()))
                throw new TemplateSyntaxError("Failed to parse expression [" + expression +"] Unsupported token: " + jsToken, this.token);
            
            for(jsToken = jsToken.getFirstChild(); jsToken !=null; jsToken = jsToken.getNext())
                workQueue.add(jsToken);
        }
        
    }

    
    private static List<String> splitLiterals(String str) {
        List<String> bits = new ArrayList<String>();
        
        String quotes = "\"'";
        boolean inQuote = false, lastWasEscape = false;
        char quoteChar = '"';
        StringBuilder buffer = new StringBuilder();
        
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            
            if(inQuote) {
                buffer.append(c);
                
                if(!lastWasEscape) {
                    if(c == quoteChar) {
                        inQuote = false;
                        bits.add(buffer.toString());
                        buffer.setLength(0);
                    }
                    else if(c == '\\') {
                        lastWasEscape = true;
                    }
                }
                else {
                    lastWasEscape = false;
                }
            }
            else if(quotes.indexOf(c) > -1) {
                inQuote = true;
                if(buffer.length() != 0) {
                    bits.add(buffer.toString());
                    buffer.setLength(0);
                }
                
                buffer.append(c);
            }
            else {
                buffer.append(c);
            }
        }

        if(buffer.length() != 0)
            bits.add(buffer.toString());

        return bits;
    }
    
    public String preprocess(String exp, boolean useLiteralEscapes) {
        StringBuilder buffer = new StringBuilder();
        String quotes = "\"'";

        Pattern numAlphaIdentifiers = Pattern.compile("(?<!\\w)[0-9_]\\w*[A-Za-z_$]\\w*(?!\\w)"); //matches all identifiers that start with a number
        Pattern numericProp = Pattern.compile("((?:[A-Za-z]\\w*|\\]|\\))\\s*\\.\\s*)([0-9]+)(?![0-9])");    //matches variable.8
                
        for(String bit : splitLiterals(exp)) {
            boolean isQuoted = (quotes.indexOf(bit.charAt(0)) > -1) && (bit.charAt(0) == bit.charAt(bit.length() - 1));
            
            if(!isQuoted) {
                bit = numAlphaIdentifiers.matcher(bit).replaceAll("_$0");
                for(String reservedWord : JAVASCRIPT_RESERVED_WORDS)
                    bit = bit.replaceAll("(?<!\\w)"+reservedWord+"(?!\\w)", "_$0");
                bit = numericProp.matcher(bit).replaceAll("$1_$2");
            }
            else {
                //kill escapes
                if(!useLiteralEscapes)
                    bit = bit.replaceAll("\\\\(?!["+quotes+"])", "\\\\\\\\");
                
                if(bit.charAt(1) == '_'){
                    bit = bit.charAt(0) + "_" + bit.substring(1);
                }
            }
            
            buffer.append(bit);
        }
        
        if(!buffer.toString().equals(exp))
            log.debug("Transformed [" + exp + "] to: [" + buffer + "]. " + "(" + token.getOrigin() + ":" + token.getStartLine() + ")");
            
        
        return buffer.toString();
    }
    private Node postprocess(Node node) {
        if(node.getType() == Token.NAME || node.getType() == Token.STRING) {
            String str = node.getString();

            if(str.startsWith("_")) {
                Node newNode = Node.newString(node.getType(), str.substring(1));
                
                for(Node child = node.getFirstChild(); child != null; child = child.getNext())
                    newNode.addChildToBack(child);
                
                node = newNode;
            }
        }
        
        for(Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            Node newChild = postprocess(child);
            if(newChild != child)
                node.replaceChild(child, newChild);
            child = newChild;
        }
        
        return node;
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
            throw new IllegalStateException("Expression is not a literal [" + expression + "]");
        
        return resolve(null, null);
    }
    
    
    public Object resolve(Scope scope, Context ctx) {

        Object obj = UNDEFINED_VALUE;
        try {
            obj = resolve(scope, ctx, parsedExpression.getFirstChild(), true);
        } finally {
            if(obj == UNDEFINED_VALUE)
                log.debug("Failed to resolve [" + this +  "]. (" + token.getOrigin() + ":" + token.getStartLine() + ")");
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
                throw new TemplateException("Can't handle native objects of type [" + temp.getClass().getName() + "]");
            JSObject obj = (JSObject)temp;
            
            //get the property
            Object prop = resolve(scope, ctx, node.getLastChild(), true);
            if(prop == null || prop == UNDEFINED_VALUE)
                return UNDEFINED_VALUE;
            
            Object val = obj.get(prop);
            if(val == null)
                val = obj.containsKey(prop.toString()) ? null : UNDEFINED_VALUE;
            
            if (autoCall 
                    && (val instanceof JSFunction)
                    && ( 
                        !(val instanceof JSPyObjectWrapper) 
                        || ((JSPyObjectWrapper)val).isCallable()
                        )
                    ) {

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
                    throw new TemplateException("Can only call functions.  [" + expression + "]");
                callMethodObj = (JSFunction)temp;
                
                //get this
                callThisObj = (JSObject)resolve(scope, ctx, callArgs.getFirstChild(), true);;
            } else {
                temp = resolve(scope, ctx, callArgs, false);
                if(!(temp instanceof JSFunction))
                    throw new TemplateException("Can only call functions. [" + expression + "]");
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
            
            if (autoCall 
                && (lookupValue instanceof JSFunction)
                && !(lookupValue instanceof JSFileLibrary)
                && ( 
                    !(lookupValue instanceof JSPyObjectWrapper)
                    || ((JSPyObjectWrapper)lookupValue).isCallable()
                    )
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
    
    
    //Functors
    private static class resolveFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object contextObj, Object[] extra) {
            Expression thisObj = (Expression)scope.getThis();
            return thisObj.resolve(scope, (Context)contextObj);
        }
    }
    private static class isTrueFunc extends JSFunctionCalls1 {
        public Object call(Scope scope, Object p0, Object[] extra) {
            return isTrue(p0);
        }
    }
    private static class toStringFunc extends JSFunctionCalls0 {
        public Object call(Scope scope, Object[] extra) {
            Expression thisObj = (Expression)scope.getThis();
            return thisObj.toString();
        }
    }
    
    
    //Constructors
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls1() {
        public Object call(Scope scope, Object expressionObj, Object[] extra) {
            throw new UnsupportedOperationException();
        }
        protected void init() {
            set("UNDEFINED_VALUE", UNDEFINED_VALUE);
            set("is_true", new isTrueFunc());
            set("resolve", new resolveFunc());
            set("toString", new toStringFunc());
        }
    };
}
