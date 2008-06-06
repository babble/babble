package ed.appserver.templates.djang10.filters;

import java.util.Date;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.Util;
import ed.js.JSDate;

public class DateFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        if (value == null || value == Expression.UNDEFINED_VALUE)
            return Expression.UNDEFINED_VALUE;

        Date date = new Date(((JSDate) value).getTime());

        return Util.formatDate(date, param.toString());
    }

}
