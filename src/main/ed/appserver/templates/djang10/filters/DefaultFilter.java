package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.UnresolvedValue;


public class DefaultFilter implements Filter {

	public Object apply(boolean wasFound, Object value, String param) {
		return (!wasFound || value == null)? new UnresolvedValue(param) : value;
	}

}
