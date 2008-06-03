package ed.appserver.templates.djang10.filters;

import java.util.Date;

import ed.appserver.templates.djang10.Util;
import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;
import ed.js.JSDate;

public class DateFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == null || value == VariableTagHandler.UNDEFINED_VALUE)
			return VariableTagHandler.UNDEFINED_VALUE;
		
		Date date = new Date( ((JSDate)value).getTime() );

		return Util.formatDate(date, param.toString());
	}

}
