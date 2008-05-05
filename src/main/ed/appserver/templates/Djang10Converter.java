// Djang10Converter.java

package ed.appserver.templates;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ed.appserver.templates.djang10.Node;
import ed.appserver.templates.djang10.Parser;
import ed.appserver.templates.djang10.generator.JSWriter;
import ed.appserver.templates.djang10.tagHandlers.ForTagHandler;
import ed.appserver.templates.djang10.tagHandlers.IfTagHandler;
import ed.appserver.templates.djang10.tagHandlers.IncludeTagHandler;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;


public class Djang10Converter implements TemplateConverter {
	public static final String extension = "djang10";
	public Djang10Converter(){

    }

	public Result convert(Template t) {
		if(!(extension).equals( t._extension))
			return null;
		
		Parser parser = new Parser(t._content);
		LinkedList<Node> nodeList = parser.parse();
		JSWriter writer = new JSWriter();
		
		writer.append("var "+JSWriter.CONTEXT_STACK_VAR+" = (arguments.length == 0)? [scope] : (arguments[0] instanceof Array)? arguments[0] : [arguments[0]];\n");
		for(Node node : nodeList) {
			node.getRenderJSFn(writer);
		}
		
		String newName = t.getName().replaceAll( "\\.("+extension+")+$" , "_$1.js" );
		return new Result(new Template(newName, writer.toString()), writer.getLineMap());
	}

    
    //Helpers
    public static void injectHelpers(Scope scope) {
    	JSObjectBase namespace = new JSObjectBase();
    	scope.set(JSWriter.NS, namespace);
    	
    	Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
    	
    	helpers.putAll(JSWriter.getHelpers());
    	
    	for (TagHandler tagHandler : _tagHandlers.values()) {
    		helpers.putAll(tagHandler.getHelpers());
		}
    	for(String name : helpers.keySet()) {
    		namespace.set(name, helpers.get(name));
    	}		
    }
    
    //TagHandler Registration
    public static Map<String, TagHandler> getTagHandlers() {
    	return _tagHandlers;
    }
    static HashMap<String, TagHandler> _tagHandlers = new HashMap<String, TagHandler>();
    static {
    	_tagHandlers.put("if", new IfTagHandler());
    	_tagHandlers.put("for", new ForTagHandler());
    	_tagHandlers.put("include", new IncludeTagHandler());
    }

}
