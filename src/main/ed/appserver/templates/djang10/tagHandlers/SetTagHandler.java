package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class SetTagHandler implements TagHandler {

	public Node compile(Parser parser, String command, Token token) {
		Pattern regex = Pattern.compile("^\\s*\\S+\\s+(\\S*)\\s*=\\s*(.+?)\\s*$");
		Matcher matcher = regex.matcher(token.contents);
		
		if(!matcher.find())
			throw new TemplateException("Synatx Error on set tag");
		
		String varName = matcher.group(1);
		String varValue = matcher.group(2);
		
		return new SetNode(token, varName, varValue);
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>(); 
	}

	
	private static class SetNode extends Node {
		private final String varName;
		private final String varValue;
		
		public SetNode(Token token, String varName, String varValue) {
			super(token);
			this.varName = varName;
			this.varValue = varValue;
		}
		
		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) {
			buffer.appendCurrentContextVar(startLine, varName);
			buffer.append("=");
			buffer.appendVarExpansion(startLine, varValue, "null");
			buffer.append(";\n");
		}
	}
}
