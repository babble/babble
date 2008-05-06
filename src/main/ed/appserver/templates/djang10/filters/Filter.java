package ed.appserver.templates.djang10.filters;

public interface Filter {
	public Object apply(boolean wasFound, Object value, String param);
}
