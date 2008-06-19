package ed.appserver.templates.djang10;

import ed.js.JSException;

public class TemplateException extends JSException {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable t) {
        super(message, t);
    }

    public TemplateException(int startLine, String message) {
        super(message + ". On Line: " + startLine);
    }
}
