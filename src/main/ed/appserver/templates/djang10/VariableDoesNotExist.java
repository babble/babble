/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

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
    private boolean sameAsJsNull = false;

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

        super.init(format(msg, expression, subExpression), cause);
    }

    public boolean isSameAsJsNull() {
        return sameAsJsNull;
    }
    public void setSameAsJsNull(boolean sameAsJsNull) {
        this.sameAsJsNull = sameAsJsNull;
    }

    public static String format(String msg, Expression expression, String subExpression) {
        Token t = expression.getToken();
        return msg + ". [" + subExpression + "] in [" + expression + "] in (" +t.getOrigin() + ":" + t.getStartLine() + ")";
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
}
