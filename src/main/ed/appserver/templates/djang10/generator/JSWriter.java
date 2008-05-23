package ed.appserver.templates.djang10.generator;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Util;


public class JSWriter {
	public static final String CONTEXT_STACK_VAR = "obj";
	public static final String RENDER_OPTIONS_VAR = "renderOpts";
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
		append(srcLine, JSHelper.NS + "." + name);
	}
	
	public void appendVarExpansion(int srcLine, String varName, String defaultValue) {
		appendVarExpansion(srcLine, varName, defaultValue, false, true);
	}
	public void appendVarExpansion(int srcLine, String varName, String defaultValue, boolean allowGlobal, boolean callLeaf) {
		appendHelper(srcLine, JSHelper.VAR_EXPAND);
		append("(\"");
		append(varName.replace("\"", "\\\""));
		append("\",");
		append(defaultValue);
		append(", ");
		append(Boolean.toString(allowGlobal));
		append(", ");
		append(Boolean.toString(callLeaf));
		append(")");
	}
	public void appendCurrentContextVar(int srcLine, String name) {
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, "[");
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, ".length - 1]");
		name = name.replace("\"", "\\\"");
		append(srcLine, "[\""+name+"\"]");
	}
	public void appendPopContext(int srcLine) {
		append(srcLine, CONTEXT_STACK_VAR);
		append(srcLine, ".pop();\n");
	}
	public void appendPushContext(int srcLine) {
		append(srcLine, CONTEXT_STACK_VAR + ".push({});\n");
	}
	public Map<Integer, Integer> getLineMap() {
		return lineMap;
	}
	
	
	public int getLineCount() {
		return currentLine;
	}
	
	@Override
	public String toString() {
		return buffer.toString();
	}
}
