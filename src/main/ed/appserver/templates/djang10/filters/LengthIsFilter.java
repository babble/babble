package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Parser;
import ed.js.JSArray;

public class LengthIsFilter implements Filter {

	public Object apply(boolean wasFound, Object value, String param) {
		if(!wasFound || value==null)
			return null;
		
		JSArray array = (JSArray)value;
		int length = Integer.parseInt(Parser.dequote(param));
		
		return array.size() == length;
	}

}
