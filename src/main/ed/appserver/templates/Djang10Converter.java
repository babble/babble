// Djang10Converter.java

package ed.appserver.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ed.appserver.templates.djang10.ElseTagHandler;
import ed.appserver.templates.djang10.EndForLoopTag;
import ed.appserver.templates.djang10.ForLoopTagHandler;
import ed.appserver.templates.djang10.IfEndTagHandler;
import ed.appserver.templates.djang10.IfTagHandler;
import ed.appserver.templates.djang10.IncludeTagHandler;
import ed.appserver.templates.djang10.TagHandler;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;


public class Djang10Converter extends HtmlLikeConverter {
	public Djang10Converter(){
        super( "djang10" , _codeTags );
    }

    protected boolean wants( Template t ){
        return t.getName().endsWith( ".djang10" );
    }

    protected String getNewName( Template t ){
        return t.getName().replaceAll( "\\.(djang10)$" , "_$1.js" );
    }
    
    @Override
    protected Generator createGenerator(Template t, State s) {
    	return new MyGenerator(s);
    }
    
    @Override
    protected void start(Generator g) {
    	//inject helpers
    	g.append( "var "+MyGenerator.CONTEXT_STACK_VAR+" = (arguments.length == 0)? [scope] : (arguments[0] instanceof Array)? arguments[0] : [arguments[0]];\n" );

    }

    protected void gotCode( Generator g , CodeMarker cm , String code ){
        MyGenerator myG = (MyGenerator)g;
        
    	if ( cm._startTag.equals( "{{" ) ){
    		myG.append("print(");
    		myG.appendVarExpansion(code.trim(), "\"\"");
    		myG.append(");\n");
            return;
        }
    	
    	if(cm._startTag == "{%") {
    		code = code.trim();
    		
    		String[] p = smartSplit(code);
    		String tagName = p[0];
    		String[] params = new String[p.length - 1];
    		System.arraycopy(p, 1, params, 0, p.length -1);
    		
    		_tagHandlers.get(tagName).Compile(myG, tagName, params);
    		
    		return;
    	}
        throw new RuntimeException( "can't handle : " + cm._startTag );
    }

    protected boolean gotStartTag( Generator g , String tag , State state ){
        return false;
    }
    
    protected boolean gotEndTag( Generator g , String tag , State state ){
        return false;
    }
    
    static List<CodeMarker> _codeTags = new ArrayList<CodeMarker>();
    static {
        _codeTags.add( new CodeMarker( "{{" , "}}" ) );
        _codeTags.add( new CodeMarker( "{%" , "%}" ) );

    }
    
    public static String[] smartSplit(String str) {
    	ArrayList<String> parts = new ArrayList<String>();
    	
    	String delims = " \t\r\n\"";
    	StringTokenizer tokenizer = new StringTokenizer(str, delims, true);

    	StringBuilder quotedBuffer = null;
    	
    	
    	while(tokenizer.hasMoreTokens()) {
    		String token = tokenizer.nextToken();
    		
    		if(token.length() == 1 && delims.contains(token)) {
    			if("\"".equals(token)) {
    				if(quotedBuffer == null)
    					quotedBuffer = new StringBuilder();
    				else {
    					parts.add(quotedBuffer.toString());
    					quotedBuffer = null;
    				}
    			}
    			else if(quotedBuffer != null) {
    					quotedBuffer.append(token);
				}
    		}
    		else {
    			if(quotedBuffer != null)
    				quotedBuffer.append(token);
    			else
    				parts.add(token);
    		}
    	}
    	String[] partArray = new String[parts.size()];
    	return parts.toArray(partArray);
    }
    
    //Helpers
    public static void injectHelpers(Scope scope) {
    	JSObjectBase namespace = new JSObjectBase();
    	scope.set(MyGenerator.NS, namespace);
    	
    	namespace.set(MyGenerator.VAR_EXPAND, new JSFunctionCalls2() {
			@Override
			public Object call(Scope scope, Object varName, Object defaultValue, Object[] extra) {
				
				String varNameStr = ((JSString)varName).toString();
				String[] varNameParts = varNameStr.split("\\.");
				
				// find the starting point
				JSArray contextStack = (JSArray)scope.get(MyGenerator.CONTEXT_STACK_VAR);
				Object varValue = null;
				
				for(int i=contextStack.size() - 1; i>=0 && varValue == null; i--) {
					JSObject context = (JSObject)contextStack.get(i);
					varValue = context.get(varNameParts[0]);
				}
				// find the rest of the variable members
				for(int i=1; i<varNameParts.length; i++) {
					String varNamePart = varNameParts[i];

					if(varValue == null || !(varValue instanceof JSObject)) {
						varValue = null;
						break;
					}
					
					JSObject varValueJsObj = (JSObject)varValue;
					varValue = varValueJsObj.get(varNamePart);
					
					if(varValue instanceof JSFunction)
						varValue = ((JSFunction)varValue).callAndSetThis(scope.child(), varValueJsObj, new Object[0]);
				}
				if(varValue == null) {
					varValue = defaultValue;
				}

				return varValue;
			}
		});
		
		for (TagHandler tagHandler : _tagHandlers.values()) {
			for(Map.Entry<String, Object> entry : tagHandler.getHelpers().entrySet()) {
				namespace.set(entry.getKey(), entry.getValue());
			}
		}
    }
    
    //TagHandler Registration
    static HashMap<String, TagHandler> _tagHandlers = new HashMap<String, TagHandler>();
    static {
    	_tagHandlers.put("if", new IfTagHandler());
    	_tagHandlers.put("else", new ElseTagHandler());
    	_tagHandlers.put("endif", new IfEndTagHandler());
    	_tagHandlers.put("for", new ForLoopTagHandler());
    	_tagHandlers.put("endfor", new EndForLoopTag());
    	_tagHandlers.put("include", new IncludeTagHandler());
    }

    public class MyGenerator extends Generator {
    	public static final String CONTEXT_STACK_VAR = "obj";
    	public static final String NS = "_djang10Helper";
    	public static final String VAR_EXPAND = "djangoVarExpand";
    	
		public MyGenerator(State state) {
			super(state);
		}
    	public void appendVarExpansion(String varName, String defaultValue) {
    		append(NS);
    		append(".");
    		append(VAR_EXPAND);
    		append("(\"");
    		append(varName);
    		append("\",");
    		append(defaultValue);
    		append(")");
    	}
    	public void appendCallHelper(String name, String... params) {
    		appendHelper(name);
    		append("(");
    		boolean isFirst = true;
    		for(String param : params) {
    			if(!isFirst)
    				append(", ");
    			isFirst = false;
    			append(param);
    		}
    		append(")");
    	}
    	public void appendHelper(String name) {
    		append(NS);
    		append(".");
    		append(name);
    	}
    	public void appendCurrentContextVar(String name) {
    		append(CONTEXT_STACK_VAR);
    		append("[");
    		append(CONTEXT_STACK_VAR);
    		append(".length - 1].");
    		append(name);
    	}
    	public void appendPopContext() {
    		append(CONTEXT_STACK_VAR);
    		append(".pop();\n");
    	}
    }
}