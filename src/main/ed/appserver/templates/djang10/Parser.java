package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.appserver.templates.Djang10Converter;
import ed.appserver.templates.djang10.Node.VariableNode;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSFunction;
import ed.js.JSON;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls3;
import ed.util.DependencyTracker;

public class Parser {

    private static final Map<String, TagDelimiter> tags;
    static {
        tags = new HashMap<String, TagDelimiter>();
        tags.put("{{", new TagDelimiter(TagDelimiter.Type.Var, "{{", "}}"));
        tags.put("{%", new TagDelimiter(TagDelimiter.Type.Block, "{%", "%}"));
        tags.put("{#", new TagDelimiter(TagDelimiter.Type.Comment, "{#", "#}"));

    }

    private static final Pattern regex;
    static {
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        boolean isFirst = true;
        for (TagDelimiter delim : tags.values()) {
            if (!isFirst)
                buffer.append('|');
            isFirst = false;
            buffer.append(delim.start.replaceAll("(.)", "\\\\$1"));
            buffer.append(".*?");
            buffer.append(delim.end.replaceAll("(.)", "\\\\$1"));
        }
        buffer.append(')');

        regex = Pattern.compile(buffer.toString());
    }

    private final LinkedList<Token> tokens;
    private final Map<Class<? extends TagHandler>, Object> stateVariables;
    private final Map<String, Filter> filterMapping;
    private final Map<String, TagHandler> tagHandlerMapping;
    private final DependencyTracker tracker;

    public Parser(String string, DependencyTracker tracker, Map<String, ? extends Filter> filterMapping, Map<String, ? extends TagHandler> tagHandlerMapping) {
        this.tokens = new LinkedList<Token>();
        this.stateVariables = new HashMap<Class<? extends TagHandler>, Object>();
        this.tracker = tracker;
        this.filterMapping = new HashMap<String, Filter>(filterMapping);
        this.tagHandlerMapping = new HashMap<String, TagHandler>( tagHandlerMapping );

        int line = 1;
        boolean inTag = false;
        Tokenizer tokenizer = new Tokenizer(string, regex, true);

        while (tokenizer.hasNext()) {
            String bit = tokenizer.next();

            if (bit.length() > 0) {
                int startLine = line;
                line += Util.countOccurance(bit, '\n');

                if (inTag) {
                    TagDelimiter delim = tags.get(bit.substring(0, 2));

                    String content = bit.substring(2, bit.length() - 2).trim();
                    tokens.add(new Token(delim.type, content, startLine));
                } else {
                    tokens.add(new Token(TagDelimiter.Type.Text, bit, startLine));
                }
            }
            inTag = !inTag;
        }
    }

    public LinkedList<Node> parse(String... untilTags) throws TemplateException {
        LinkedList<Node> nodes = new LinkedList<Node>();

        while (!tokens.isEmpty()) {
            Token token = nextToken();

            if (token.type == TagDelimiter.Type.Text) {
                nodes.add(new Node.TextNode(token));
            } else if (token.type == TagDelimiter.Type.Var) {
                if (token.getContents().length() == 0)
                    throw new TemplateException(token.getStartLine(), "Empty Variable Tag");

                FilterExpression variableExpression = new FilterExpression(this, token.getContents());
                VariableNode varNode = new VariableNode(token, variableExpression);
                nodes.add(varNode);
            } else if (token.type == TagDelimiter.Type.Block) {
                for (String untilTag : untilTags) {
                    if (token.getContents().contains(untilTag)) {
                        tokens.addFirst(token);
                        return nodes;
                    }
                }

                String command = token.getContents().split("\\s")[0];
                TagHandler handler = getTagHandlers().get(command);
                Node node = handler.compile(this, command, token);
                nodes.add(node);
            }
        }

        if (untilTags.length > 0)
            throw new TemplateException("Unclosed tags: " + Arrays.toString(untilTags));

        return nodes;
    }

    public Token nextToken() {
        return tokens.remove();
    }

    public DependencyTracker getTracker() {
        return tracker;
    }

    public Map<String, Filter> getFilters() {
        return filterMapping;
    }

    public Map<String, TagHandler> getTagHandlers() {
        return tagHandlerMapping;
    }

    public <T> void setStateVariable(Class<? extends TagHandler> key, T value) {
        stateVariables.put(key, value);
    }

    public <T> T getStateVariable(Class<? extends TagHandler> key) {
        return (T) stateVariables.get(key);
    }

    public void clearStateVariable(Class<? extends TagHandler> key) {
        stateVariables.remove(key);
    }

    public static class Token extends JSObjectBase {
        public static final String NAME = "Token";
        
        private TagDelimiter.Type type;
        private String contents;

        private int startLine;

        public Token(TagDelimiter.Type type, String contents, int startLine) {
            this();
            this.type = type;
            this.contents = contents;
            this.startLine = startLine;
        }

        private Token() {
            super(CONSTRUCTOR);
        }
        
        public String getContents() {
            return contents;
        }

        public int getStartLine() {
            return startLine;
        }
        public String toJavascript() {
            return "( new " + JSHelper.NS + "." + NAME + "(\"" + type + "\", " + JSON.serialize(contents) + ", " + startLine + ") )";
        }
        
        public static final JSFunction CONSTRUCTOR = new JSFunctionCalls3() {
            public Object call(Scope scope, Object typeObj, Object contentsObj, Object startLineObj, Object[] extra) {
                Token thisObj = (Token)scope.getThis();
                
                JSString type = (JSString)typeObj;
                JSString contents = (JSString)contentsObj;
                int startLine = (Integer)startLineObj;
                
                thisObj.type = Enum.valueOf(TagDelimiter.Type.class, type.toString());
                thisObj.contents = contents.toString();
                thisObj.startLine = startLine;

                return null;
            }
            public JSObject newOne() {
                return new Token();
            }
        };
    }

    private static class TagDelimiter {
        public final String start, end;
        public final Type type;

        public TagDelimiter(Type type, String start, String end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }
        
        public enum Type {
            Text, Var, Block, Comment
        }
    }

    private class Tokenizer implements Iterator<String> {
        private String input;
        private Matcher matcher;
        private boolean returnDelims;

        private String delim;
        private String match;
        private int lastEnd;

        public Tokenizer(String input, Pattern pattern, boolean returnDelims) {
            this.input = input;
            this.matcher = pattern.matcher(input);
            this.returnDelims = returnDelims;

            delim = null;
            match = null;
            lastEnd = 0;
        }

        public boolean hasNext() {
            if (matcher == null)
                return false;

            if (delim != null || match != null) {
                return true;
            }
            if (matcher.find()) {
                if (returnDelims) {
                    delim = input.substring(lastEnd, matcher.start());
                }
                match = matcher.group();
                lastEnd = matcher.end();
            } else if (returnDelims && lastEnd < input.length()) {
                delim = input.substring(lastEnd, input.length());
                lastEnd = input.length();

                matcher = null;
            }
            return delim != null || match != null;
        }

        public String next() {
            String result = null;

            if (delim != null) {
                result = delim;
                delim = null;
            } else if (match != null) {
                result = match;
                match = null;
            }
            return result;
        }

        public boolean isNextToken() {
            return delim == null && match != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static String[] smartSplit(String str) {
        return smartSplit(str, " \t\r\n");
    }

    public static String[] smartSplit(String str, String delims) {
        return smartSplit(str, delims, -1);
    }

    public static String[] smartSplit(String str, String delims, int limit) {
        String quotes = "\'\"";
        delims += quotes;

        ArrayList<String> parts = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(str, delims, true);
        int pos = 0;
        StringBuilder buffer = new StringBuilder();
        char openQuote = '\"';
        boolean inQuote = false;

        while (tokenizer.hasMoreTokens() && (parts.size() < limit - 1 || limit < 0)) {
            String token = tokenizer.nextToken();
            pos += token.length();

            boolean isQuote = token.length() == 1 && quotes.contains(token);
            boolean isDelim = token.length() == 1 && delims.contains(token);

            if (isQuote) {
                if (!inQuote) {
                    openQuote = token.charAt(0);
                    inQuote = true;
                } else if (openQuote == token.charAt(0))
                    inQuote = false;
            } else if (!inQuote && isDelim) {
                parts.add(buffer.toString());
                buffer.setLength(0);

                continue;
            }
            buffer.append(token);
        }
        parts.add(buffer.toString() + str.substring(pos));

        String[] partArray = new String[parts.size()];
        return parts.toArray(partArray);
    }

    public static String dequote(String str) {
        if (isQuoted(str)) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static boolean isQuoted(String str) {
        return str != null && str.length() > 1 && "\"\'".contains("" + str.charAt(0))
                && (str.charAt(0) == str.charAt(str.length() - 1));
    }
}
