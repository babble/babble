package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Variable;



public class DefaultFilter implements Filter {

	public Object apply(Object value, Object param) {
		return (value == Variable.UNDEFINED_VALUE || value == null)? param : value;
	}

}
