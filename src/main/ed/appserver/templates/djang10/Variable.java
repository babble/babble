package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;


public class Variable {
	public static final Object UNDEFINED_VALUE = new Object() {
		public String toString() { return null; };
	};

	public String base;
	public List<FilterSpec> filters;
	
	public Variable() {
		base = null;
		filters = new ArrayList<FilterSpec>();
	}

	
	public static class FilterSpec {
		public final String name;
		public final String param;
		
		public FilterSpec(String name, String param) {
			super();
			this.name = name;
			this.param = param;
		}
	}
}
