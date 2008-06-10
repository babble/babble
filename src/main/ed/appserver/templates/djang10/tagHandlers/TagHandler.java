package ed.appserver.templates.djang10.tagHandlers;

import java.util.Map;

import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.js.JSFunction;

public interface TagHandler {
    public TagNode compile(Parser parser, String command, Parser.Token token) throws TemplateException;

    public Map<String, JSFunction> getHelpers();
}
