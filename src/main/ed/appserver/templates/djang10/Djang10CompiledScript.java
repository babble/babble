package ed.appserver.templates.djang10;

import java.util.Collection;

import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class Djang10CompiledScript extends JSFunctionCalls1 {
    private final NodeList nodes;
    
    
    public Djang10CompiledScript(NodeList nodes, Collection<Library> loadedLibraries) {
        super();
        this.nodes = nodes;
        
        JSArray arr = new JSArray();
        arr.addAll(loadedLibraries);
        set("loadedLibraries", new JSArray( arr));
        
        set("nodelist", nodes);
    }


    public Object call(Scope scope, Object contextObj, Object[] extra) {
        Context context;
        
        if(contextObj instanceof Context)
            context = (Context)contextObj;
        else if(contextObj instanceof JSObject)
            context = new Context((JSObject)contextObj);
        else
            context = new Context(scope);
        
        nodes.__render(scope, context, (JSFunction)scope.get("print"));
        return null;
    }
}
