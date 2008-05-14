package ed.appserver.templates.djang10.filters;

public class UpperFilter implements Filter {

	public Object apply(boolean wasFound, Object value, String param) {
		if(!wasFound || value == null)
			return null;
		
		return value.toString().toUpperCase();
	}

}
