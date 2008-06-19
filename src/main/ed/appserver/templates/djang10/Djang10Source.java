package ed.appserver.templates.djang10;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
          
            _lastParse = lastUpdated();
            _dependencies.clear();
            
            NodeList nodes = null;
            Collection<Library> libraries;
            
            String contents = getContent();
            

            Parser parser = new Parser(contents);
            JSHelper jsHelper = JSHelper.get(Scope.getThreadLocal());
            for(Pair<JxpSource,Library> lib : jsHelper.getDefaultLibraries()) {
                parser.add_dependency(lib.first);
                parser.add_library(lib.second);
            }
            
            nodes = parser.parse(Scope.getThreadLocal(), new JSArray());
            libraries = parser.getLoadedLibraries();
            
            _dependencies.addAll(parser.get_dependencies());
                
            compiledScript = new Djang10CompiledScript(nodes, libraries);
            compiledScript.set(JxpSource.JXP_SOURCE_PROP, this);            
        }

        return compiledScript;
    }

    public NodeList compile(Scope scope) {
        String contents;

        try {
            contents = getContent();
        } catch (IOException e) {
            throw new TemplateException("Failed to read the template source from: " + getFile(), e);
        }

        Parser parser = new Parser(contents);
        JSHelper jsHelper = JSHelper.get(scope);
        for(Pair<JxpSource,Library> lib : jsHelper.getDefaultLibraries()) {
            parser.add_dependency(lib.first);
            parser.add_library(lib.second);
        }

        return parser.parse(scope, new JSArray());
    }
    
    public static JSHelper install(Scope scope) {
        JSHelper jsHelper = JSHelper.install(scope);
        
        JSFileLibrary js = JSFileLibrary.loadLibraryFromEd("ed/appserver/templates/djang10/js", "djang10", scope);
        
        for(String jsFile : new String[] {"defaulttags", "loader_tags", "defaultfilters", "tengen_extras"}) {
            JSFunction jsFileFn = (JSFunction)js.get(jsFile);
            Library lib = (Library) jsHelper.evalLibrary.call(scope, jsFileFn);

            jsHelper.addDefaultLibrary((JxpSource)jsFileFn.get(JxpSource.JXP_SOURCE_PROP), lib);
        }
        
        return jsHelper;
    }
}
