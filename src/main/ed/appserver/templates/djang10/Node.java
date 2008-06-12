package ed.appserver.templates.djang10;

import java.util.StringTokenizer;

import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;

public abstract class Node {
    public final int startLine;
    public final Token token;

    protected Node(Token token) {
        this.startLine = token.getStartLine();
        this.token = token;
    }

    public abstract void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException;

    public static class TextNode extends Node {
        private final String text;

        public TextNode(Token token) {
            super(token);

            this.text = token.getContents();
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {

            String escaped = text.replace("\"", "\\\"");

            StringTokenizer tokenizer = new StringTokenizer(escaped, "\n");

            int line = startLine;
            boolean endsWithNewline = text.endsWith("\n");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                buffer.append(line, "print(\"");
                buffer.append(line, token);
                if (endsWithNewline || tokenizer.hasMoreTokens())
                    buffer.append(line, "\\n");
                buffer.append(line, "\");\n");

                line++;
            }
        }
    }

    public static class VariableNode extends Node {
        private final FilterExpression expression;

        public VariableNode(Token token, FilterExpression expression) {
            super(token);

            this.expression = expression;
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.append(startLine, "print(" + JSHelper.NS + "." + Expression.DEFAULT_VALUE + "(" + expression.toJavascript()
                    + ", " + "''));\n");
        }
    }
    
    public static abstract class TagNode extends Node {
        public final String tagName;

        public TagNode(Token token) {
            super(token);
            this.tagName = token.getContents().trim().split("\\s")[0];
        }
        
        
    }
}
