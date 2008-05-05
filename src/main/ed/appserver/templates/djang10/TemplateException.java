package ed.appserver.templates.djang10;

public class TemplateException extends RuntimeException {

	public TemplateException(String message) {
		super(message);
	}
	public TemplateException(int startLine, int endLine, String message) {
		super(message + ". Starting on Line: " + startLine + ", ending on Line: " + endLine);
		// TODO Auto-generated constructor stub
	}
	
}
