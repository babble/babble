
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
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.python.core.PyObject;
import org.python.core.PyString;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.Parser.Token;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSFunction;
import ed.js.JSON;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSRegex;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.js.func.JSFunctionCalls3;
import ed.lang.python.Python;
import ed.log.Level;
import ed.log.Logger;
import ed.util.Pair;

public class JSHelper extends JSObjectBase {
    private final Logger log;
    
    public static final String NS = "djang10";

    private final ArrayList<JSFileLibrary> moduleRoots;
    private final ArrayList<Pair<JxpSource,Library>> defaultLibraries;

    public JSHelper(Logger djang10Logger) {
        this.log = djang10Logger;

        
        moduleRoots = new ArrayList<JSFileLibrary>();
        defaultLibraries = new ArrayList<Pair<JxpSource,Library>>();

        // add the basic helpers
        this.set("ALLOW_GLOBAL_FALLBACK", Boolean.TRUE);
        this.set("TEMPLATE_DIRS", new JSArray());
        this.set("TEMPLATE_LOADERS", new JSArray());
        
        JSFunction get_template = new get_templateFunc(djang10Logger);
        this.set("get_template", get_template);
        //backwards compat
        this.set("loadTemplate", get_template);
        this.set("addTemplateRoot", addTemplateRoot);

        
        this.set("addTemplateTagsRoot", addModuleRoot);
        
        this.set("loadLibrary", loadLibrary);
        this.set("evalLibrary", evalLibrary);
        
        this.set("formatDate", formatDate);
        this.set("formatTime", formatTime);
        this.set("formatString", formatString);

        this.set("Context", Context.CONSTRUCTOR);
        this.set("Library", Library.CONSTRUCTOR);
        this.set("Node", Node.CONSTRUCTOR);
        this.set("TextNode", Node.TextNode.CONSTRUCTOR);
        this.set("VariableNode", Node.VariableNode.CONSTRUCTOR);
        this.set("Expression", Expression.CONSTRUCTOR);
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
        
        this.set("NewTemplateException", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return new TemplateException(String.valueOf(p0));
            }
        });
        
        this.set("NewTemplateSyntaxException", new JSFunctionCalls3() {
            public Object call(Scope scope, Object messageObj, Object tokenObj, Object causeObj, Object[] extra) {
                String msg = messageObj.toString();
                Token token = (Token)tokenObj;
                
                if(causeObj == null)
                    return new TemplateSyntaxError(msg, token);
                
                Throwable cause = (Throwable) causeObj;
                
                return new TemplateSyntaxError(msg, token, cause);
            }
        });
        
        this.set("split_str", split_str);
        
        this.set("str_encode", new JSFunctionCalls3() {
            public Object call(Scope scope, Object strObj, Object charsetNameObj, Object errorsObj, Object[] extra) {
                String str = ((JSString)strObj).toString();
                String charsetName = ((JSString)charsetNameObj).toString();
                String errors = ((JSString)errorsObj).toString();
                
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
        });
    }

    public static JSHelper install(Scope scope) {
        //XXX: during appcontext init, the site loger hasn't been setup yet, so have to pull it out of the scope
        Logger djang10Logger = ((Logger)scope.get("log")).getChild("djang10");
        djang10Logger.setLevel(Level.INFO);
        
        JSHelper helper = new JSHelper(djang10Logger);
        scope.set(NS, helper);
        return helper;
    }
    
    public static JSHelper get(Scope scope) {
        Object temp = scope.get(NS);
        if(!(temp instanceof JSHelper))
            throw new IllegalStateException("Can't find JSHelper not installed");
        return (JSHelper)temp;
    }
    
    public JSFunction formatString = new JSFunctionCalls2() {
        public Object call(Scope scope, Object obj, Object formatObj, Object[] extra) {
            PyObject wrappedObj = Python.toPython(obj);
            String format = formatObj.toString();
            PyString wrappedFormat = new PyString(format);
            
            PyString result = (PyString)wrappedFormat.__mod__(wrappedObj);
            
            return new JSString(result.toString());
        };
    };
    
    public JSFunction formatDate = new JSFunctionCalls2() {
        public Object call(Scope scope, Object p0, Object p1, Object[] extra) {
            JSDate date = (JSDate)p0;
            String format = ((JSString)p1).toString();
            
            return new JSString( Util.formatDate(new Date(date.getTime()), format) );
        };
    };
    public JSFunction formatTime = new JSFunctionCalls2() {
        public Object call(Scope scope, Object p0, Object p1, Object[] extra) {
            JSDate date = (JSDate)p0;
            String format = ((JSString)p1).toString();
            
            return new JSString( Util.formatTime(new Date(date.getTime()), format) );
        };
    };
    
    public JSFunction split_str = new JSFunctionCalls2() {
        public Object call(Scope scope, Object strObj, Object regexObj, Object[] extra) {
            String str = strObj.toString();
            Pattern pattern = (regexObj != null)? ((JSRegex)regexObj).getCompiled() : Pattern.compile("\\s+");
            
            String[] bits = Util.split(pattern, str);
            JSArray result = new JSArray();
            for(String bit : bits)
                result.add(new JSString(bit));
            
            return result;
        }
    };
    
    public class get_templateFunc extends JSFunctionCalls2 {
        private final Logger log;
        public get_templateFunc(Logger djang10Logger) {
            this.log = djang10Logger.getChild("loaders").getChild("get_template");
        }
        public Object call(Scope scope, Object pathObj, Object dirsObj, Object[] extra) {            
            if (pathObj == null || pathObj == Expression.UNDEFINED_VALUE)
                throw new NullPointerException("Can't load a null or undefined template");

            log.debug("loading: " + pathObj);
            
            JSArray loaders = (JSArray)JSHelper.this.get("TEMPLATE_LOADERS");
            
            for(Object loaderObj : loaders) {
                JSFunction loader;
                if(loaderObj instanceof JSString || loaderObj instanceof String) {
                    String loaderStr = loaderObj.toString();
                    int firstDot = loaderStr.indexOf('.');
                    int lastDot = loaderStr.lastIndexOf('.');
                    String rootPart = loaderStr.substring(0, firstDot);
                    String filePart = loaderStr.substring(firstDot+1, lastDot);
                    String methodPart = loaderStr.substring(lastDot+1);
                    
                    JSFileLibrary root = (JSFileLibrary)scope.get(rootPart);
                    JSCompiledScript file = (JSCompiledScript)root.getFromPath(filePart.replace('.', '/'));
                    
                    Scope childScope = scope.child();
                    file.call(childScope);
                    loader = scope.getFunction(methodPart);
                }
                else
                    loader = (JSFunction)loaderObj;
                

                Scope childScope = scope.child();
                childScope.setGlobal(true);
                Djang10CompiledScript template = (Djang10CompiledScript)loader.call(childScope, pathObj, dirsObj);
                if(template != null)
                    return template;
            }

            
            String msg = "get_template: Failed to load: " + pathObj +". ";
            msg += "TEMPLATE_DIRS: ";
            try {
                msg += JSON.serialize(JSHelper.this.get("TEMPLATE_DIRS"));
            } catch(Exception e) {
                msg += e.getMessage();
            }
            log.error(msg);
            
            return null;
        }
    };

    public final JSFunction addTemplateRoot = new JSFunctionCalls1() {
        public Object call(Scope scope, Object newRoot, Object[] extra) {
            JSArray template_dirs = (JSArray)JSHelper.this.get("TEMPLATE_DIRS");
            
            log.debug("Adding TemplateRoot: " + newRoot);
            
            template_dirs.add(newRoot);

            return null;
        }
    };

    public final JSFunction addModuleRoot = new JSFunctionCalls1() {
        public Object call(Scope scope, Object newRoot, Object[] extra) {
            JSFileLibrary tagFileLib;

            if (newRoot instanceof JSString) {
                tagFileLib = resolvePath(scope, newRoot.toString());
            } else if (newRoot instanceof JSFileLibrary) {
                tagFileLib = (JSFileLibrary) newRoot;
            } else {
                throw new IllegalArgumentException("Only Paths and FileLibraries are accepted");
            }
            addModuleRoot(tagFileLib);

            return null;
        }
    };
    private final JSFunction loadLibrary = new JSFunctionCalls1() {
        public Object call(Scope scope, Object p0, Object[] extra) {
            return loadModule(scope, p0.toString());
        }
    };
    
    public final JSFunction evalLibrary = new JSFunctionCalls1() {
        public Object call(Scope scope, Object moduleFileObj, Object[] extra) {
            JSCompiledScript moduleFile = (JSCompiledScript)moduleFileObj;
            Scope child = scope.child();
            child.setGlobal(true);
            
            try {
                moduleFile.call(child);
            } catch(Exception t) {
                throw new TemplateException("Failed to load library from file: " + moduleFile.get(JxpSource.JXP_SOURCE_PROP), t);
            }
            Object temp = child.get("register");
            if(!(temp instanceof Library))
                throw new TemplateException("Misconfigured library doesn't contain a correct register variable. file: " + moduleFile.get(JxpSource.JXP_SOURCE_PROP));
            Library lib = (Library)temp;

            //wrap all the tag handlers
            JSObject tagHandlers = lib.getTags();
            for(String tagName : tagHandlers.keySet()) {
                JSFunction tagHandler = (JSFunction)tagHandlers.get(tagName);
                tagHandlers.set(tagName, tagHandler);
            }

            return lib;
        };
    };

    public void addDefaultLibrary(JxpSource source, Library library) {
        defaultLibraries.add(new Pair<JxpSource, Library>(source, library));
    }
    public List<Pair<JxpSource,Library>> getDefaultLibraries() {
        return defaultLibraries;
    }
    
    public void addModuleRoot(JSFileLibrary newRoot) {
        if (newRoot != null)
            moduleRoots.add(newRoot);
    }

    public JSCompiledScript loadModule(Scope scope, String name) {
        if (name == null)
            return null;

        JSCompiledScript moduleFile = null;

        log.debug("Trying to load module: " + name);
        
        ListIterator<JSFileLibrary> iter = moduleRoots.listIterator(moduleRoots.size());
        while(iter.hasPrevious()) {
            JSFileLibrary fileLib = iter.previous();
            
            log.debug("Checking: " + fileLib);
            Object file = fileLib.get(name);

            if (file instanceof JSCompiledScript) {
                log.debug("found");
                moduleFile = (JSCompiledScript) file;
                break;
            }
            else if(file != null) {
                log.warn(fileLib + "/" + file + " is not a file, but a: " + file.getClass());
            }
            else {
                log.debug("not found");
            }
        }
        
        log.debug("Checking: /local/templatetags");
        //try resolving against /local/templatetags
        if(!(moduleFile instanceof JSCompiledScript)) {
            JSFileLibrary local = (JSFileLibrary)scope.get("local");
            Object templateTagDirObj = local.get("templatetags");
            if(templateTagDirObj instanceof JSFileLibrary) {
                Object file = ((JSFileLibrary)templateTagDirObj).get(name);
                if(file instanceof JSCompiledScript) {
                    log.debug("Found");
                    moduleFile = (JSCompiledScript)file;
                }
                else if(file != null) {
                    log.warn(templateTagDirObj + "/" + file + " is not a file, but a: " + file.getClass());
                }
                else {
                    log.debug("not found");
                }
            }
            else {
                log.debug("/local/templatetags is not a fileLib");
            }
        }
        if(moduleFile == null)
            throw new TemplateException("Failed to find module: " + name);
        return moduleFile;
    }

    private JSFileLibrary resolvePath(Scope callingScope, String path) {
        JSFileLibrary templateFileLib;

        String newRootPath = path.toString().trim().replaceAll("/+", "/");
        if (!newRootPath.startsWith("/"))
            throw new TemplateException("Only Absolute paths are allowed");

        String[] newRootPathParts = newRootPath.split("/", 3);

        // find the base file lib
        Object templateFileLibObj = callingScope.get(newRootPathParts[1]);
        if (!(templateFileLibObj instanceof JSFileLibrary))
            throw new TemplateException("Path not found");
        templateFileLib = (JSFileLibrary) templateFileLibObj;

        if (newRootPathParts.length == 3 && newRootPathParts[2].length() > 0) {
            templateFileLibObj = templateFileLib.getFromPath(newRootPathParts[2]);

            if (!(templateFileLibObj instanceof JSFileLibrary))
                throw new TemplateException("Path not found: " + path);

            templateFileLib = (JSFileLibrary) templateFileLibObj;
        }

        return templateFileLib;
    }
    
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
}
