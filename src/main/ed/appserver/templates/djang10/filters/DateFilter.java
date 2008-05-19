package ed.appserver.templates.djang10.filters;

import java.util.Date;

import ed.appserver.templates.djang10.Util;
import ed.js.JSDate;

public class DateFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == null)
			return null;
		
		Date date = new Date( ((JSDate)value).getTime() );

		return Util.formatDate(date, param.toString());
	}

}
