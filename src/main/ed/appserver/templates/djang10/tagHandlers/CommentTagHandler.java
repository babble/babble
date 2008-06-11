package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class CommentTagHandler implements TagHandler {

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {
        parser.parse("end" + command);
        parser.nextToken();
        return new CommentNode(token);
    }

    public Map<String, JSFunction> getHelpers() {
        return new HashMap<String, JSFunction>();
    }

    private static class CommentNode extends TagNode {
        public CommentNode(Token token) {
            super(token);
        }

        public void toJavascript(JSWriter preamble, JSWriter buffer) throws TemplateException {
            // noop
        }
    }
}
