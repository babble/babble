package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Expression;

public class UpperFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        if (value == Expression.UNDEFINED_VALUE || value == null)
            return null;

        return value.toString().toUpperCase();
    }

}
