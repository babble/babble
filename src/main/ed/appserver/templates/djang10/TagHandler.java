/**
 * 
 */
package ed.appserver.templates.djang10;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.Djang10Converter.MyGenerator;

public abstract class TagHandler {
	
	public abstract void Compile(MyGenerator g, String name,  String... params);
	public Map<String, Object> getHelpers() {
		return new HashMap<String, Object>();
	}
}