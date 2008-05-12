package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;
import ed.js.PrintBuffer;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;

public class FilterTagHandler implements TagHandler {
 
	public Node compile(Parser parser, String command, Token token) {
		List<Node> nodyNodes = parser.parse("end" + command);
		parser.nextToken();
		String filterParams = token.contents.split("\\s", 2)[1].trim();
		
		return new FilterNode(token, filterParams, nodyNodes);
	}

	public Map<String, JSFunction> getHelpers() {
		Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
		
		helpers.put("newPrintBuffer", new JSFunctionCalls0() {
			@Override
			public Object call(Scope scope, Object[] extra) {
				return new PrintBuffer();
			}
		});
		return helpers;
	}
	
	private static class FilterNode extends Node {
		private final List<Node> bodyNodes;
		private final String filterParams;
		
		public FilterNode(Token token, String filterParams, List<Node> bodyNodes) {
			super(token);
			
			this.filterParams = filterParams;
			this.bodyNodes = bodyNodes;
		}
		
		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) {
			buffer.append(startLine, "print(");
				buffer.appendHelper(startLine, JSWriter.VAR_EXPAND + "(");
					buffer.append(startLine, "(function(print) {\n");
				
						for(Node node : bodyNodes)
							node.getRenderJSFn(preamble, buffer);
				
						buffer.append(startLine, "return \"\\\"\" + print.toString() + \"\\\"\";\n");
						
						buffer.append(startLine, "})(" + JSWriter.NS + ".newPrintBuffer() )");
					
					buffer.append(startLine, "+\"|"+filterParams + "\"");
				buffer.append(startLine, ")");
			buffer.append(startLine, ");\n");
			
		}
	}
}
