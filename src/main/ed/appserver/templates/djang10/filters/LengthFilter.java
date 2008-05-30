package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;
import ed.js.JSArray;

public class LengthFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == VariableTagHandler.UNDEFINED_VALUE || value==null)
			return null;
		
		return ((JSArray)value).size();
	}

}
