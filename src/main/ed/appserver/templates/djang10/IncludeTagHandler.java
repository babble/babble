package ed.appserver.templates.djang10;

import java.util.StringTokenizer;

import ed.appserver.templates.Djang10Converter.MyGenerator;

public class IncludeTagHandler extends TagHandler {
	@Override
	public void Compile(MyGenerator g, String name, String... params) {
		StringBuilder buffer = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(params[0], "/");
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(token.length() > 0) {
				if(buffer.length() > 0) {
					buffer.append("[\"");
					buffer.append(token);
					buffer.append("\"]");
				}
				else
					buffer.append(token);
			}
		}
		buffer.append("(");
		buffer.append(MyGenerator.CONTEXT_STACK_VAR);
		buffer.append(");\n");
		g.append(buffer.toString());
	}

}
