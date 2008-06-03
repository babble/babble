package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;

public class EscapeFilter implements Filter {

    public Object apply(Object value, Object param) {
        
        if ( value == VariableTagHandler.UNDEFINED_VALUE || value==null)
            return null;
        
        return ed.js.Encoding._escapeHTML( value.toString() );
    }

}
