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

public class CallTagHandler implements TagHandler {
	public static final String EVAL = "eval";
	public Node compile(Parser parser, String command, Token token) throws TemplateException {
		Pattern pattern = Pattern.compile("^\\s*\\S+\\s+(.+?)(?:\\s+(allowGlobal))?\\s*$");
		Matcher matcher = pattern.matcher(token.contents);
		if(!matcher.find())
			throw new TemplateException("Invlaid syntax");
		
		String methodCall = matcher.group(1);
		boolean allowGlobal = matcher.group(2) != null;
		
		String compiledExpr = VariableTagHandler.compileExpression(methodCall);
		
		return new CallNode(token, compiledExpr);
	}

	public Map<String, JSFunction> getHelpers() {
		return new HashMap<String, JSFunction>();
	}

	private static class CallNode extends Node {
		private final String compiledExpr;
		
		public CallNode(Token token, String compiledExpr) {
			super(token);
			this.compiledExpr = compiledExpr;
		}
		
		@Override
		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
			buffer.append(startLine, compiledExpr + ";\n");
		}
	}
}
