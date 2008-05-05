package ed.appserver.templates.djang10;

import java.util.StringTokenizer;

import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;

public abstract class Node {
	public final int startLine, endLine;
	
	protected Node(Token token) {
		this.startLine = token.startLine;
		this.endLine = token.endLine;
	}
		
	public abstract void getRenderJSFn(JSWriter buffer);
	
	
	public static class TextNode extends Node {
		private final String text;
		
		public TextNode(Token token) {
			super(token);
			
			this.text = token.contents;
		}

		public void getRenderJSFn(JSWriter buffer) {
			
			String escaped = text.replace("\"", "\\\"");
			
			StringTokenizer tokenizer = new StringTokenizer(escaped,"\n");
			
			int line = startLine;
			boolean endsWithNewline = text.endsWith("\n");
			
			while(tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				
				buffer.append(line, "print(\"");
				buffer.append(line, token);
				if(endsWithNewline || tokenizer.hasMoreTokens())
					buffer.append(line, "\\n");
				buffer.append(line, "\");\n");
				
				line ++;
			}
		}
	}
	
	public static class VariableNode extends Node {
		private final String name;
		
		public VariableNode(Token token) {
			super(token);
			
			this.name = token.contents;
		}

		public void getRenderJSFn(JSWriter buffer) {
			buffer.append(startLine, "print(");
			buffer.appendVarExpansion(startLine, name, "\"\"");
			buffer.append(startLine, ");\n");
		}
	}
}
