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
