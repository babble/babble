package ed.appserver.templates.djang10;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls2;

public class Library extends JSObjectBase {
    public Library() {
        setConstructor(CONSTRUCTOR);

        setFilters(new JSObjectBase());
        setTags(new JSObjectBase());
    }

    public static final JSFunction CONSTRUCTOR = new JSFunctionCalls0() {
        public JSObject newOne() {
            return new Library();
        }

        public Object call(Scope scope, Object[] extra) {
            return null;
        }

        protected void init() {
            _prototype.set("filter", new JSFunctionCalls2() {
                @Override
                public Object call(Scope scope, Object nameObj, Object filterFuncObj, Object[] extra) {
                    String name = ((JSString) nameObj).toString();
                    JSFunction filterFunc = (JSFunction) filterFuncObj;

                    return ((Library) scope.getThis()).filter(name, filterFunc);
                }
            });

            _prototype.set("tag", new JSFunctionCalls2() {
                @Override
                public Object call(Scope scope, Object nameObj, Object tagFuncObj, Object[] extra) {
                    String name = ((JSString) nameObj).toString();
                    JSFunction tagFunc = (JSFunction) tagFuncObj;

                    return ((Library) scope.getThis()).tag(name, tagFunc);
                }
            });
        }
    };

    private void setFilters(JSObject filterDict) {
        set("filters", filterDict);
    }

    public JSObject getFilters() {
        return (JSObject) get("filters");
    }

    private void setTags(JSObject filterDict) {
        set("tags", filterDict);
    }

    public JSObject getTags() {
        return (JSObject) get("tags");
    }

    public JSFunction filter(String name, JSFunction filter) {
        getFilters().set(name, filter);

        return filter;
    }

    public JSFunction tag(String name, JSFunction tagFunc) {
        getTags().set(name, tagFunc);

        return tagFunc;
    }
}
