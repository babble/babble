/**
 * 
 */
package ed.appserver.templates.djang10;

import ed.appserver.templates.Djang10Converter.MyGenerator;

public class EndForLoopTag extends TagHandler {
	@Override
	public void Compile(MyGenerator g, String name, String... params) {
		
		g.appendVarExpansion("forloop", "null");
		g.append(".moveNext();\n");

		g.append("}\n");
		g.appendPopContext();
	}
}