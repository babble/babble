package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Variable;
import ed.js.JSArray;

public class LengthFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == null || value == Variable.UNDEFINED_VALUE)
			return null;
		
		return ((JSArray)value).size();
	}

}
