package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.Variable;
import ed.js.JSArray;

public class LengthIsFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == Variable.UNDEFINED_VALUE || value==null)
			return null;
		
		JSArray array = (JSArray)value;
		int length = Integer.parseInt(Parser.dequote(param.toString()));
		
		return array.size() == length;
	}

}