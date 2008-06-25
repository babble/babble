package ed.appserver.templates.djang10;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.io.StreamUtil;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.util.Dependency;
import ed.util.Pair;

public class Djang10Source extends JxpSource {
    private final Djang10Content content;
    private Djang10CompiledScript compiledScript;

    public Djang10Source(File f) {
        content = new Djang10File(f);
        compiledScript = null;
    }
    public Djang10Source(String content) {
        this.content = new DJang10String(content);
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
    
    public File getFile() {
        return (content instanceof Djang10File)? ((Djang10File)content).getFile() : null;
    }
    public long lastUpdated(Set<Dependency> visitedDeps) {
        visitedDeps.add(this);

        long lastUpdated = content.lastUpdated();
        for(Dependency dep : _dependencies)
            if(!visitedDeps.contains(dep))
                lastUpdated = Math.max(lastUpdated, dep.lastUpdated(visitedDeps));
        
        return lastUpdated;
    }
    protected String getContent() throws IOException {
        return content.getContent();
    }
    protected InputStream getInputStream() throws IOException {
        return content.getInputStream();
    }
    protected String getName() {
        return content.getName();
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
    
    private static interface Djang10Content {
        public String getContent() throws IOException;
        public InputStream getInputStream() throws IOException;
        public long lastUpdated();
        public String getName();
    }
    private static class Djang10File implements Djang10Content {
        private final File file;
        
        public Djang10File(File file) {
            this.file = file;
        }
        public String getContent() throws IOException {
            return StreamUtil.readFully(file);
        }
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }
        public long lastUpdated() {
            return file.lastModified();
        }
        public File getFile() {
            return file;
        }
        public String getName() {
            return file.toString();
        }
    }
    private static class DJang10String implements Djang10Content {
        private final String content;
        private final long timestamp;
        
        public DJang10String(String content) {
            this.content = content;
            timestamp = System.currentTimeMillis();
        }
        public String getContent() throws IOException {
            return content;
        }
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content.getBytes());
        }
        public long lastUpdated() {
            return timestamp;
        }
        public String getName() {
            return "temp"+timestamp+".djang10";
        }
    }
}
