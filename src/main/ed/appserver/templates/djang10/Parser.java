package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jruby.RubyProcess.Sys;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.Node.VariableNode;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls3;

public class Parser extends JSObjectBase{

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

    
//==============================================================================================================================
    
    private final LinkedList<Token> tokens;
    private final Set<Library> loadedLibraries;
    private final Map<String, JSFunction> filterMapping;
    private final Map<String, JSFunction> tagHandlerMapping;
    private final Set<JxpSource> dependencies;

    public Parser(String string) {
        super(CONSTRUCTOR);

        this.tokens = new LinkedList<Token>();
        loadedLibraries = new HashSet<Library>();
        this.filterMapping = new HashMap<String, JSFunction>();
        this.tagHandlerMapping = new HashMap<String, JSFunction>();
        dependencies = new HashSet<JxpSource>();

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
 
    public NodeList parse(Scope scope, JSArray untilTagsList ) throws TemplateException {
        NodeList nodelist = create_nodelist();

        if(untilTagsList == null)
            untilTagsList = new JSArray();
        
        while (!tokens.isEmpty()) {
            Token token = next_token();

            if (token.type == TagDelimiter.Type.Text) {
                extend_nodelist(nodelist, new Node.TextNode(token.getContents()), token);
            }
            else if (token.type == TagDelimiter.Type.Var) {
                if (token.getContents().length() == 0)
                    throw new TemplateException(token.getStartLine(), "Empty Variable Tag");

                FilterExpression variableExpression = compile_filter(token.getContents().toString());
                VariableNode varNode = create_variable_node(variableExpression);
                extend_nodelist(nodelist, varNode, token);
            }
            else if (token.type == TagDelimiter.Type.Block) {
                if(untilTagsList.contains(token.getContents())) {
                    prepend_token(token);
                    return nodelist;
                }
                
                String command = token.getContents().toString().split("\\s")[0];
                
                JSFunction handler = getTagHandlers().get(command);
                if(handler == null)
                    throw new TemplateException("Unknown block tag: " + command);
                
                try {
                    JSObject node = (JSObject)handler.call(scope, this, token);
                    extend_nodelist(nodelist, node, token);
                } catch(TemplateException e) {
                    throw e;
                } catch(Exception e) {
                
                    throw new TemplateException("Failed to compile the block tag: " + command, e);
                }
            }
        }

        if (untilTagsList.size() > 0) {
            throw new TemplateException("Unclosed tags ");
        }

        return nodelist;
    }

    public void skip_past(JSString endtag) throws TemplateException {
        while(!tokens.isEmpty()) {
            Token token = next_token();
            if(token.type == TagDelimiter.Type.Block && endtag.equals(token.getContents()))
                return;
        }
        throw new TemplateException("Unclosed tags ");
    }
    public Token next_token() {
        return tokens.remove();
    }
    public void prepend_token(Token token) {
        tokens.addFirst(token);
    }
    public void delete_first_token() {
        next_token();
    }

    
    public NodeList create_nodelist() {
        return new NodeList();
    }
    public void extend_nodelist(NodeList nodeList, JSObject node, Token token) throws TemplateException  {
        boolean must_be_first = node.get("must_be_first") == Boolean.TRUE;
        if(must_be_first && nodeList.get("contains_nontext") == Boolean.TRUE)
            throw new TemplateException(node + "must be the first nontext node in the template");

        if(!(node instanceof Node.TextNode))
            nodeList.set("contains_nontext", true);
        
        nodeList.add(node);
    }
    
    
    public Node.VariableNode create_variable_node(FilterExpression expression) {
        return new Node.VariableNode(expression);
    }
    public Expression compile_expression(String str) throws TemplateException {
        return new Expression(str);
    }
    public FilterExpression compile_filter(String str) throws TemplateException {
        return new FilterExpression(this, str);
    }
    
    public void add_library(Library library) {
        add_library(library, true);
    }
    public void add_library(Library library, boolean overwrite) {
        if(loadedLibraries.contains(library))
            return;
        loadedLibraries.add(library);
        for(String tagName : library.getTags().keySet()) {
            if(!overwrite && tagHandlerMapping.containsKey(tagName))
                continue;
            tagHandlerMapping.put(tagName, (JSFunction)library.getTags().get(tagName));
        }
        for(String filterName : library.getFilters().keySet()) {
            if(!overwrite && filterMapping.containsKey(filterName))
                continue;
            filterMapping.put(filterName, (JSFunction)library.getFilters().get(filterName));
        }
    }

    public Set<Library> getLoadedLibraries() {
        return loadedLibraries;
    }
    public Map<String, JSFunction> getFilters() {
        return filterMapping;
    }
    public Map<String, JSFunction> getTagHandlers() {
        return tagHandlerMapping;
    }

    public void add_dependency(JxpSource file) {
        dependencies.add(file);
    }
    public Set<JxpSource> get_dependencies() {
        return dependencies;
    }
    
    
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls0() {
        public Object call(Scope scope, Object[] extra) {
            throw new NotImplementedException();
        }
        protected void init() {
            super.init();
            _prototype.set("parse", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    Parser thisObj = (Parser)scope.getThis();
                    try {
                        return thisObj.parse(scope, (JSArray)p0);
                    } catch (TemplateException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    };
//==============================================================================================================================
    
    
    public static class Token extends JSObjectBase {
        public static final String NAME = "Token";
        
        private TagDelimiter.Type type;

        private int startLine;

        private Token() {
            super(CONSTRUCTOR);
        }
        
        public Token(TagDelimiter.Type type, String contents, int startLine) {
            this();
            this.type = type;
            this.startLine = startLine;
            
            set("contents", new JSString(contents));
        }

        public JSString getContents() {
            return (JSString)get("contents");
        }

        public int getStartLine() {
            return startLine;
        }
        public String[] split_contents() {
            return Parser.smartSplit(getContents().toString());
        }
        
        public String toString() {
            return "<" + type + ": " + getContents().toString().substring(0, Math.min(20, getContents().length())) + "...>";
        }
        
        public static final JSFunction CONSTRUCTOR = new JSFunctionCalls3() {
            public Object call(Scope scope, Object typeObj, Object contentsObj, Object startLineObj, Object[] extra) {
                Token thisObj = (Token)scope.getThis();
                
                JSString type = (JSString)typeObj;
                JSString contents = (JSString)contentsObj;
                int startLine = (Integer)startLineObj;
                
                thisObj.type = Enum.valueOf(TagDelimiter.Type.class, type.toString());
                thisObj.set("contents", contents.toString());
                thisObj.startLine = startLine;

                return null;
            }
            public JSObject newOne() {
                return new Token();
            }
            protected void init() {
                _prototype.set("split_contents", new JSFunctionCalls0() {
                    public Object call(Scope scope, Object[] extra) {
                        Token thisObj = (Token)scope.getThis();
                        JSArray parts = new JSArray();
                        for(String part : thisObj.split_contents())
                            parts.add(new JSString(part));
                        return parts;
                    }
                });
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
