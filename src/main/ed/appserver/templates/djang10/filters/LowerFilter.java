package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Variable;


public class LowerFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == Variable.UNDEFINED_VALUE || value==null)
			return null;
		
		return value.toString().toLowerCase();
	}

}
