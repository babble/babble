package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Expression;

public class DefaultFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        return (value == Expression.UNDEFINED_VALUE || value == null) ? param : value;
    }

}
