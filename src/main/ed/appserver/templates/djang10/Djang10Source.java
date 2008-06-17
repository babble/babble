package ed.appserver.templates.djang10;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.appserver.jxp.JxpSource.JxpFileSource;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.util.Pair;

public class Djang10Source extends JxpFileSource {
    private Djang10CompiledScript compiledScript;

    public Djang10Source(File f) {
        super(f);
        compiledScript = null;
    }

    public synchronized JSFunction getFunction() throws IOException {
        if(_needsParsing() || compiledScript == null) {
          
            _lastParse = Calendar.getInstance().getTimeInMillis();
            _dependencies.clear();
            
            NodeList nodes = null;
            Collection<Library> libraries;
            
            String contents = getContent();
            
            try {
                Parser parser = new Parser(contents);
                JSHelper jsHelper = (JSHelper)Scope.getThreadLocal().get(JSHelper.NS);
                for(Pair<JxpSource,Library> lib : jsHelper.getDefaultLibraries())
                    parser.add_library(lib.first, lib.second);
                
                nodes = parser.parse(Scope.getThreadLocal(), new JSArray());
                libraries = parser.getLoadedLibraries();
                
                _dependencies.addAll(parser.get_dependencies());
                
            } catch(TemplateException e) {
                //FIXME: TemplateException should be subclass runtime exception
                throw new RuntimeException(e);
            }
            compiledScript = new Djang10CompiledScript(nodes, libraries);
            compiledScript.set(JxpSource.JXP_SOURCE_PROP, this);            
        }

        return compiledScript;
    }

    public NodeList compile(Scope scope) throws TemplateException {
        String contents;

        try {
            contents = getContent();
        } catch (IOException e) {
            throw new TemplateException("Failed to read the template source", e);
        }

        Parser parser = new Parser(contents);
        JSHelper jsHelper = (JSHelper)scope.get(JSHelper.NS);
        for(Pair<JxpSource,Library> lib : jsHelper.getDefaultLibraries())
            parser.add_library(lib.first, lib.second);

        return parser.parse(scope, new JSArray());
    }
    
    public static JSHelper install(Scope scope) {
        JSHelper jsHelper = JSHelper.install(scope);
        
        JSFileLibrary js = JSFileLibrary.loadLibraryFromEd("ed/appserver/templates/djang10/js", "djang10", scope);
        
        for(String jsFile : new String[] {"defaulttags", "loader_tags", "defaultfilters", "tengen_extras"}) {
            JSFunction jsFileFn = (JSFunction)js.get(jsFile);
            Scope child = scope.child();
            child.setGlobal(true);
            jsFileFn.call(child);
            Library lib = (Library)child.get("register");
            jsHelper.addDefaultLibrary((JxpSource)jsFileFn.get(JxpSource.JXP_SOURCE_PROP), lib);
        }
        
        return jsHelper;
    }
}
