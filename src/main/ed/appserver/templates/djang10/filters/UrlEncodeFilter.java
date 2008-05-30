package ed.appserver.templates.djang10.filters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ed.appserver.templates.djang10.tagHandlers.VariableTagHandler;

public class UrlEncodeFilter implements Filter {

	public Object apply(Object value, Object param) {
		if(value == VariableTagHandler.UNDEFINED_VALUE || value==null)
			return null;
		
		try {
			return URLEncoder.encode(value.toString(), "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
