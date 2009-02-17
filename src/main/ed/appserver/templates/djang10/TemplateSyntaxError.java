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
import ed.js.func.JSFunctionCalls3;

public class TemplateSyntaxError extends Djang10Exception {
    private Token token;

    private TemplateSyntaxError() { }

    public TemplateSyntaxError(String message, Token token) {
        init(message, token, null);
    }

    public TemplateSyntaxError(String message, Token token, Throwable cause) {
        init(message, token, cause);

        this.token = token;
    }

    protected void init(String message, Token token, Throwable cause) {
        super.init(message + " (" + token.getOrigin() + ":"+token.getStartLine()+")", cause);
        this.token = token;
    }

    public JSFunction getConstructor() {
        return cons;
    }

    public Token getToken() {
        return token;
    }

    public static final Constructor cons = new Constructor(Djang10Exception.cons);

    public static class Constructor extends JSFunctionCalls3 {
        public Constructor(JSFunction superConstructor) {
            getPrototype().set("__proto__", superConstructor.getPrototype());
        }
        public JSObject newOne() {
            return new TemplateSyntaxError();
        }
        public Object call(Scope scope, Object messageObj, Object tokenObj,  Object causeObj, Object[] extra) {
            TemplateSyntaxError thisObj = (TemplateSyntaxError)scope.getThis();

            thisObj.init(((messageObj == null)? null : messageObj.toString()), (Token)tokenObj, (Throwable)causeObj);


            return null;
        }
    }
}
