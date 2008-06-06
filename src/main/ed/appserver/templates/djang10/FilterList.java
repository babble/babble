package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;

import ed.appserver.templates.djang10.filters.Filter;

public class FilterList {
    private final List<FilterSpec> filters;

    public FilterList(Parser parser, String delimitedStr) throws TemplateException {
        filters = new ArrayList<FilterSpec>();

        String[] parts = Parser.smartSplit(delimitedStr.trim(), "|", -1);

        if (delimitedStr != null && delimitedStr.trim().length() > 0) {
            for (int i = 0; i < parts.length; i++) {
                String[] filterParts = parts[i].split(":", 2);
                String filterNamePart = filterParts[0].trim();
                String filterParamPart = null;

                if (filterParts.length > 1)
                    filterParamPart = filterParts[1].trim();

                Filter filter = parser.getFilters().get(filterNamePart);
                Expression filterParam = (filterParamPart == null) ? null : new Expression(filterParamPart);
                filters.add(new FilterSpec(filterNamePart, filter, filterParam));
            }
        }
    }

    public String toJavascript(Expression expression) throws TemplateException {
        return toJavascript(expression.toJavascript());
    }

    public String toJavascript(String expression) throws TemplateException {
        for (FilterSpec filterSpec : filters) {
            String param = (filterSpec.param == null) ? null : filterSpec.param.toJavascript();
            expression = filterSpec.filter.toJavascript(filterSpec.filterName, expression, param);
        }

        return expression;
    }

    private static class FilterSpec {
        public final String filterName;
        public final Filter filter;
        public final Expression param;

        public FilterSpec(String filterName, Filter filter, Expression param) {
            this.filterName = filterName;
            this.filter = filter;
            this.param = param;
        }
    }
}
