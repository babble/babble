package ed.appserver.templates.djang10;

import ed.js.JSException;

public class Djang10Exception extends JSException {
    public Djang10Exception(Object o, Throwable t, boolean wantedJSException) {
        super(o, t, wantedJSException);
    }

    public Djang10Exception(Object o, Throwable t) {
        super(o, t);
    }

    public Djang10Exception(Object o) {
        super(o);
    }
}
