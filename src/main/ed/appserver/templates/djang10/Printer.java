package ed.appserver.templates.djang10;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public abstract class Printer extends JSFunctionCalls1 {
    public Printer() {
        JSHelper.mark_safe(this);
    }
    public Object call(Scope scope, Object p0, Object[] extra) {
        if((p0 != null) && (p0 instanceof JSObject) && (((JSObject)p0).get("toString") instanceof JSFunction)) {
            JSFunction toStringFn = (JSFunction)((JSObject)p0).get("toString");
            p0 = toStringFn.callAndSetThis(scope.child(), p0, new Object[0]);
        }
        
        boolean is_safe =  JSHelper.is_safe(p0);
        if(!is_safe)
            JSHelper.mark_escape(this);

        print(scope, p0);
        return null;
    }

    public boolean is_safe() {
        return JSHelper.is_safe(this);
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
            if(is_safe())
                str = (JSString)JSHelper.mark_safe(str);
            
            return str;
        }
    }
}
