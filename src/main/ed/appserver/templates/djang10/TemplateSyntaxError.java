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
