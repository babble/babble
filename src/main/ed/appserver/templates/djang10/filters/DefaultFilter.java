package ed.appserver.templates.djang10.filters;



public class DefaultFilter implements Filter {

	public Object apply(boolean wasFound, Object value, String param) {
		return (!wasFound || value == null)? param : value;
	}

}
