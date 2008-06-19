package ed.appserver.templates.djang10;

public class TemplateException extends RuntimeException {

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
