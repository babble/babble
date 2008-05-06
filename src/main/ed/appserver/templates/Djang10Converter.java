// Djang10Converter.java

package ed.appserver.templates;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.UnresolvedValue;
import ed.appserver.templates.djang10.Variable;
import ed.appserver.templates.djang10.Variable.FilterSpec;
import ed.appserver.templates.djang10.filters.DefaultFilter;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.appserver.templates.djang10.tagHandlers.BlockTagHandler;
import ed.appserver.templates.djang10.tagHandlers.CommentTagHandler;
import ed.appserver.templates.djang10.tagHandlers.ExtendsTagHandler;
import ed.appserver.templates.djang10.tagHandlers.ForTagHandler;
import ed.appserver.templates.djang10.tagHandlers.IfEqualTagHandler;
import ed.appserver.templates.djang10.tagHandlers.IfTagHandler;
import ed.appserver.templates.djang10.tagHandlers.IncludeTagHandler;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;


public class Djang10Converter implements TemplateConverter {
	public static final String extension = "djang10";
	public Djang10Converter(){

    }

	public Result convert(Template t) {
		if(!(extension).equals( t._extension))
			return null;
		
		Parser parser = new Parser(t._content);
		LinkedList<Node> nodeList = parser.parse();
		JSWriter writer = new JSWriter();
		
		writer.append("var "+JSWriter.CONTEXT_STACK_VAR+" = (arguments.length == 0)? [scope] : (arguments[0] instanceof Array)? arguments[0] : [arguments[0]];\n");
		writer.append("var "+JSWriter.RENDER_OPTIONS_VAR+" = (arguments.length < 2)? {} : arguments[1];\n");
		for(Node node : nodeList) {
			node.getRenderJSFn(writer);
		}
		
		String newName = t.getName().replaceAll( "\\.("+extension+")+$" , "_$1.js" );
		return new Result(new Template(newName, writer.toString()), writer.getLineMap());
	}
	
	
	public static Object resolveVariable(Scope scope, UnresolvedValue unresolvedValue) {
		String varName = unresolvedValue.stringRef;
		
		if(Parser.isQuoted(varName))
			return Parser.dequote(varName);
		
		try {
			return Integer.valueOf(varName).toString();
		} catch(Exception e) {}
		try {
			return Float.valueOf(varName).toString();
		} catch(Exception e) {}		
		
		String[] varNameParts = varName.split("\\.");
		
		// find the starting point
		JSArray contextStack = (JSArray)scope.get(JSWriter.CONTEXT_STACK_VAR);
		Object varValue = null;
		
		for(int i=contextStack.size() - 1; i>=0 && varValue == null; i--) {
			JSObject context = (JSObject)contextStack.get(i);
			varValue = context.get(varNameParts[0]);
		}
		if(varValue == null)
			throw new NoSuchFieldError();
		
		// find the rest of the variable members
		for(int i=1; i<varNameParts.length; i++) {
			String varNamePart = varNameParts[i];

			if(varValue == null || !(varValue instanceof JSObject))
				throw new NoSuchFieldError();
			
			JSObject varValueJsObj = (JSObject)varValue;
			
			if(!varValueJsObj.containsKey(varNamePart))
				throw new NoSuchFieldError();
			
			varValue = varValueJsObj.get(varNamePart);
			
			if(varValue instanceof JSFunction)
				varValue = ((JSFunction)varValue).callAndSetThis(scope.child(), varValueJsObj, new Object[0]);
		}

		return varValue;
	}

    //Helpers
    public static void injectHelpers(Scope scope) {
    	JSObjectBase namespace = new JSObjectBase();
    	scope.set(JSWriter.NS, namespace);
    	
    	Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
    	    	
    	for (TagHandler tagHandler : _tagHandlers.values()) {
    		helpers.putAll(tagHandler.getHelpers());
		}
    	for(String name : helpers.keySet()) {
    		namespace.set(name, helpers.get(name));
    	}
    	
    	namespace.set(JSWriter.VAR_EXPAND, new JSFunctionCalls2() {
    		@Override
    		public Object call(Scope scope, Object varName, Object defaultValue, Object[] extra) {
    			Object value = null;
    			boolean wasFound = false;
    			
    			
    			Variable variable = Parser.parseVariable(((JSString)varName).toString());
				try {
					value = resolveVariable(scope, new UnresolvedValue( variable.base));
					wasFound = true;
				} catch(NoSuchFieldError e) { }
				
				for(FilterSpec filterSpec : variable.filters) {
					Filter filter = _filters.get(filterSpec.name);
					value = filter.apply(wasFound, value, filterSpec.param);
					
					if(value instanceof UnresolvedValue) {
						try {
							value = resolveVariable(scope, (UnresolvedValue)value);
							wasFound = true;
						} catch(Exception e) {
							wasFound = false;
							value = null;
						}
					}
				}
    			
    			return value == null? defaultValue : value;
    		}
    	});
    }
    
    //TagHandler Registration
    public static Map<String, TagHandler> getTagHandlers() {
    	return _tagHandlers;
    }
    private static HashMap<String, TagHandler> _tagHandlers = new HashMap<String, TagHandler>();
    static {
    	_tagHandlers.put("if", new IfTagHandler());
    	_tagHandlers.put("for", new ForTagHandler());
    	_tagHandlers.put("include", new IncludeTagHandler());
    	_tagHandlers.put("block", new BlockTagHandler());
    	_tagHandlers.put("extends", new ExtendsTagHandler());
    	_tagHandlers.put("ifequal", new IfEqualTagHandler(false));
    	_tagHandlers.put("ifnotequal", new IfEqualTagHandler(true));
    	_tagHandlers.put("comment", new CommentTagHandler());
    }

    
    //Filter Registration
    public static Map<String, Filter> getFilters() {
    	return _filters;
    }
    private static Map<String, Filter> _filters = new HashMap<String, Filter>();
    static {
    	_filters.put("default", new DefaultFilter());
    }
}
