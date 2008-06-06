package ed.appserver.templates.djang10.filters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ed.appserver.templates.djang10.Expression;
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.TemplateException;
import ed.js.JSFunction;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls3;

public abstract class JavaFilter implements Filter {
    public static final String APPLY_FILTER = "applyFilter";

    public static final Map<String, JavaFilter> filters;
    static {
        HashMap<String, JavaFilter> _filters = new HashMap<String, JavaFilter>();
        filters = Collections.unmodifiableMap(_filters);

        _filters.put("default", new DefaultFilter());
        _filters.put("urlencode", new UrlEncodeFilter());
        _filters.put("escape", new EscapeFilter());
        _filters.put("date", new DateFilter());
        _filters.put("upper", new UpperFilter());
        _filters.put("lower", new LowerFilter());
        _filters.put("dictsort", new DictSortFilter(false));
        _filters.put("dictsortreverse", new DictSortFilter(true));
        _filters.put("length_is", new LengthIsFilter());
        _filters.put("length", new LengthFilter());
    }

    public static Map<String, JSFunction> getHelpers() {
        HashMap<String, JSFunction> helpers = new HashMap<String, JSFunction>();

        helpers.put(APPLY_FILTER, new JSFunctionCalls3() {
            public Object call(Scope scope, Object value, Object filterName, Object param, Object[] extra) {

                return applyFilter(value, ((JSString) filterName).toString(), param);
            }
        });

        return helpers;
    }

    public static Object applyFilter(Object value, String filterName, Object param) {
        try {
            JavaFilter filter = filters.get(filterName);
            if (filter == null)
                throw new RuntimeException("Filter not found: " + filterName);

            return filter.apply(value, param);
        } catch (Throwable t) {
            return Expression.UNDEFINED_VALUE;
        }
    }

    public abstract Object apply(Object value, Object param);

    public String toJavascript(String filterName, String compiledValue, String compiledParam) throws TemplateException {
        return JSHelper.NS + "." + APPLY_FILTER + "(" + compiledValue + ", \"" + filterName + "\", " + compiledParam + ")";
    }
}
