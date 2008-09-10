/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSException;
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

    public FilterExpression(Parser parser, String filterExpression, Token token, boolean useLiteralEscapes) {
        setConstructor(CONSTRUCTOR);

        String[] parts = Util.smart_split(filterExpression.trim(), new String[] {"|"}, true);

        this.expression = new Expression(parts[0], token, useLiteralEscapes);
        this.filterSpecs = new ArrayList<FilterSpec>();
        

        for (int i = 1; i < parts.length; i++) {
            String[] filterParts = parts[i].split(":", 2);
            String filterNamePart = filterParts[0].trim();
            String filterParamPart = null;

            if (filterParts.length > 1)
                filterParamPart = filterParts[1].trim();

            JSFunction filter = parser.find_filter(filterNamePart);

            Expression filterParam = (filterParamPart == null) ? null : new Expression(filterParamPart, token, useLiteralEscapes);
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
                JSHelper jsHelper = JSHelper.get(scope);
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
            throw new UnsupportedOperationException();
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
            Object new_obj;
            
            try {
                if(filter.get("needs_autoescape") == Boolean.TRUE)
                    new_obj = filter.call(scope.child(), value, context.get("autoescape") != Boolean.FALSE, paramValue);
                else
                    new_obj = filter.call(scope.child(), value, paramValue);
            }
            catch(JSException e) {
                throw JSHelper.unnestJSException(e);
            }
            
            if( (filter.get("is_safe") == Boolean.TRUE) && (JSHelper.is_safe(value) == Boolean.TRUE) && (new_obj instanceof JSObject) )
                new_obj = JSHelper.mark_safe((JSObject)new_obj);
            else if( (JSHelper.is_escape(value) == Boolean.TRUE) && (new_obj instanceof JSObject) )
                new_obj = JSHelper.mark_escape((JSObject)new_obj);
            
            return new_obj;
        }
        public String toString() {
            return filterName + ":" + String.valueOf(param);
        }
    }
}
