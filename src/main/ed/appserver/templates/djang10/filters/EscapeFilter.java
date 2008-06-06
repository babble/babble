package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Expression;

public class EscapeFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        if (value == Expression.UNDEFINED_VALUE || value == null)
            return null;

        return ed.js.Encoding._escapeHTML(value.toString());
    }

}
