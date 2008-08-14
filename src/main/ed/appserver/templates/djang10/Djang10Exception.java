package ed.appserver.templates.djang10;

import ed.js.JSException;

public class Djang10Exception extends RuntimeException {
    public Djang10Exception(String msg) {
        super(msg);
    }
    public Djang10Exception(String msg, Throwable t) {
        super(msg, t);
    }
}
