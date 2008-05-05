package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class BlockTagHandler implements TagHandler {
	public static final String RENDER_OPT_BLOCK_MAP = "blocks";
	
	public Node compile(Parser parser, String command, Token token) {
		String[] parts = token.contents.split("\\s");
		
		String blockName = parts[1];
		
		
		boolean isChildTemplate = (parser.getStateVariable(ExtendsTagHandler.class) != null);
		
		Map<String, BlockNode> blockMap = parser.getStateVariable(this.getClass());
		boolean isFirstNode = (blockMap == null);
		
		if(isFirstNode) {
			blockMap = new HashMap<String, BlockNode>();
			parser.setStateVariable(this.getClass(), blockMap);
		}
		
		if(blockMap.containsKey(blockName))
			throw new TemplateException("Block names have to be unique to templates");
		
		List<Node> bodyNodes = parser.parse("end" + command);
		parser.nextToken();
		
		BlockNode node = new BlockNode(token, blockName, bodyNodes, isChildTemplate, isFirstNode);
		blockMap.put(blockName, node);
		
		return node;
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>();
	}
	
	public static class BlockNode extends Node {
		private final String name;
		private final List<Node> bodyNodes;
		private final boolean isChildTemplate;
		private final boolean isFirstNode;
		
		public BlockNode(Token token, String name, List<Node> bodyNodes, boolean isChildTemplate, boolean isFirstNode) {
			super(token);
			
			this.name = name;
			this.bodyNodes = bodyNodes;
			this.isChildTemplate = isChildTemplate;
			this.isFirstNode = isFirstNode;
		}
		
		@Override
		public void getRenderJSFn(JSWriter buffer) {
			if(isFirstNode) {
				buffer.append(startLine, "if(" + JSWriter.RENDER_OPTIONS_VAR + "."+RENDER_OPT_BLOCK_MAP+" == null){\n");
				buffer.append(startLine, JSWriter.RENDER_OPTIONS_VAR + "."+RENDER_OPT_BLOCK_MAP+" = {};\n");
				buffer.append(startLine, "}\n");
			}
			
			buffer.append(startLine, "if(!"+JSWriter.RENDER_OPTIONS_VAR + "[\"" + name + "\"]) {\n");
			
				buffer.append(startLine, JSWriter.RENDER_OPTIONS_VAR + "[\"" + name + "\"] = function() {\n");

					for(Node node : bodyNodes) {
						node.getRenderJSFn(buffer);
					}
					
				
				buffer.append(startLine, "};\n");

			buffer.append(startLine, "}\n");
			
			if(!isChildTemplate)
				buffer.append(startLine, JSWriter.RENDER_OPTIONS_VAR + "[\"" + name + "\"]();\n");
				
		}
	}
}
