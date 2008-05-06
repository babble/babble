package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IfEqualTagHandler implements TagHandler {
	private boolean inverted;
	
	
	public IfEqualTagHandler(boolean inverted) {
		super();
		this.inverted = inverted;
	}

	public Node compile(Parser parser, String command, Token token) {
		String[] parts = token.contents.split("\\s");
		if(parts.length != 3)
			throw new TemplateException("Expected 2 arguments");
		
		List<Node> trueNodes = parser.parse("else", "end"+command);
		List<Node> falseNodes;
		
		Token elseToken = parser.nextToken();
		Token endToken;
		
		if("else".equals(elseToken.contents)) {
			falseNodes = parser.parse("end"+command);
			endToken = parser.nextToken();
		} else {
			endToken = elseToken;
			elseToken = null;
			
			falseNodes = new LinkedList<Node>();
		}
		
		return new IfEqualNode(token, elseToken, endToken, parts[1], parts[2], trueNodes, falseNodes, inverted);
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>();
	}

	private static class IfEqualNode extends Node {
		private final Token elseToken, endToken;
		private final String varName1, varName2;
		private final List<Node> trueNodes, falseNodes;
		private final boolean inverted;
		
		public IfEqualNode(Token token, Token elseToken, Token endToken, String varName1, String varName2,
				List<Node> trueNodes, List<Node> falseNodes, boolean inverted) {
			super(token);
			this.elseToken = elseToken;
			this.endToken = endToken;
			
			this.varName1 = varName1;
			this.varName2 = varName2;
			this.trueNodes = trueNodes;
			this.falseNodes = falseNodes;
			this.inverted = inverted;
		}
		
		@Override
		public void getRenderJSFn(JSWriter buffer) {
			// TODO Auto-generated method stub
		
			buffer.append(startLine, "if(");
			if(varName1.matches("^\\s*\\\".*\\\"\\s*$"))
				buffer.append(startLine, varName1);
			else
				buffer.appendVarExpansion(startLine, varName1, "null");
			
			buffer.append(startLine, inverted? " != " : " == ");

			if(varName2.matches("^\\s*\\\".*\\\"\\s*$"))
				buffer.append(startLine, varName2);
			else
				buffer.appendVarExpansion(startLine, varName2, "null");
			
			buffer.append(startLine, ") {\n");
			
			for(Node node : trueNodes) {
				node.getRenderJSFn(buffer);
			}
			
			if(elseToken != null) {
				buffer.append(elseToken.startLine, "} else {\n");
				
				for(Node node : falseNodes) {
					node.getRenderJSFn(buffer);
				}
			}
			buffer.append(endToken.startLine, "}\n");
		}
		
	}
}
