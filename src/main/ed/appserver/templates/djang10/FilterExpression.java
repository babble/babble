package ed.appserver.templates.djang10;

public class FilterExpression {
    private final Expression expression;
    private final FilterList filters;

    public FilterExpression(Parser parser, String filterExpression) throws TemplateException {
        String[] parts = Parser.smartSplit(filterExpression.trim(), "|", 2);

        this.expression = new Expression(parts[0]);
        this.filters = new FilterList(parser, (parts.length == 1) ? "" : parts[1]);
    }

    public FilterExpression(Parser parser, Expression expression, FilterList filters) throws TemplateException {

        this.expression = expression;
        this.filters = filters;
    }

    public String toJavascript() throws TemplateException {
        return filters.toJavascript(expression);
    }
}