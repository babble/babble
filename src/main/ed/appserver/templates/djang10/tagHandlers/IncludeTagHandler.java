package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class IncludeTagHandler implements TagHandler {

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {
        String[] parts = Parser.smartSplit(token.getContents());
        Expression var = new Expression(parts[1]);

        return new IncludeNode(token, var);
    }

    public Map<String, JSFunction> getHelpers() {
        Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();

        return helpers;
    }

    private static class IncludeNode extends TagNode {
        private Expression var;

        public IncludeNode(Token token, Expression var) {
            super(token);

            this.var = var;
        }

        @Override
        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.appendHelper(startLine, JSHelper.CALL_PATH + "(" + var.toJavascript() + ", " + JSWriter.CONTEXT_STACK_VAR
                    + ");\n");
        }
    }
}
