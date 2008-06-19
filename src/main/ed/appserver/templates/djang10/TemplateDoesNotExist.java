package ed.appserver.templates.djang10;

public class TemplateDoesNotExist extends TemplateException {
    private String path;

    public TemplateDoesNotExist(String path) {
        super("Template doesn't exist: " + path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
