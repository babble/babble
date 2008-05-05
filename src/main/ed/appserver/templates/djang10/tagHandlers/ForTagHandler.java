package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;

public class ForTagHandler implements TagHandler {

	public Node compile(Parser parser, String command, Token token) {
		String[] params = token.contents.split("\\s");
		String itemName = params[1];
		if(!"in".equals(params[2]))
			throw new TemplateException("expected in operator");
		String listName = params[3];
		boolean isReversed = params.length > 4? Boolean.parseBoolean(params[4]) : false;
		
		
		List<Node> bodyNodes = parser.parse("end" + command);
		parser.nextToken();
		
		return new ForNode(token, itemName, listName, isReversed, bodyNodes);
	}

	public Map<String, JSFunction> getHelpers() {
		Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
		
		helpers.put("newForLoopObjFn", new JSFunctionCalls2() {
			@Override
			public Object call(Scope scope, Object array, Object isReversedObj, Object[] extra) {
				
				JSArray contextStack = (JSArray)scope.get(JSWriter.CONTEXT_STACK_VAR);
				Object parentForLoop = null;
				for(int i=contextStack.size() - 1; i >= 0 && parentForLoop == null; i--)
					parentForLoop = ((JSObject)contextStack.get(i)).get("forloop");
				
				int length = (Integer)(((JSObject)array).get("length"));
				boolean isReversed = isReversedObj instanceof Boolean ? ((Boolean)isReversedObj) : false;
				
				JSObjectBase newContext = new JSObjectBase();
				ForLoopObj forLoopObj = new ForLoopObj(parentForLoop, length, isReversed);  
				newContext.set("forloop", forLoopObj);
				
				contextStack.add(newContext);

				return forLoopObj;
			}
		});
		
		return helpers;
	}
	
	private static class ForLoopObj extends JSObjectBase {
		private int i, length;
		private boolean isReversed;
		
		public ForLoopObj(Object parent, int length, boolean isReversed) {
			this.length = length;
			this.isReversed = isReversed;
			
			i = isReversed? length : -1;
			set("parent", parent);
			moveNext();
		}

		public void moveNext() {
			i += isReversed? -1 : 1;
			
			int counter0 = isReversed? length - i - 1 : i;
			int revcounter0 = isReversed? i : length - i - 1;
			
			set("i", i);
			set("counter0", counter0);
			set("counter", counter0 + 1);
			set("revcounter0", revcounter0);
			set("revcounter", revcounter0 + 1);
			set("first", counter0 == 0);
			set("last", revcounter0 == 0);
		}
	}
	
	private static class ForNode extends Node {
		private final String itemName;
		private final String listName;
		private final boolean isReversed;
		private List<Node> bodyNodes;
		
		public ForNode(Token token, String itemName, String listName, boolean isReversed, List<Node> bodyNodes) {
			super(token);
			
			this.itemName = itemName;
			this.listName = listName;
			this.isReversed = isReversed;
			this.bodyNodes = bodyNodes;
		}
		
		@Override
		public void getRenderJSFn(JSWriter buffer) {
			buffer.appendHelper(startLine, "newForLoopObjFn");
			buffer.append(startLine, "(");
			buffer.appendVarExpansion(startLine, listName, "[]");
			buffer.append(startLine, ",");
			buffer.append(startLine, "" + isReversed);
			buffer.append(startLine, ");\n");
			
			buffer.append(startLine, "while(");
			buffer.appendVarExpansion(startLine, "forloop.revcounter", "null");
			buffer.append(startLine, " > 0) {\n");
			
			buffer.appendCurrentContextVar(startLine, itemName);
			buffer.append(startLine, "=");
			buffer.appendVarExpansion(startLine, listName, "[]");
			buffer.append(startLine, "[");
			buffer.appendVarExpansion(startLine, "forloop.i", "null");
			buffer.append(startLine, "];\n");
			
			for(Node node : bodyNodes) {
				node.getRenderJSFn(buffer);
			}
			
			buffer.appendVarExpansion(startLine, "forloop", "null");
			buffer.append(startLine, ".moveNext();\n");

			buffer.append(startLine, "}\n");
			buffer.appendPopContext(startLine);

		}
	}
}
