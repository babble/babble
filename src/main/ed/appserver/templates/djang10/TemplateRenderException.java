package ed.appserver.templates.djang10;

import java.util.Stack;

import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSObject;

public class TemplateRenderException extends Djang10Exception {
    public TemplateRenderException(Context context, Exception e) {
        super("Failed to render: " + context.getRenderStack().peek(), e);
        
        Stack<JSObject> renderedNodes = context.getRenderStack();
        
        StackTraceElement[] trace = new StackTraceElement[renderedNodes.size()];
        int i = trace.length - 1;
        for(JSObject node : renderedNodes) {
            String repr = node.toString();
            Token token = (Token)node.get("__token");
            
            trace[i--] = new StackTraceElement("", repr, token.getOrigin(), token.getStartLine());
        }
        
        setStackTrace(trace);
    }
}
