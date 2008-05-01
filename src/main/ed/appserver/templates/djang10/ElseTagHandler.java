/**
 * 
 */
package ed.appserver.templates.djang10;

import ed.appserver.templates.Djang10Converter.MyGenerator;


public class ElseTagHandler extends TagHandler {
	public void Compile(MyGenerator g, String name, String... params) {
		g.append("} else {\n");
	}
}