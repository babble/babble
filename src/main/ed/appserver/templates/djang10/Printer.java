package ed.appserver.templates.djang10;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public abstract class Printer extends JSFunctionCalls1 {
    private Boolean is_safe;
    
    public Printer() {
        this.is_safe = null;
    }
    public Object call(Scope scope, Object arg, Object[] extra) {
        
        if((arg != null) && (arg instanceof JSObject) && (((JSObject)arg).get("toString") instanceof JSFunction)) {
            JSFunction toStringFn = (JSFunction)((JSObject)arg).get("toString");
            arg = toStringFn.callAndSetThis(scope.child(), arg, new Object[0]);
        }

        Boolean arg_safety = JSHelper.is_safe(arg);

        if(arg_safety == Boolean.FALSE)
            this.is_safe = false;
        else if(this.is_safe != Boolean.FALSE && arg_safety != null)
            this.is_safe = arg_safety;
        print(scope, arg);

        return null;
    }

    public Boolean is_safe() {
        return is_safe;
    }
    
    protected abstract void print(Scope scope, Object obj);



    public static class DelegatingPrinter extends Printer {
        private final JSFunction inner;
        
        public DelegatingPrinter(JSFunction inner) {
            this.inner = inner;
        }

        protected void print(Scope scope, Object obj) {
            inner.call(scope.child(), obj);
        }
    }


    public static class RedirectedPrinter extends Printer {
        private final StringBuilder buffer;
        
        public RedirectedPrinter() {
            buffer = new StringBuilder();
        }
        protected void print(Scope scope, Object obj) {
            buffer.append(obj);            
        }
        public JSString getJSString() {
            JSString str = new JSString(buffer.toString());
            if(is_safe() == Boolean.TRUE)
                str = (JSString)JSHelper.mark_safe(str);
            else if(is_safe() == Boolean.FALSE)
                str = (JSString)JSHelper.mark_escape(str);
            
            return str;
        }
    }
}
