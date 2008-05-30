package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;
import ed.js.PrintBuffer;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;

public class FilterTagHandler implements TagHandler {
 
	public Node compile(Parser parser, String command, Token token) throws TemplateException {
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
		
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
			String[] filterParamParts = Parser.smartSplit(filterParams, "|");
			
			buffer.append(startLine, "print(");
				buffer.append(startLine, JSHelper.NS + "." + VariableTagHandler.DEFAULT_VAR + "(");
				
					for(int i=filterParamParts.length-1; i>=0; i--) {
						buffer.append(startLine, JSHelper.NS + "." + VariableTagHandler.APPLY_FILTER + "(");
					}
	
						buffer.append(startLine, "(function(print) {\n");
					
							for(Node node : bodyNodes)
								node.getRenderJSFn(preamble, buffer);
					
							buffer.append(startLine, "return print.toString();\n");
							
						buffer.append(startLine, "})(" + JSHelper.NS + ".newPrintBuffer() )");
					
					for(int i=0; i < filterParamParts.length; i++) {
						String[] filterParts = filterParamParts[i].split(":", 2);
						String filterName = filterParts[0].trim();
						String filterParam;

						if(filterParts.length > 1)
							filterParam = VariableTagHandler.compileFilterExpression(filterParts[1]);
						else
						    filterParam = "null";
						
						buffer.append(startLine, ", \"" + filterName + "\", " + filterParam + ")");
					}
				buffer.append(startLine, ", \"\")");
			buffer.append(startLine, ");\n");					
		}
	}
}
