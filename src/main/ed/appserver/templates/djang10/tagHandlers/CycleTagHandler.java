package ed.appserver.templates.djang10.tagHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Node.TagNode;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;

public class CycleTagHandler implements TagHandler {
    private static final String namedCycles_var = "__namedCycles__";
    private static final String anonymous = "__anonymous__";

    public TagNode compile(Parser parser, String command, Token token) throws TemplateException {
        Pattern p = Pattern.compile("^\\s*(?:\\S+) (.+?)(?: as (\\S+))?\\s*$");
        Matcher m = p.matcher(token.getContents());
        if (!m.find())
            throw new TemplateException("invalid syntax for cycle tag");

        String param = m.group(1);
        String alias = m.group(2);
        boolean isFirst = false;

        // try splitting by commas
        List<String> paramParts = Arrays.asList(Parser.smartSplit(param, ","));

        // if none are found, then try spaces
        if (paramParts.size() == 1) {
            paramParts = new ArrayList<String>();
            for (String paramPart : Parser.smartSplit(param, " ")) {
                paramPart = Parser.dequote(paramPart.trim());
                paramParts.add(paramPart);
            }
        }

        // dictionary for name mangling
        Map<String, Integer> cycleDict = parser.getStateVariable(getClass());
        if (cycleDict == null) {
            cycleDict = new HashMap<String, Integer>();
            parser.setStateVariable(getClass(), cycleDict);
            isFirst = true;
        }

        // {% cycle name %}
        if (paramParts.size() == 1 && alias == null) {
            alias = paramParts.get(0);

            Integer rank = cycleDict.get(alias);
            if (rank == null)
                throw new TemplateException("Cycle name " + alias + " doens't exist");

            String mangledName = alias + rank.toString();

            return new CycleCallNode(token, mangledName);
        }
        // {% cycle item1,item2 %} or {% cycle item1,item2 as name %}
        else if (paramParts.size() > 1) {
            if (alias == null)
                alias = anonymous;

            Integer rank = cycleDict.get(alias);
            if (rank == null)
                rank = -1;

            rank++;
            cycleDict.put(alias, rank);

            String mangledName = alias + rank;
            List<String> itemList = paramParts;

            return new CycleDefinitionNode(token, mangledName, itemList, isFirst);
        } else {
            throw new TemplateException("Syntax error");
        }
    }

    public Map<String, JSFunction> getHelpers() {
        return new HashMap<String, JSFunction>();
    }

    private static class CycleCallNode extends TagNode {
        protected final String name;

        public CycleCallNode(Token token, String name) {
            super(token);
            this.name = name;
        }

        @Override
        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
            buffer.append("print(");
            buffer.append(namedCycles_var + "[\"" + name + "\"].items");
            buffer.append("[" + namedCycles_var + "[\"" + name + "\"].iter]");
            buffer.append(");\n");

            buffer.append(namedCycles_var + "[\"" + name + "\"].iter = (" + namedCycles_var + "[\"" + name + "\"].iter + 1) % "
                    + namedCycles_var + "[\"" + name + "\"].items.length;\n");
        }
    }

    private static class CycleDefinitionNode extends CycleCallNode {
        private final List<String> itemList;
        private final boolean isFirstDefinitionNode;

        public CycleDefinitionNode(Token token, String name, List<String> itemList, boolean isFirst) {
            super(token, name);
            this.itemList = itemList;
            this.isFirstDefinitionNode = isFirst;
        }

        @Override
        public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
            if (isFirstDefinitionNode)
                preamble.append("var " + namedCycles_var + " = {};\n");

            preamble.append(namedCycles_var + "[\"" + name + "\"] = {");
            preamble.append("items : [");
            boolean isFirst = true;

            for (String item : itemList) {
                if (!isFirst)
                    preamble.append(", ");
                isFirst = false;

                preamble.append("\"" + item + "\"");
            }
            preamble.append("],");
            preamble.append("iter: 0");
            preamble.append("};\n");

            super.getRenderJSFn(preamble, buffer);
        }
    }
}
