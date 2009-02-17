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

import java.util.ArrayList;
import java.util.List;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSObjectSize;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;
import ed.log.Level;
import ed.log.Logger;
import ed.util.SeenPath;
import ed.util.Sizable;

public class FilterExpression extends JSObjectBase {
    private final Logger log;

    private Expression expression;
    private List<FilterSpec> filterSpecs;

    public FilterExpression(Parser parser, String filterExpression, Token token, boolean useLiteralEscapes) {
        setConstructor(CONSTRUCTOR);
        this.log = Logger.getRoot().getChild("djang10").getChild("FilterExpression");

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
    public Object resolve(Scope scope, Context context, Boolean ignore_failures) {
        Object value;

        try {
            value = expression.resolve(scope, context);
        }
        catch(VariableDoesNotExist e) {
            if(ignore_failures) {
                value = null;
            }
            else {
                if(!e.isSameAsJsNull()) {
                    log.debug(e.getMessage(), e);
                }

                JSHelper jsHelper = JSHelper.get(scope);
                value = jsHelper.fix_invalid_expression(expression);
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


    public long approxSize(SeenPath seen) {
        long sum = super.approxSize( seen );

        sum += JSObjectSize.size( log, seen, this );
        sum += JSObjectSize.size( expression, seen, this );
        sum += JSObjectSize.size( filterSpecs, seen, this );

        return sum;
    }

    public static JSFunction CONSTRUCTOR = new JSFunctionCalls2() {
        public Object call(Scope scope, Object parserObj, Object filterExpressionObj, Object[] extra) {
            throw new UnsupportedOperationException();
        }
        protected void init() {
            _prototype.set("resolve", new JSFunctionCalls2() {
                public Object call(Scope scope, Object contextObj, Object ignore_failuresObj, Object[] extra) {
                    FilterExpression thisObj = (FilterExpression)scope.getThis();
                    return thisObj.resolve(scope, (Context)contextObj, ignore_failuresObj == Boolean.TRUE);
                }
            });
        }
    };


    private static class FilterSpec implements Sizable {
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
                    new_obj = filter.call(scope, value, context.get("autoescape") != Boolean.FALSE, paramValue);
                else
                    new_obj = filter.call(scope, value, paramValue);
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

        public long approxSize(SeenPath seen) {
            long sum = JSObjectSize.OBJ_OVERHEAD;

            sum += JSObjectSize.size( filterName, seen, this );
            sum += JSObjectSize.size( filter, seen, this );
            sum += JSObjectSize.size( param, seen, this );

            return sum;
        }
    }
}
