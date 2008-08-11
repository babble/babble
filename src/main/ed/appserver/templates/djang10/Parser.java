/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver.templates.djang10;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ed.appserver.jxp.JxpSource;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

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

    public Parser(String origin, String string) {
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
                    tokens.add(new Token(delim.type, origin, content, startLine));
                } else {
                    tokens.add(new Token(TagDelimiter.Type.Text, origin, bit, startLine));
                }
            }
            inTag = !inTag;
        }
    }
 
    public NodeList parse(Scope scope, JSArray untilTagsList ) {
        NodeList nodelist = create_nodelist();

        if(untilTagsList == null)
            untilTagsList = new JSArray();
        
        while (!tokens.isEmpty()) {
            Token token = next_token();

            if (token.type == TagDelimiter.Type.Text) {
                JSObject textNode = new Node.TextNode(token.getContents());
                textNode = NodeWrapper.wrap(textNode, token);
                extend_nodelist(nodelist, textNode, token);
            }
            else if (token.type == TagDelimiter.Type.Var) {
                if (token.getContents().length() == 0)
                    throw new TemplateException(token.getStartLine(), "Empty Variable Tag");

                FilterExpression variableExpression = compile_filter(token.getContents().toString());
                JSObject varNode = new Node.VariableNode(variableExpression);
                varNode = NodeWrapper.wrap(varNode, token);
                extend_nodelist(nodelist, varNode, token);
            }
            else if (token.type == TagDelimiter.Type.Block) {
                if(untilTagsList.contains(token.getContents())) {
                    prepend_token(token);
                    return nodelist;
                }
                
                String command = token.getContents().toString().split("\\s")[0];
                JSFunction handler = find_taghandler(command);
                JSObject node = (JSObject)handler.call(scope, this, token);
                node = NodeWrapper.wrap(node, token);
                extend_nodelist(nodelist, node, token);

            }
        }

        if (untilTagsList.size() > 0) {
            throw new TemplateException("Unclosed tags, expected: ["  + untilTagsList.toString() + "]");
        }

        return nodelist;
    }

    public void skip_past(JSString endtag) {
        while(!tokens.isEmpty()) {
            Token token = next_token();
            if(token.type == TagDelimiter.Type.Block && endtag.equals(token.getContents()))
                return;
        }
        throw new TemplateException("Unclosed tags, exptected: " + endtag);
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
    public void extend_nodelist(NodeList nodeList, JSObject node, Token token) {
        boolean must_be_first = node.get("must_be_first") == Boolean.TRUE;
        if(must_be_first && nodeList.get("contains_nontext") == Boolean.TRUE)
            throw new TemplateException(node + "must be the first nontext node in the template");

        if(!(node instanceof Node.TextNode))
            nodeList.set("contains_nontext", true);
        
        nodeList.add(node);
    }
    
    
    public Expression compile_expression(String str) {
        return new Expression(str);
    }
    public FilterExpression compile_filter(String str) {
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
    public JSFunction find_filter(String name) {
        JSFunction filter = this.filterMapping.get(name);
        if(filter == null)
            throw new TemplateException("Undefined filter: " + name);
        
        return filter;
    }
    public JSFunction find_taghandler(String name) {
        JSFunction handler = this.tagHandlerMapping.get(name);
        if(handler == null)
            throw new TemplateException("Undefined tag: " + name);
        
        return handler;
    }

    public void add_dependency(JxpSource file) {
        dependencies.add(file);
    }
    public Set<JxpSource> get_dependencies() {
        return dependencies;
    }
    
    
    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls0() {
        public Object call(Scope scope, Object[] extra) {
            throw new UnsupportedOperationException();
        }
        protected void init() {
            super.init();
            _prototype.set("parse", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    Parser thisObj = (Parser)scope.getThis();

                    return thisObj.parse(scope, (JSArray)p0);
                }
            });
        }
    };
//==============================================================================================================================
    
    
    public static class Token extends JSObjectBase {
        public static final String NAME = "Token";
        
        private String origin;
        private TagDelimiter.Type type;
        private int startLine;

        private Token() {
            super(CONSTRUCTOR);
        }
        
        public Token(TagDelimiter.Type type, String origin, String contents, int startLine) {
            this();
            this.type = type;
            this.origin = origin;
            this.startLine = startLine;
            
            set("contents", new JSString(contents));
        }

        public JSString getContents() {
            return (JSString)get("contents");
        }

        public int getStartLine() {
            return startLine;
        }
        
        public String toString() {
            return "<" + type + ": " + getContents().toString().substring(0, Math.min(20, getContents().length())) + "...>";
        }
        
        public JSArray split_contents() {
            JSArray parts = new JSArray();
            for(String part : Util.smart_split(getContents().toString()))
                parts.add(new JSString(part));
            return parts;
        }
        public String getOrigin() {
            return origin;
        }
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
