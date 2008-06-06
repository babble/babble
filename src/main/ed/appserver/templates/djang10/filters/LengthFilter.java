package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Expression;
import ed.js.JSArray;

public class LengthFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        if (value == Expression.UNDEFINED_VALUE || value == null)
            return null;

        return ((JSArray) value).size();
    }

}
