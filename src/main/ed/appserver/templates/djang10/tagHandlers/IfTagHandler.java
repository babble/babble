package ed.appserver.templates.djang10.tagHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.FilterExpression;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IfTagHandler implements TagHandler {

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {

        String tokenContents = token.getContents().replaceAll("\\s+", " "); // remove
                                                                        // extra
                                                                        // whitespace
        tokenContents = tokenContents.replaceFirst("\\S+\\s*", ""); // remove
                                                                    // the tag
                                                                    // name

        String[] boolPairs = tokenContents.split(" and ");
        boolean isAnd;

        if (boolPairs.length <= 1) {
            boolPairs = tokenContents.split(" or ");
            isAnd = false;
        } else {
            if (tokenContents.contains(" or "))
                throw new TemplateException(token.getStartLine(), "Can't mix and & or in if statements");
            isAnd = true;
        }

        ArrayList<IfExpr> ifParams = new ArrayList<IfExpr>();
        for (String boolPair : boolPairs) {
            String[] boolPairParts = boolPair.split(" ", 2);

            if (boolPairParts.length > 1) {
                if (!"not".equals(boolPairParts[0]))
                    throw new TemplateException(token.getStartLine(), "if statement improperly formatted");

                ifParams.add(new IfExpr(true, new FilterExpression(parser, boolPairParts[1])));
            }
            ifParams.add(new IfExpr(false, new FilterExpression(parser, boolPairParts[0])));
        }

        LinkedList<Node> trueNodes = parser.parse("else", "endif");
        LinkedList<Node> falseNodes;

        Token elseToken = parser.nextToken();
        Token endifToken = null;

        if ("else".equals(elseToken.getContents())) {
            falseNodes = parser.parse("endif");
            endifToken = parser.nextToken();
        } else {
            endifToken = elseToken;
            elseToken = null;

            falseNodes = new LinkedList<Node>();
        }

        return new IfNode(token, elseToken, endifToken, isAnd, ifParams, trueNodes, falseNodes);
    }

    public Map<String, JSFunction> getHelpers() {
        return new HashMap<String, JSFunction>();
    }

    private class IfExpr {
        public final FilterExpression varName;
        public final boolean inverted;

        public IfExpr(boolean inverted, FilterExpression varName) {
            super();
            this.varName = varName;
            this.inverted = inverted;
        }

    }

    private class IfNode extends TagNode {
        private final boolean isAnd;
        private final List<IfExpr> ifParams;
        private final List<Node> trueNodes, falseNodes;
        private final int elseStartLine;
        private final int endIfStartLine;

        public IfNode(Token token, Token elseToken, Token endToken, boolean isAnd, List<IfExpr> ifParams, List<Node> trueNodes,
                List<Node> falseNodes) {
            super(token);

            this.isAnd = isAnd;
            this.ifParams = ifParams;
            this.trueNodes = trueNodes;
            this.falseNodes = falseNodes;

            if (elseToken != null) {
                elseStartLine = elseToken.getStartLine();
            } else {
                elseStartLine = 0;
            }
            endIfStartLine = endToken.getStartLine();
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.append(startLine, "if(");

            boolean isFirst = true;
            for (IfExpr ifParam : ifParams) {
                if (!isFirst) {
                    buffer.append(startLine, isAnd ? " && " : " || ");
                }
                isFirst = false;

                if (ifParam.inverted)
                    buffer.append(startLine, "!");

                buffer.appendHelper(startLine, Expression.IS_TRUE + "(" + ifParam.varName.toJavascript() + ")");
            }

            buffer.append(startLine, ") {\n");

            for (Node node : trueNodes) {
                node.toJavascript(preamble, buffer);
            }

            buffer.append(elseStartLine, "} else {\n");

            for (Node node : falseNodes) {
                node.toJavascript(preamble, buffer);
            }

            buffer.append(endIfStartLine, "}\n");

        }
    }
}
