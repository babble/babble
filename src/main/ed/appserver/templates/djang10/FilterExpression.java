package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;

import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;

public class FilterExpression extends JSObjectBase {
    private Expression expression;
    private List<FilterSpec> filterSpecs;

    protected FilterExpression() {
        setConstructor(CONSTRUCTOR);
    }
    public FilterExpression(Parser parser, String filterExpression) throws TemplateException {
        this();
        init(parser, filterExpression);
    }
    private void init(Parser parser, String filterExpression) throws TemplateException {
        String[] parts = Parser.smartSplit(filterExpression.trim(), "|");

        this.expression = new Expression(parts[0]);
        this.filterSpecs = new ArrayList<FilterSpec>();
        

        for (int i = 1; i < parts.length; i++) {
            String[] filterParts = parts[i].split(":", 2);
            String filterNamePart = filterParts[0].trim();
            String filterParamPart = null;

            if (filterParts.length > 1)
                filterParamPart = filterParts[1].trim();

            JSFunction filter = parser.getFilters().get(filterNamePart);
            if(filter == null)
                throw new TemplateException("Unknown filter: " + filterNamePart);

            Expression filterParam = (filterParamPart == null) ? null : new Expression(filterParamPart);
            filterSpecs.add(new FilterSpec(filterNamePart, filter, filterParam));
        }

    }

    public boolean is_literal() {
        if(!this.expression.is_literal())
            return false;
        
        for(FilterSpec filterSpec : filterSpecs) {
            if(filterSpec.param != null && !filterSpec.param.is_literal())
                return false;
        }
        return true;
    }
    
    public Object get_literal_value() {
        if(!is_literal())
            throw new IllegalStateException();
        
        return resolve(null, null);
    }
    
    public Object resolve(Scope scope, Context context) {
        return resolve(scope, context, false);
    }
    public Object resolve(Scope scope, Context context, boolean ignore_failures) {
        Object value = expression.resolve(scope, context);
        
        if(value == Expression.UNDEFINED_VALUE) {
            if(ignore_failures) {
                value = null;
            }
            else {
                JSHelper jsHelper = (JSHelper)scope.get(JSHelper.NS);
                JSString invalid_var_format_string = (JSString)jsHelper.get("TEMPLATE_STRING_IF_INVALID");
                if(invalid_var_format_string != null && !invalid_var_format_string.equals("")) {
                    return new JSString(invalid_var_format_string.toString().replace("%s", expression.toString()));
                }
                value = invalid_var_format_string;
            }
        }
        
        for(FilterSpec filterSpec : filterSpecs) {
            value = filterSpec.apply(scope, context, value);
        }
        
        return value;
    }
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(expression.toString());
        for(FilterSpec filter : filterSpecs)
            str.append("|" + filter.toString());
        return str.toString();
    }
    
    public static JSFunction CONSTRUCTOR = new JSFunctionCalls2() {
        public Object call(Scope scope, Object parserObj, Object filterExpressionObj, Object[] extra) {
            FilterExpression thisObj = (FilterExpression)scope.getThis();
            
            try {
                thisObj.init((Parser)parserObj, filterExpressionObj.toString());
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
            
            return null;
        }
        public JSObject newOne() {
            return new FilterExpression();
        }
        protected void init() {
            _prototype.set("resolve", new JSFunctionCalls1() {
                public Object call(Scope scope, Object contextObj, Object[] extra) {
                    FilterExpression thisObj = (FilterExpression)scope.getThis();
                    return thisObj.resolve(scope, (Context)contextObj);
                }
            });
        }
    };
    
    
    private static class FilterSpec {
        public final String filterName;
        public final JSFunction filter;
        public final Expression param;

        public FilterSpec(String filterName, JSFunction filter, Expression param) {
            this.filterName = filterName;
            this.filter = filter;
            this.param = param;
        }
        
        public Object apply(Scope scope, Context context, Object value) {
            Object paramValue = (param == null)? null : param.resolve(scope, context);
            return filter.call(scope.child(), value, paramValue);
        }
        public String toString() {
            return filterName + ":" + String.valueOf(param);
        }
    }
}