package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class Context extends JSObjectBase {
	public static final String PUSH = "__push";
	public static final String POP = "__pop";
	
	public final static JSFunction CONSTRUCTOR = new JSFunctionCalls1() {
		@Override
		public JSObject newOne() {
			return new Context();
		}
		@Override
		public Object call(Scope scope, Object p0, Object[] extra) {
			if(p0 != null)
				((Context)scope.getThis()).rebase((JSObject)p0);
			
			return null;
		}
		
		@Override
		protected void init() {
			
			_prototype.set(PUSH, new JSFunctionCalls0() {
				@Override
				public Object call(Scope scope, Object[] extra) {
					((Context)scope.getThis()).push();
					return null;
				}
			});
			_prototype.set(POP, new JSFunctionCalls0() {
				@Override
				public Object call(Scope scope, Object[] extra) {
					((Context)scope.getThis()).pop();
					return null;
				}
			});
		}	
	};

	private static final String[] SPECIAL_PROPS = new String[] {"prototype", "__proto__", "constructor",  "__constructor__", "__parent__"};
	private static final List<String> SPECIAL_PROPS_LIST;
	static {
		Arrays.sort(SPECIAL_PROPS);
		SPECIAL_PROPS_LIST = Arrays.asList(SPECIAL_PROPS);
	}
	
	private final LinkedList<JSObject> objectStack;
	
	private Context() {		
		objectStack = new LinkedList<JSObject>();
		objectStack.add(new JSObjectBase());
		
		setConstructor(CONSTRUCTOR);
	}
	
	private void rebase(JSObject obj) {
		this.objectStack.clear();
		this.objectStack.addFirst(obj);
		
	}
	@Override
	public boolean containsKey(String s) {
		if(SPECIAL_PROPS_LIST.contains(s))
			return super.containsKey(s);

		for(JSObject obj : objectStack) {
			if(obj.containsKey(s))
				return true;
		}
		return super.containsKey(s);
	}
	@Override
	public Object _simpleGet(String s) {
		if(SPECIAL_PROPS_LIST.contains(s))
			return super._simpleGet(s);

		for(JSObject obj : objectStack) {
			if((obj instanceof Scope) || obj.containsKey(s))
				return obj.get(s);
		}
		return super._simpleGet(s);
	}

	@Override
	public Collection<String> keySet() {
		ArrayList<String> compositeKeySet = new ArrayList<String>();
		
		for(JSObject obj : objectStack) {
			compositeKeySet.addAll(obj.keySet());
		}
		compositeKeySet.addAll(super.keySet());

		return compositeKeySet;
	}
	
	@Override
	public Object removeField(Object n) {
		if(SPECIAL_PROPS_LIST.contains(n))
			return super.removeField(n);

		for(JSObject obj : objectStack) {
			if(obj.containsKey(n.toString())) {
				return obj.removeField(n);
			}
		}
		return super.removeField(n);
	}

	@Override
	public Object set(Object n, Object v) {
		if(SPECIAL_PROPS_LIST.contains(n))
			return super.set(n, v);

		return objectStack.getFirst().set(n, v);
	}
	
	public void push() {
		objectStack.addFirst(new JSObjectBase());
	}
	public void pop() {
		if(objectStack.size() <= 1)
			throw new IllegalStateException("Can't remove the last backing object");
		objectStack.remove();
	}	
}
