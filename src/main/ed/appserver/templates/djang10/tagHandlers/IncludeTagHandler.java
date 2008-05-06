package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IncludeTagHandler implements TagHandler {

	public Node compile(Parser parser, String command, Token token) {
		String[] parts = Parser.smartSplit(token.contents);

		return new IncludeNode(token, Parser.dequote(parts[1]));
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>();
	}

	private static class IncludeNode extends Node {
		private String path;

		public IncludeNode(Token token, String path) {
			super(token);

			this.path = path;
		}

		@Override
		public void getRenderJSFn(JSWriter buffer) {
			StringTokenizer tokenizer = new StringTokenizer(path, "/");

			boolean isFirst = true;
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.length() > 0) {
					if (!isFirst) {
						buffer.append("[\"");
						buffer.append(token);
						buffer.append("\"]");
					} else
						buffer.append(token);
				}
				isFirst = false;
			}
			buffer.append("(");
			buffer.append(JSWriter.CONTEXT_STACK_VAR);
			buffer.append(");\n");

		}
	}
}
