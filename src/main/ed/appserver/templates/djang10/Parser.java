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
import ed.appserver.templates.djang10.tagHandlers.TagHandler;


public class Parser {
	private static final String FILTER_SEPARATOR = "|";
	private static final String FILTER_ARGUMENT_SEPARATOR = ":";
	private static final String VARIABLE_ATTRIBUTE_SEPARATOR = ".";
	private static final String SINGLE_BRACE_START = "{";
	private static final String SINGLE_BRACE_END = "}";
	
	private static final Map<String, TagDelimiter>  tags;
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
		for(TagDelimiter delim : tags.values()) {
			if(!isFirst)
				buffer.append('|');
			isFirst = false;
			buffer.append(delim.start.replaceAll("(.)", "\\\\$1"));
			buffer.append(".*?");
			buffer.append(delim.end.replaceAll("(.)", "\\\\$1"));
		}
		buffer.append(')');
		
		regex = Pattern.compile(buffer.toString());
	}
	
	private LinkedList<Token> tokens;
	private Map<Class<? extends TagHandler>, Object> stateVariables;
	
	public Parser(String string) {
		this.tokens = new LinkedList<Token>();
		this.stateVariables = new HashMap<Class<? extends TagHandler>, Object>();
		
		int line = 1;
		boolean inTag = false;
		Tokenizer tokenizer = new Tokenizer(string, regex, true);
		
		while(tokenizer.hasNext()) {
			String bit = tokenizer.next();
			
			if(bit.length() > 0) {
				int startLine = line;
				line += Util.countOccurance(bit, '\n');
				
				if(inTag) {
					TagDelimiter delim = tags.get(bit.substring(0, 2));
					
					String content = bit.substring(2, bit.length() - 2).trim();
					tokens.add(new Token(delim.type, content, startLine, line));
				}
				else {
					tokens.add(new Token(TagDelimiter.Type.Text, bit, startLine, line));
				}
			}
			inTag = !inTag;
		}
	}

	
	public LinkedList<Node> parse(String... untilTags) throws TemplateException {
		LinkedList<Node> nodes = new LinkedList<Node>();
		
		while(!tokens.isEmpty()) {
			Token token = nextToken();
			
			if(token.type == TagDelimiter.Type.Text) {
				nodes.add(new Node.TextNode(token));
			}
			else if(token.type == TagDelimiter.Type.Var) {
				if(token.contents.length() == 0)
					throw new TemplateException(token.startLine, token.endLine, "Empty Variable Tag");
				
				nodes.add(Djang10Converter.getVariableTagHandler().compile(this, null, token));
			}
			else if(token.type == TagDelimiter.Type.Block) {
				for(String untilTag : untilTags) {
					if(token.contents.contains(untilTag)) {
						tokens.addFirst(token);
						return nodes;
					}
				}
				
				String command = token.contents.split("\\s")[0];
				TagHandler handler = Djang10Converter.getTagHandlers().get(command);
				Node node = handler.compile(this, command, token);
				nodes.add(node);
			}
		}

		if(untilTags.length > 0)
			throw new TemplateException("Unclosed tags: " + Arrays.toString(untilTags));

		return nodes;
	}
	
	public Token nextToken() {
		return tokens.remove();
	}

	
	public <T> void setStateVariable(Class<? extends TagHandler> key, T value) {
		stateVariables.put(key, value);
	}
	public <T> T getStateVariable(Class<? extends TagHandler> key) {
		return (T)stateVariables.get(key);
	}
	public void clearStateVariable(Class<? extends TagHandler> key) {
		stateVariables.remove(key);
	}
	
	public static class Token {
		public final TagDelimiter.Type type;
		public final String contents;
		public final int startLine, endLine;
		
		public Token(TagDelimiter.Type type, String contents, int startLine, int endLine) {
			super();
			this.type = type;
			this.contents = contents;
			this.startLine = startLine;
			this.endLine = endLine;
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
			Text,
			Var,
			Block,
			Comment
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
			if(matcher == null)
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
            } 
            else if (returnDelims && lastEnd < input.length()) {
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
		String quotes = "\'\"";
		delims += quotes;
		
    	ArrayList<String> parts = new ArrayList<String>();	
    	StringTokenizer tokenizer = new StringTokenizer(str, delims, true);
    	StringBuilder buffer = new StringBuilder();
    	char openQuote = '\"';
    	boolean inQuote = false;
    	
    	
    	while(tokenizer.hasMoreTokens()) {
    		String token = tokenizer.nextToken();
    		
    		boolean isQuote = token.length() == 1 && quotes.contains(token);
    		boolean isDelim = token.length() == 1 && delims.contains(token);
    		
    		
    		if(isQuote) {
	    		if(!inQuote) {
	    			openQuote = token.charAt(0);
	    			inQuote = true;
	    		} 
	    		else if(openQuote == token.charAt(0))
    				inQuote = false;
    		}
    		else if(!inQuote && isDelim) {
    			parts.add(buffer.toString());
    			buffer.setLength(0);
    			
    			continue;
    		}
    		buffer.append(token);
    	}
    	parts.add(buffer.toString());
    	
    	String[] partArray = new String[parts.size()];
    	return parts.toArray(partArray);
    }
	
	public static Variable parseVariable(String var) {
		Variable variable = new Variable();
		
		String[] parts = smartSplit(var, "|");
		
		variable.base = parts[0];		
		
		for(int i=1; i<parts.length; i++) {
			String[] filterParts = parts[i].split(":", 2);
			String filterName;
			String filterParam = null;
			
			filterName = filterParts[0].trim();
			if(filterParts.length > 1) {
				filterParam = filterParts[1].trim();
			}
			
			variable.filters.add(new Variable.FilterSpec(filterName, filterParam));
		}
		return variable;
	}
	
	public static String dequote(String str) {
		if(isQuoted(str) ) {
			str = str.substring(1, str.length() -1 );
		}
		return str;
	}
	
	public static boolean isQuoted(String str) {
		return str != null && str.length()>1 && "\"\'".contains(""+str.charAt(0)) && (str.charAt(0)==str.charAt(str.length() -1) );
	}
}
