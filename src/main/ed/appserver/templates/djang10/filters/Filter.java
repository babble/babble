package ed.appserver.templates.djang10.filters;

import ed.appserver.templates.djang10.TemplateException;

public interface Filter {
    public String toJavascript(String filterName, String compiledValue, String compiledParam) throws TemplateException;
}
