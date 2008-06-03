package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;




public class DefaultFilter implements Filter {

	public Object apply(Object value, Object param) {
		return (value == VariableTagHandler.UNDEFINED_VALUE || value == null)? param : value;
	}

}
