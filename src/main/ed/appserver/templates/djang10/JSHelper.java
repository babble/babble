
/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver.templates.djang10;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.python.core.PyObject;
import org.python.core.PyString;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSON;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSRegex;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.python.Python;
import ed.log.Level;
import ed.log.Logger;
import ed.util.Pair;

public class JSHelper extends JSObjectBase {
    private final Logger log;
    
    public static final String NS = "djang10";

    private final ArrayList<TemplateTagRoot> moduleRoots;
    private final ArrayList<Pair<JxpSource,Library>> defaultLibraries;
    
    private final Map<String, JSFileLibrary> fileRoots;
    
    public JSHelper(Logger djang10Logger, Map<String, JSFileLibrary> fileRoots) {
        this.log = djang10Logger;
        this.fileRoots = Collections.unmodifiableMap(fileRoots);
        moduleRoots = new ArrayList<TemplateTagRoot>();
        defaultLibraries = new ArrayList<Pair<JxpSource,Library>>();

        // add the basic helpers
        this.set("ALLOW_GLOBAL_FALLBACK", Boolean.TRUE);
        this.set("TEMPLATE_DIRS", new JSArray());
        this.set("TEMPLATE_LOADERS", new JSArray());
                
        this.set("Context", Context.CONSTRUCTOR);
        this.set("Library", Library.CONSTRUCTOR);
        this.set("Node", Node.CONSTRUCTOR);
        this.set("TextNode", Node.TextNode.CONSTRUCTOR);
        this.set("VariableNode", Node.VariableNode.CONSTRUCTOR);
        this.set("Expression", Expression.CONSTRUCTOR);
        
        this.set("TemplateSyntaxError", TemplateSyntaxError.cons);
        this.set("Djang10Exception", Djang10Exception.cons);
        this.set("TemplateDoesNotExist", TemplateDoesNotExist.cons);
        
        this.set("mark_safe", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return mark_safe((JSObject)p0);
            }
        });
        this.set("mark_escape", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return mark_escape((JSObject)p0);
            }
        });
        this.set("is_safe", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return is_safe(p0);
            }
        });
        this.set("is_escape", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return is_escape(p0);
            }
        });
        
        this.set("TEMPLATE_STRING_IF_INVALID", new JSString(""));
        addTemplateTagsRoot("/local/templatetags");
    }
    
    
    //Scope management ===================================================
    public static JSHelper install(Scope scope, Map<String, JSFileLibrary> fileLibRoots, Logger siteLogger) {
        //XXX: during appcontext init, the site loger hasn't been setup yet, so have to pull it out of the scope
        Logger djang10Logger = siteLogger.getChild("djang10");
        djang10Logger.setLevel(Level.INFO);
        
        
        
        JSHelper helper = new JSHelper(djang10Logger, fileLibRoots);
        scope.set(NS, helper);
        
        return helper;
    }
    
    public static JSHelper get(Scope scope) {
        Object temp = scope.get(NS);
        if(!(temp instanceof JSHelper))
            throw new IllegalStateException("Can't find JSHelper in scope");
        return (JSHelper)temp;
    }
    
    
    //Exception Constructors ===================================================
    public static RuntimeException unnestJSException(JSException e) {
        return (e.getCause() instanceof RuntimeException)? (RuntimeException)e.getCause() : e;
    }
    
    //Text Formatting helpers ===================================================
    public JSString formatString(Object obj, String format) {
        PyObject wrappedObj = Python.toPython(obj);
        PyString wrappedFormat = new PyString(format);
        
        PyString result = (PyString)wrappedFormat.__mod__(wrappedObj);
        
        return new JSString(result.toString());
    }
    
    public JSString formatDate(JSDate date, String format) {            
        return new JSString( Util.formatDate(new Date(date.getTime()), format) );
    };
    public JSString formatTime(JSDate date, String format) {
        return new JSString( Util.formatTime(new Date(date.getTime()), format) );
    };
    
    public JSArray split_str(String str, JSRegex regex) {
        Pattern pattern = (regex != null)? regex.getCompiled() : Pattern.compile("\\s+");
        
        String[] bits = Util.split(pattern, str);
        JSArray result = new JSArray();
        for(String bit : bits)
            result.add(new JSString(bit));
        
        return result;
    }
    
    public JSArray smart_split(String str, JSArray seperators) {
        String[] sepArray = new String[seperators.size()];
        for(int i=0; i<sepArray.length; i++)
            sepArray[i] = seperators.getAsString(i);

        String[] resultsArray = Util.smart_split(str, sepArray, false);
        
        return new JSArray((Object[])resultsArray);
    }
    public JSString str_encode(String str, String charsetName, String errors) {
        Charset asciiCharset = Charset.forName(charsetName);
        
        CodingErrorAction errorAction;
        if("strict".equals(errors))
            errorAction = CodingErrorAction.REPORT;
        else if("ignore".equals(errors))
            errorAction = CodingErrorAction.IGNORE;
        else if("replace".equals(errors))
            errorAction = CodingErrorAction.REPLACE;
        else 
            throw new UnsupportedOperationException("Unsupported error handler: " + errors);
        
        ByteBuffer bytes;
        try {
            bytes = asciiCharset.newEncoder()
                .onMalformedInput(errorAction)
                .onUnmappableCharacter(errorAction)
                .encode(CharBuffer.wrap(str));
        } catch (CharacterCodingException e) {
            throw new RuntimeException("Unexpected error:", e);
        }
        
        return new JSString( asciiCharset.decode(bytes).toString() );
    }
    
    public Object resolve_absolute_path(String path) {
        log.debug("resolving [" + path + "]");

        path = path.trim().replaceAll("/+", "/");
        if (!path.startsWith("/"))
            throw new TemplateException("Only Absolute paths are allowed, path: [" + path + "]");

        String[] parts = path.split("/", 3);

        JSFileLibrary root = fileRoots.get(parts[1]);
        if(root == null)
            return null;

        if (parts.length == 3 && parts[2].length() > 0)
            return root.getFromPath(parts[2]);
        
        return root;
    }
    
    //Template loading ==========================================================================
    @Deprecated
    public Djang10CompiledScript loadTemplate(Scope scope, JSString path, JSArray dirs) {
        return get_template(scope, path, dirs);
    }
    public Djang10CompiledScript get_template(Scope scope, JSString path, JSArray dirs) {
        Logger log = this.log.getChild("loader");
        
        if(path == null)
            throw new NullPointerException("Can't load a null or undefined template");
        
        log.debug("loading [" + path + "]");
        
        JSArray loaders = (JSArray)get("TEMPLATE_LOADERS");
        
        for(Object loaderObj : loaders) {
            JSFunction loader;
            if(loaderObj instanceof JSString || loaderObj instanceof String) {
                String loaderStr = loaderObj.toString();
                int lastDot = loaderStr.lastIndexOf('.');
                
                if(lastDot < 0)
                    throw new IllegalArgumentException("The loader ["+loaderStr+"] is invalid format, use the format path.to.file.loaderMethodName");
                
                String filePart = '/' + loaderStr.substring(0, lastDot).replace('.', '/');
                String methodPart = loaderStr.substring(lastDot+1);
                
                Object temp = resolve_absolute_path(filePart);
                if(!(temp instanceof JSCompiledScript)) {
                    log.error("Failed to locate the loader ["+loaderStr+"], the path [" + filePart + "] resolves to [" + temp +"] which isn't a script");
                    continue;
                }
                JSCompiledScript file = (JSCompiledScript)temp;
                
                Scope childScope = scope.child();
                file.call(childScope);
                loader = scope.getFunction(methodPart);
            }
            else
                loader = (JSFunction)loaderObj;
            

            Scope childScope = scope.child();
            childScope.setGlobal(true);
            Djang10CompiledScript template;
            
            try {
                template = (Djang10CompiledScript)loader.call(childScope, path, dirs);
            } catch(JSException e) {               
                throw unnestJSException(e);
            }
            if(template != null)
                return template;
        }

        
        String msg = "get_template: Failed to load [" + path +"]. ";
        msg += "TEMPLATE_DIRS: ";
        try {
            msg += JSON.serialize(JSHelper.this.get("TEMPLATE_DIRS"));
        } catch(Exception e) {
            msg += e.getMessage();
        }
        log.error(msg);
        
        return null;
    }


    public void addTemplateRoot(Scope scope, Object newRoot) {
        Logger log = this.log.getChild("loader");
        
        JSArray template_dirs = (JSArray)get("TEMPLATE_DIRS");    
        
        log.debug("Adding TemplateRoot [" + newRoot + "]");
        template_dirs.add(newRoot);
    }

    
    // Template Module loading ===================================================================
    public void addTemplateTagsRoot(String path) {
        log.debug("adding [" + path + "] as a new templatetags path");
        
        if(path == null)
            throw new NullPointerException("Can't add a null templatetag root");
        
        moduleRoots.add(new TemplateTagStringRoot(path));
    }
    public void addTemplateTagsRoot(JSFileLibrary newRoot) {
        log.debug("Adding [" + newRoot + "] as a new templatetags root");
        
        if(newRoot == null)
            throw new NullPointerException("Can't add a null templatetag root");
        
        moduleRoots.add(new TemplateTagFileLibRoot(newRoot));
    }
    
    public JSCompiledScript loadLibrary(Scope scope, String name) {
        log.debug("loading template tag library [" + name + "]");
        
        ListIterator<TemplateTagRoot> iter = moduleRoots.listIterator(moduleRoots.size());
        while(iter.hasPrevious()) {
            TemplateTagRoot templateTagRoot = iter.previous();
            log.debug("checking templatetag root [" + templateTagRoot + "]");
            
            JSFileLibrary fileLib;
            
            try {
                fileLib = templateTagRoot.resolve(scope);
            } catch(RuntimeException e) {
                log.warn("failed to resolve templatetag root [" + templateTagRoot + "]. error [" + e.getMessage() + "]");
                continue;
            }
            
            Object file = fileLib.getFromPath(name);
            if (file instanceof JSCompiledScript) {
                log.debug("loaded [" + fileLib + "/" + name + "]");
                return (JSCompiledScript) file;
            }
            else if(file == null) {
                log.debug("[" + fileLib + "/" + name + "] doesn't exist");
            }
            else {
                log.debug("[" + fileLib + "/" + name + "] is not a file, but a [" + file.getClass() + "]");
            }
        }

        throw new TemplateException("Failed to find module [" + name + "]");
    }
    
    public Library evalLibrary(Scope scope, JSCompiledScript moduleFile) {
        log.debug("Processing [" + moduleFile + "]");
        
        Scope child = scope.child();
        child.setGlobal(true);
        
        try {
            moduleFile.call(child);
        } catch(Exception t) {
            throw new TemplateException("Failed to load library from file [" + moduleFile.get(JxpSource.JXP_SOURCE_PROP) + "]", t);
        }
        Object temp = child.get("register");
        if(!(temp instanceof Library))
            throw new TemplateException("Misconfigured library doesn't contain a correct register variable. file [" + moduleFile.get(JxpSource.JXP_SOURCE_PROP) + "]");
        Library lib = (Library)temp;

        //wrap all the tag handlers
        JSObject tagHandlers = lib.getTags();
        for(String tagName : tagHandlers.keySet()) {
            JSFunction tagHandler = (JSFunction)tagHandlers.get(tagName);
            tagHandlers.set(tagName, tagHandler);
        }
        log.debug("done processing [" + moduleFile + "]");
        return lib;
    }
    
    public void addDefaultLibrary(JxpSource source, Library library) {
        log.debug("Adding default tag library from [" + source + "]");
        
        defaultLibraries.add(new Pair<JxpSource, Library>(source, library));
    }
    public List<Pair<JxpSource,Library>> getDefaultLibraries() {
        return defaultLibraries;
    }

    
    // Safe string stuff ===================================================================
    public static JSObject mark_safe(JSObject obj) {
        obj.set("__safe_data", true);
        return obj;
    }
    public static JSObject mark_escape(JSObject obj) {
        if(is_safe(obj) == Boolean.TRUE)
            return obj;        
        obj.set("__safe_data", false);

        return obj;
    }
    public static Boolean is_safe(Object obj) {
        if(obj == null || "".equals(obj))
            return true;
        
        if((obj instanceof JSString) && obj.equals(""))
            return true;
        
        if(obj instanceof Number)
            return true;

        if(obj instanceof JSObject)            
            return (Boolean)((JSObject)obj).get("__safe_data");
        
        return false;
    }
    public static Boolean is_escape(Object obj) {
        Boolean attr = is_safe(obj);
        
        if(attr == null)
            return null;
        
        return !attr.booleanValue();
    }
    
    
    private interface TemplateTagRoot {
        public JSFileLibrary resolve(Scope scope);
    }
    private class TemplateTagStringRoot  implements TemplateTagRoot {
        private final String path;
        
        public TemplateTagStringRoot(String path) {
            if(path == null)
                throw new NullPointerException("Can't add a null templatetag root");

            this.path = path;
        }
        public JSFileLibrary resolve(Scope scope) {            
            Object temp = resolve_absolute_path(this.path);

            if(!(temp instanceof JSFileLibrary))
                throw new IllegalArgumentException("The path [" + path + "] is invalid, because its not a file library, but a: [" + ((temp == null)?"null":temp.getClass()) + "]");

            return (JSFileLibrary)temp;
        }
        public String toString() {
            return "[" + path + "]";
        }
    }
    private class TemplateTagFileLibRoot implements TemplateTagRoot {
        private final JSFileLibrary fileLib;
        
        public TemplateTagFileLibRoot(JSFileLibrary fileLib) {
            this.fileLib = fileLib;
        }
        public JSFileLibrary resolve(Scope scope) {
            return fileLib;
        }
        public String toString() {
            return fileLib.toString();
        }
    }
}
