package ed.appserver.templates.djang10.filters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlEncodeFilter implements Filter {

	public Object apply(boolean wasFound, Object value, String param) {
		if(!wasFound || value == null)
			return null;
		
		try {
			return URLEncoder.encode(value.toString(), "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
