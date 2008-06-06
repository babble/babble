package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class FirstOfTagHandler implements TagHandler {

    public Node compile(Parser parser, String command, Token token) throws TemplateException {
        String tokenContents = token.contents.replaceFirst("\\S+\\s*", ""); // remove
                                                                            // the
                                                                            // tag
                                                                            // name

        String[] parts = Parser.smartSplit(tokenContents, " ");
        Expression[] variables = new Expression[parts.length];
        for (int i = 0; i < parts.length; i++)
            variables[i] = new Expression(parts[i]);

        return new FirstOfNode(token, variables);
    }

    public Map<String, JSFunction> getHelpers() {
        // TODO Auto-generated method stub
        return new HashMap<String, JSFunction>();
    }

    private static class FirstOfNode extends Node {
        private Expression[] variables;

        public FirstOfNode(Token token, Expression[] variables) {
            super(token);
            this.variables = variables;
        }

        @Override
        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.append("print(");

            for (Expression var : variables) {
                buffer.appendHelper(startLine, Expression.IS_TRUE + "(" + var.toJavascript() + ")?" + var.toJavascript() + ":");
            }
            buffer.append("\"\"");
            buffer.append(");\n");

        }
    }
}
