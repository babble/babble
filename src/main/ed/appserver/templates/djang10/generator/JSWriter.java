package ed.appserver.templates.djang10.generator;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Util;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;


public class JSWriter {
	public static final String CONTEXT_STACK_VAR = "obj";
	public static final String RENDER_OPTIONS_VAR = "renderOpts";
	public static final String NS = "_djang10Helper";
	public static final String VAR_EXPAND = "djangoVarExpand";
	
	private final StringBuilder buffer;
	private final Map<Integer, Integer> lineMap;
	private int currentLine;
	
	public JSWriter() {
		buffer = new StringBuilder();
		lineMap = new HashMap<Integer, Integer>();
		currentLine = 1;
	}
	
	public void append(String code) {
		currentLine += Util.countOccurance(code, '\n');
		
		buffer.append(code);
	}
	
	public void append(int srcLine, String code) {
		int startOutput = currentLine;
		
		append(code);
		
		int endOutput = currentLine + (code.endsWith("\n")? 0 : 1);
		
		for(int i=startOutput; i<endOutput; i++)
			lineMap.put(i, srcLine);
	}
	
	public void appendHelper(int srcLine, String name) {
		append(srcLine, NS + "." + name);
	}
	
	public void appendVarExpansion(int srcLine, String varName, String defaultValue) {
		appendHelper(srcLine, VAR_EXPAND);
		append("(\"");
		append(varName);
		append("\",");
		append(defaultValue);
		append(")");
	}
	public void appendCurrentContextVar(int srcLine, String name) {
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, "[");
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, ".length - 1].");
		append(name);
	}
	public void appendPopContext(int srcLine) {
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, ".pop();\n");
	}
	public Map<Integer, Integer> getLineMap() {
		return lineMap;
	}
	
	@Override
	public String toString() {
		return buffer.toString();
	}
	
	public static Map<String, JSFunction> getHelpers() {
		HashMap<String, JSFunction> helpers = new HashMap<String, JSFunction>();
		
    	helpers.put(VAR_EXPAND, new JSFunctionCalls2() {
			@Override
			public Object call(Scope scope, Object varName, Object defaultValue, Object[] extra) {
				
				String varNameStr = ((JSString)varName).toString();
				String[] varNameParts = varNameStr.split("\\.");
				
				// find the starting point
				JSArray contextStack = (JSArray)scope.get(CONTEXT_STACK_VAR);
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
    	return helpers;
    }
}
