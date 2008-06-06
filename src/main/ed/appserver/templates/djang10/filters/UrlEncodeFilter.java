package ed.appserver.templates.djang10.filters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ed.appserver.templates.djang10.Expression;

public class UrlEncodeFilter extends JavaFilter {

    public Object apply(Object value, Object param) {
        if (value == Expression.UNDEFINED_VALUE || value == null)
            return null;

        try {
            return URLEncoder.encode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
