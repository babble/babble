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
import ed.js.JSException;

public class TemplateSyntaxError extends Djang10Exception {
    private final Token token;
    
    public TemplateSyntaxError(String message, Token token) {
        super(message + " (" + token.getOrigin() + ":"+token.getStartLine()+")");
    
        this.token = token;
    }

    public TemplateSyntaxError(String message, Token token, Throwable cause) {
        super(message + " (" + token.getOrigin() + ":"+token.getStartLine()+")", cause);
        
        this.token = token;
    }
    
    public Token getToken() {
        return token;
    }
}
