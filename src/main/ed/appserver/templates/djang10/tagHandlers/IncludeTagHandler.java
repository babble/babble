package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IncludeTagHandler implements TagHandler {

	public Node compile(Parser parser, String command, Token token) throws TemplateException {
		String[] parts = Parser.smartSplit(token.contents);

		return new IncludeNode(token, parts[1]);
	}

	public Map<String, JSFunction> getHelpers() {
		Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
		
		return helpers;
	}
	
	private static class IncludeNode extends Node {
		private String varName;

		public IncludeNode(Token token, String varName) {
			super(token);

			this.varName = varName;
		}

		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
			buffer.appendHelper(startLine, JSHelper.CALL_PATH + "(");
			if(Parser.isQuoted(varName))
				buffer.append(startLine, varName);
			else
			    buffer.append(startLine, VariableTagHandler.compileFilterExpression(varName, "null"));
			buffer.append(startLine, ", " + JSWriter.CONTEXT_STACK_VAR + ");\n");
		}
	}
}
