/**
 * 
 */
package ed.appserver.templates.djang10;

import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.Djang10Converter.MyGenerator;
import ed.js.JSArray;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;

public class ForLoopTagHandler extends TagHandler {
	public void Compile(MyGenerator g, String name, String... params) {
		if(!"in".equals(params[1]))
			throw new RuntimeException();
		
		String itemName = params[0];
		String listName = params[2];
		boolean isReversed = params.length > 3? Boolean.parseBoolean(params[3]) : false;
		
		g.appendHelper("newForLoopObjFn");
		g.append("(");
		g.appendVarExpansion(listName, "[]");
		g.append(",");
		g.append("" + isReversed);
		g.append(");\n");
		
		g.append("while(");
		g.appendVarExpansion("forloop.revcounter", "null");
		g.append(" > 0) {\n");
		
		g.appendCurrentContextVar(itemName);
		g.append("=");
		g.appendVarExpansion(listName, "[]");
		g.append("[");
		g.appendVarExpansion("forloop.i", "null");
		g.append("];\n");
	}
	@Override
	public Map<String, Object> getHelpers() {
		return _helpers;
	}
	
	private static Map<String, Object> _helpers = new HashMap<String, Object>();
	static {
		_helpers.put("newForLoopObjFn", new JSFunctionCalls2() {
			@Override
			public Object call(Scope scope, Object array, Object isReversedObj, Object[] extra) {
				
				JSArray contextStack = (JSArray)scope.get(MyGenerator.CONTEXT_STACK_VAR);
				Object parentForLoop = null;
				for(int i=contextStack.size() - 1; i >= 0 && parentForLoop == null; i--)
					parentForLoop = ((JSObject)contextStack.get(i)).get("forloop");
				
				int length = (Integer)(((JSObject)array).get("length"));
				boolean isReversed = isReversedObj instanceof Boolean ? ((Boolean)isReversedObj) : false;
				
				JSObjectBase newContext = new JSObjectBase();
				ForLoopTagHandler.ForLoopObj forLoopObj = new ForLoopObj(parentForLoop, length, isReversed);  
				newContext.set("forloop", forLoopObj);
				
				contextStack.add(newContext);

				return forLoopObj;
			}
		});
	}
	
    private static class ForLoopObj extends JSObjectBase {
		private int i, length;
		private boolean isReversed;
		
		public ForLoopObj(Object parent, int length, boolean isReversed) {
			this.length = length;
			this.isReversed = isReversed;
			
			i = isReversed? length : -1;
			set("parent", parent);
			moveNext();
		}

		public void moveNext() {
			i += isReversed? -1 : 1;
			
			int counter0 = isReversed? length - i - 1 : i;
			int revcounter0 = isReversed? i : length - i - 1;
			
			set("i", i);
			set("counter0", counter0);
			set("counter", counter0 + 1);
			set("revcounter0", revcounter0);
			set("revcounter", revcounter0 + 1);
			set("first", counter0 == 0);
			set("last", revcounter0 == 0);
		}
	}
}