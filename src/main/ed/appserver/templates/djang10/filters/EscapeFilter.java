package ed.appserver.templates.djang10.filters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ed.appserver.templates.djang10.Variable;

public class EscapeFilter implements Filter {

    public Object apply(Object value, Object param) {
        
        if ( value == Variable.UNDEFINED_VALUE || value==null)
            return null;
        
        return ed.js.Encoding._escapeHTML( value.toString() );
    }

}
