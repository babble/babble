// Printer.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.appserver.templates.djang10;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectSize;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.util.SeenPath;

public abstract class Printer extends JSFunctionCalls1 {
    private Boolean is_safe;

    public Printer() {
        this.is_safe = null;
    }
    public Object call(Scope scope, Object arg, Object[] extra) {

        if((arg != null) && (arg instanceof JSObject) && (((JSObject)arg).get("toString") instanceof JSFunction) && !(arg instanceof JSString)) {
            JSFunction toStringFn = (JSFunction)((JSObject)arg).get("toString");
            arg = toStringFn.callAndSetThis(scope, arg, new Object[0]);
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


    public long approxSize(SeenPath seen) {
        long sum = super.approxSize( seen );

        sum += JSObjectSize.size( is_safe, seen, this );

        return sum;
    }


    public static class DelegatingPrinter extends Printer {
        private final JSFunction inner;

        public DelegatingPrinter(JSFunction inner) {
            this.inner = inner;
        }

        protected void print(Scope scope, Object obj) {
            inner.call(scope, obj);
        }

        public long approxSize(SeenPath seen) {
            long sum = super.approxSize( seen );

            sum += JSObjectSize.size( inner, seen, this );

            return sum;
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
        public long approxSize(SeenPath seen) {
            long sum = super.approxSize( seen );

            if( seen.shouldVisit( buffer , this ) );
                sum += JSObjectSize.OBJ_OVERHEAD + (2*buffer.capacity());

            return sum;
        }
    }
}
