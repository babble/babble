package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.FilterList;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class FilterTagHandler implements TagHandler {
    public static final String HIJACK_PRINT = "hijackPrint";
    public static final String REVERT_PRINT = "revertPrint";

    public Node compile(Parser parser, String command, Token token) throws TemplateException {
        List<Node> nodyNodes = parser.parse("end" + command);
        parser.nextToken();

        String filterParams = token.contents.split("\\s", 2)[1].trim();
        FilterList filters = new FilterList(parser, filterParams);

        return new FilterNode(token, filters, nodyNodes);
    }

    public Map<String, JSFunction> getHelpers() {
        Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();

        helpers.put(HIJACK_PRINT, new JSFunctionCalls0() {
            public Object call(Scope scope, Object[] extra) {
                JSFunction oldPrint = (JSFunction) scope.get("print");
                scope.set("print", new PrintBuffer(oldPrint));

                return null;
            }
        });

        helpers.put(REVERT_PRINT, new JSFunctionCalls0() {
            public Object call(Scope scope, Object[] extra) {
                PrintBuffer printBuffer = (PrintBuffer) scope.get("print");
                scope.set("print", printBuffer.oldPrint);
                return null;
            }
        });

        return helpers;
    }

    private static class FilterNode extends Node {
        private final List<Node> bodyNodes;
        private final FilterList filters;

        public FilterNode(Token token, FilterList filters, List<Node> bodyNodes) {
            super(token);

            this.filters = filters;
            this.bodyNodes = bodyNodes;
        }

        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.appendHelper(startLine, HIJACK_PRINT + "();\n");

            for (Node node : bodyNodes)
                node.getRenderJSFn(preamble, buffer);

            buffer.append("var printOutput = print.toString();\n");
            buffer.appendHelper(startLine, REVERT_PRINT + "();\n");
            buffer.append(startLine, "print(" + JSHelper.NS + "." + Expression.DEFAULT_VALUE + "("
                    + filters.toJavascript("printOutput") + ", \"\"));\n");
            buffer.append(startLine, "delete scope.printOutput;\n");
        }
    }

    private static class PrintBuffer extends JSFunctionCalls1 {
        public final JSFunction oldPrint;
        public final StringBuilder buffer;

        public PrintBuffer(JSFunction oldPrint) {
            this.oldPrint = oldPrint;
            buffer = new StringBuilder();
        }

        public Object call(Scope scope, Object p0, Object[] extra) {
            buffer.append(p0);
            return null;
        }

        public String toString() {
            return buffer.toString();
        }
    }
}
