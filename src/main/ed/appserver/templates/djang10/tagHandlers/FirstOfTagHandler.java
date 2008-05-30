package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class FirstOfTagHandler implements TagHandler {

	public Node compile(Parser parser, String command, Token token) throws TemplateException {
		String tokenContents = token.contents.replaceFirst("\\S+\\s*", "");		//remove the tag name
		
		String[] parts = Parser.smartSplit(tokenContents, " ");

		return new FirstOfNode(token, parts);
	}

	public Map<String, JSFunction> getHelpers() {
		// TODO Auto-generated method stub
		return new HashMap<String, JSFunction>();
	}

	private static class FirstOfNode extends Node {
		private String[] variables;
		
		
		public FirstOfNode(Token token, String[] variables) {
			super(token);
			this.variables = variables;
		}


		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
			buffer.append("print(");
			
			boolean isFirst = true;
			for(String var : variables) {
				if(!isFirst)
					buffer.append(" || ");
				isFirst = false;
				
				buffer.append(startLine, VariableTagHandler.compileFilterExpression(var, "\"\""));
			}
			
			buffer.append(");\n");
			
		}
	}
}
