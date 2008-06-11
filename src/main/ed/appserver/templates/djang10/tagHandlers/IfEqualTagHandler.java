package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IfEqualTagHandler implements TagHandler {
    private boolean inverted;

    public IfEqualTagHandler(boolean inverted) {
        this.inverted = inverted;
    }

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {
        String[] parts = token.getContents().split("\\s");
        if (parts.length != 3)
            throw new TemplateException("Expected 2 arguments");

        List<Node> trueNodes = parser.parse("else", "end" + command);
        List<Node> falseNodes;

        Token elseToken = parser.nextToken();
        Token endToken;

        if ("else".equals(elseToken.getContents())) {
            falseNodes = parser.parse("end" + command);
            endToken = parser.nextToken();
        } else {
            endToken = elseToken;
            elseToken = null;

            falseNodes = new LinkedList<Node>();
        }

        Expression var1 = new Expression(parts[1]);
        Expression var2 = new Expression(parts[2]);

        return new IfEqualNode(token, elseToken, endToken, var1, var2, trueNodes, falseNodes, inverted);
    }

    public Map<String, JSFunction> getHelpers() {
        return new HashMap<String, JSFunction>();
    }

    private static class IfEqualNode extends TagNode {
        private final Token elseToken, endToken;
        private final Expression varName1, varName2;
        private final List<Node> trueNodes, falseNodes;
        private final boolean inverted;

        public IfEqualNode(Token token, Token elseToken, Token endToken, Expression varName1, Expression varName2,
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
        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
            // TODO Auto-generated method stub

            buffer.append(startLine, "if(");

            buffer.append(varName1.toJavascript());
            buffer.append(startLine, inverted ? " != " : " == ");
            buffer.append(startLine, varName2.toJavascript());

            buffer.append(startLine, ") {\n");

            for (Node node : trueNodes)
                node.toJavascript(preamble, buffer);

            if (elseToken != null) {
                buffer.append(elseToken.getStartLine(), "} else {\n");

                for (Node node : falseNodes) {
                    node.toJavascript(preamble, buffer);
                }
            }
            buffer.append(endToken.getStartLine(), "}\n");
        }

    }
}
