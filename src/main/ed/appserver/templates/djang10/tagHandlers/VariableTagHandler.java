package ed.appserver.templates.djang10.tagHandlers;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ScriptOrFnNode;

import ed.appserver.templates.Djang10Converter;
import ed.appserver.templates.djang10.Context;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.TemplateException;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.filters.Filter;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.js.func.JSFunctionCalls3;
import ed.log.Logger;

public class VariableTagHandler implements TagHandler {
	public final static String GET_PROP = "getProp";
	public final static String LOOKUP = "lookup";
	public final static String CALL = "call";
	public final static String APPLY_FILTER = "applyFilter";
	public final static String DEFAULT_VAR = "defaultValue";
	
	public final static Object UNDEFINED_VALUE = new Object();
	
	
	private final static Logger _log = Logger.getLogger( "djang10" );
	
	public Node compile(Parser parser, String command, Token token) throws TemplateException {
		try {
			String compiledExpression = compileFilterExpression(token.contents, "\"\"");
			return new VariableNode(token, compiledExpression);

		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, JSFunction> getHelpers() {
		HashMap<String, JSFunction> helpers = new HashMap<String, JSFunction>();
		
		helpers.put(GET_PROP, new JSFunctionCalls3() {
			public Object call(Scope scope, Object obj, Object prop, Object autoCall, Object[] extra) {
				if(UNDEFINED_VALUE == obj || UNDEFINED_VALUE == prop || obj == null || prop == null)
					return UNDEFINED_VALUE;
				
				Object ret;
				try {
					if(!(obj instanceof JSObject))
						throw new IllegalArgumentException("Can't get properties on non jsobjects");
					
					JSObject jsObj = (JSObject)obj;
					
					if(!jsObj.containsKey(prop.toString()))
						return UNDEFINED_VALUE;
					
					ret = jsObj.get(prop);
					
					if(autoCall == Boolean.TRUE && ret instanceof JSFunction && !(ret instanceof JSCompiledScript))
					    ret = ((JSFunction)ret).callAndSetThis(scope.child(), obj, null);
					
				} catch(Throwable t) {
					_log.error("Failed to get property: " + prop, t);
					ret = UNDEFINED_VALUE;
				}
				return ret;
			}
		});
		
		helpers.put(LOOKUP, new JSFunctionCalls2() {
			public Object call(Scope scope, Object name, Object autoCall, Object[] extra) {
				
				Object varValue;
				try {
					if(UNDEFINED_VALUE == name || name == null)
						return UNDEFINED_VALUE;
					
					if(!(name instanceof JSString))
						throw new IllegalArgumentException("Name is not a string");
					
					Context contextStack = (Context)scope.get(JSWriter.CONTEXT_STACK_VAR);
					varValue = contextStack.get(name);
					
					if(varValue == null)
						varValue = contextStack.containsKey(name.toString())? null : UNDEFINED_VALUE;
					
					//XXX: fallback on scope look ups
					if(varValue == UNDEFINED_VALUE) {
						varValue = scope.get(name);
						
						if(varValue == null) {
							varValue = scope.keySet().contains(name.toString())? null : UNDEFINED_VALUE;
						}
					}
					
					if(autoCall == Boolean.TRUE && varValue instanceof JSFunction && !(varValue instanceof JSCompiledScript))
					    varValue = ((JSFunction)varValue).call(scope.child());

				} catch(Throwable t) {
					_log.error("Failed to lookup object by name: " + name, t);
					varValue = UNDEFINED_VALUE;
				}
				return varValue;
			}
		});
		
		helpers.put(CALL, new JSFunctionCalls2() {
			@Override
			public Object call(Scope scope, Object thisObj, Object method, Object[] extra) {
				if(thisObj == UNDEFINED_VALUE || method == UNDEFINED_VALUE || method == null)
					return UNDEFINED_VALUE;
				
				Object ret;
				
				try {
					JSFunction func;
					if(thisObj == null) {
						if(!(method instanceof JSFunction))
							throw new IllegalArgumentException("when calling a global function, method must be a function");
	
						func = (JSFunction)method;
					}
					else {
						if(!(thisObj instanceof JSObject))
							throw new IllegalArgumentException("when calling a member function, object must be a JSObject");
						
						Object temp = ((JSObject)thisObj).get(method);
						
						if(!(temp instanceof JSFunction))
							throw new RuntimeException("Object doesn't contain a function by the name of " + method);
						
						func = (JSFunction)temp;
					}
					
					if(thisObj != null) {
						ret = func.callAndSetThis(scope.child(), thisObj, extra);
					}
					else {
						ret = func.call(scope.child(), extra);
					}
				} catch(Throwable t) {
					_log.error("Failed to call method", t);
					ret = UNDEFINED_VALUE;
				}
				return ret;
			}
		});
		
		helpers.put(APPLY_FILTER, new JSFunctionCalls3() {
			public Object call(Scope scope, Object value, Object filterName, Object param, Object[] extra) {
				Map<String, Filter> filterMap = Djang10Converter.getFilters();
				Object ret;
				
				try {
					Filter filter = filterMap.get(filterName.toString());
					if(filter == null)
						throw new RuntimeException("Filter not found: " + filterName);
					
					ret = filter.apply(value, param);
				} catch(Throwable t) {
					_log.error("Failed to apply filter: " + filterName, t);
					ret = UNDEFINED_VALUE;
				}
				
				return ret;
			}
		});
		
		helpers.put(DEFAULT_VAR, new JSFunctionCalls2() {
			public Object call(Scope scope, Object value, Object defaultValue, Object[] extra) {
				if(value == null || value == UNDEFINED_VALUE)
					return defaultValue;

				return value;
			}
		});
		
		return helpers;
	}

	public static String compileFilterExpression(String filterExpression, String defaultValue) throws TemplateException {
		return JSHelper.NS + "." + DEFAULT_VAR + "(" + compileFilterExpression(filterExpression) + ", " + defaultValue + ")";
	}
	
	public static String compileFilterExpression(String filterExpression) throws TemplateException {
		String[] parts = Parser.smartSplit(filterExpression.trim(), "|");
		
		//parse the expression part		
		String compiledExr = compileExpression(parts[0]);
		
		//ensure filters exist and rebuild the filter str;
		for(int i=1; i<parts.length; i++) {
			String[] filterParts = parts[i].split(":", 2);
			String filterName = filterParts[0].trim();
			String filterParam = null;

			if(filterParts.length > 1)
				filterParam = filterParts[1].trim();

			compiledExr = compileFilter(compiledExr, filterName, filterParam);
			
		}
		return compiledExr;
	}
	
	private static String compileFilter(String compiledExr, String filterName, String filterParam) throws TemplateException {
		Map<String, Filter> filterMap = Djang10Converter.getFilters();
		
		if(!filterMap.containsKey(filterName))
			throw new TemplateException("Unkown filter");
		
		String compiledParam = filterParam == null? "null" : compileExpression(filterParam);
		
		
		return JSHelper.NS + "." + APPLY_FILTER + "(" + compiledExr + ", \"" + filterName +"\", "+ compiledParam + ")"; 
		
	}

	public static String compileExpression(String expression) throws TemplateException{
		CompilerEnvirons ce  = new CompilerEnvirons();
		org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(ce, ce.getErrorReporter());
		ScriptOrFnNode scriptNode = parser.parse(expression, "foo", 0);
		
		if(scriptNode.getFirstChild() != scriptNode.getLastChild())
			throw new TemplateException("Only one expression is allowed");
		
		org.mozilla.javascript.Node exprNode = scriptNode.getFirstChild();
		
		if(exprNode.getType() != org.mozilla.javascript.Token.EXPR_RESULT)
			throw new TemplateException("Not an expression");
		
		return compileNode(exprNode.getFirstChild(), true);
	}
	
	private static String compileNode(org.mozilla.javascript.Node node, boolean autoCall) throws TemplateException{
		StringBuilder buffer = new StringBuilder();
		
		switch(node.getType()) {				
			case org.mozilla.javascript.Token.GETELEM:
			case org.mozilla.javascript.Token.GETPROP:
				buffer.append(JSHelper.NS + "." + GET_PROP + "(");
				buffer.append(compileNode(node.getFirstChild(), true));
				buffer.append(", ");
				buffer.append(compileNode(node.getLastChild(), autoCall));
				buffer.append(", " + autoCall +  ")");
				break;
			
			case org.mozilla.javascript.Token.CALL:
				org.mozilla.javascript.Node child = node.getFirstChild();
				
				String compiledThis;
				String compiledMethod;
				if(child.getType() == org.mozilla.javascript.Token.GETELEM || child.getType() == org.mozilla.javascript.Token.GETPROP) {
					compiledThis = compileNode(child.getFirstChild(), true);
					compiledMethod = compileNode(child.getLastChild(), false);
				}
				else {
					compiledThis = "null";
					compiledMethod = compileNode(child, false);
				}
				
				
				buffer.append(JSHelper.NS + "." + CALL + "(");
				buffer.append(compiledThis + ", " + compiledMethod);
				
				child = child.getNext();
				while(child != null) {
					buffer.append(", ");
					buffer.append(compileNode(child, true));
					
					child = child.getNext();
				}
				buffer.append(")");
				break;
	
			case org.mozilla.javascript.Token.NAME:
				buffer.append(JSHelper.NS + "." + LOOKUP + "(\"" + node.getString() + "\", "+ autoCall +")");
				break;
				
			case org.mozilla.javascript.Token.STRING:
				buffer.append("\"" + node.getString() + "\"");
				break;
			
			case org.mozilla.javascript.Token.NUMBER:
				buffer.append(node.getDouble());
				break;
		    
			case org.mozilla.javascript.Token.NULL:
			    buffer.append("null");
			    break;

			case org.mozilla.javascript.Token.TRUE:
			    buffer.append("true");
			    break;
		    
			case org.mozilla.javascript.Token.FALSE:
			    buffer.append("false");
			    break;

			default:
				throw new TemplateException("Invalid token");
		}
 		return buffer.toString();
	}
	
	private static class VariableNode extends Node {
		private final String compiledString;
		
		public VariableNode(Token token, String compiledString) {
			super(token);
			this.compiledString = compiledString;
		}

		public void getRenderJSFn(JSWriter preamble, JSWriter buffer) throws TemplateException {
			buffer.append(startLine, "print(" + compiledString + ");\n");
		}
	}
}
