package ed.appserver.templates.djang10;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls4;

public class VariableDoesNotExist extends Djang10Exception {
    private Expression expression;
    private String msg;
    private String subExpression;
    
    private VariableDoesNotExist() { }
    
    public VariableDoesNotExist(String msg, Expression expression, String subExpression) {
        init(msg, expression, subExpression, null);
    }
    public VariableDoesNotExist(String msg, Expression expression, String subExpression, Throwable cause) {
        init(msg, expression, subExpression, cause);
    }

    protected void init(String msg, Expression expression, String subExpression, Throwable cause ) {
        this.expression = expression;
        this.subExpression = subExpression;
        
        Token t = expression.getToken();
        super.init(msg + " [" + expression + "] (" +t.getOrigin() + ":" + t.getStartLine() + ")", cause);
    }
    
    public JSFunction getConstructor() {
        return cons;
    }
    
    public static final Constructor cons = new Constructor(Djang10Exception.cons);

    public static class Constructor extends JSFunctionCalls4 {
        public Constructor(JSFunction superConstructor) {
            getPrototype().set("__proto__", superConstructor.getPrototype());
        }
        public JSObject newOne() {
            return new VariableDoesNotExist();
        }
        public Object call(Scope scope, Object messageObj, Object expressionObj, Object subExpressionObj, Object causeObj, Object[] extra) {
            VariableDoesNotExist thisObj = (VariableDoesNotExist)scope.getThis();
            
            thisObj.init(messageObj.toString(), (Expression)expressionObj, subExpressionObj.toString(), (Throwable)causeObj);

            return null;
        }
    }
    
    public static class VariableDoesNotExistButIsNullInJS extends VariableDoesNotExist {
        public VariableDoesNotExistButIsNullInJS(String msg, Expression expression, String subExpression, Throwable cause) {
            super(msg, expression, subExpression, cause);
        }

        public VariableDoesNotExistButIsNullInJS(String msg, Expression expression, String subExpression) {
            super(msg, expression, subExpression);
        }
        
    }
}
