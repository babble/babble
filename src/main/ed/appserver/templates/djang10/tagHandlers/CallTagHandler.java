package ed.appserver.templates.djang10.tagHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class CallTagHandler implements TagHandler {
	public static final String EVAL = "eval";
	public Node compile(Parser parser, String command, Token token) {
		Pattern pattern = Pattern.compile("^\\s*\\S+\\s+(\\S+)\\((.*)\\)\\s+(allowGlobal)?\\s*$");
		Matcher matcher = pattern.matcher(token.contents);
		if(!matcher.find())
			throw new TemplateException("Invlaid syntax");
		
		String functionName = matcher.group(1);
		String paramListStr = matcher.group(2);
		boolean allowGlobal = matcher.group(3) != null;
		
		String[] params = Parser.smartSplit(paramListStr, ",");
		ArrayList<String> temp = new ArrayList<String>();
		
		for(String param : params) {
			if(param.trim().length() > 0)
				temp.add(param.trim());
		}
		params = temp.toArray(new String[0]);
		
		return new CallNode(token, functionName, params, allowGlobal);
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>();
	}

	private static class CallNode extends Node {
		private final String functionName;
		private final String[] params;
		private final boolean allowGlobal;
		
		public CallNode(Token token, String functionName, String[] params, boolean allowGlobal) {
			super(token);
			this.functionName = functionName;
			this.params = params;
			this.allowGlobal = true;
		}
		
		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) {
			buffer.appendVarExpansion(startLine, functionName, "null", allowGlobal, false);
			buffer.append(startLine, "(");
			
			boolean isFirst = true;
			for(String param : params) {
				if(!isFirst)
					buffer.append(startLine, ", ");
				isFirst = false;
				
				buffer.appendVarExpansion(startLine, param, "null", allowGlobal, true);
			}
			
			buffer.append(startLine, ");\n");
		}
	}
}
