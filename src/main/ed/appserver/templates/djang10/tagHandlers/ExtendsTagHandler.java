package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.appserver.templates.djang10.tagHandlers.BlockTagHandler.BlockNode;
import ed.js.JSFunction;

public class ExtendsTagHandler implements TagHandler {

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {
        String path = Parser.smartSplit(token.getContents())[1];
        Expression pathExpr = new Expression(path);

        parser.setStateVariable(this.getClass(), true);

        List<Node> nodeList = parser.parse();
        List<Node> blockList = new LinkedList<Node>();

        for (Node node : nodeList) {
            if (node instanceof BlockNode)
                blockList.add(node);
        }

        return new ExtendsNode(token, pathExpr, blockList);
    }

    public Map<String, JSFunction> getHelpers() {
        return new HashMap<String, JSFunction>();
    }

    private static class ExtendsNode extends TagNode {
        private final Expression path;
        private final List<Node> topLevelBlocks;

        public ExtendsNode(Token token, Expression path, List<Node> topLevelBlocks) {
            super(token);
            this.path = path;
            this.topLevelBlocks = topLevelBlocks;
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
            for (Node node : topLevelBlocks)
                node.toJavascript(preamble, buffer);

            buffer.appendHelper(startLine, JSHelper.CALL_PATH + "(");
            buffer.append(startLine, path.toJavascript());
            buffer.append(startLine, ", " + JSWriter.CONTEXT_STACK_VAR + ", " + JSWriter.RENDER_OPTIONS_VAR);
            buffer.append(startLine, ");\n");

        }

    }
}
