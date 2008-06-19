
package ed.appserver.templates.djang10;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.jruby.RubyProcess.Sys;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.js.JSDate;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.log.Logger;
import ed.util.Pair;

public class JSHelper extends JSObjectBase {

    public static final String NS = "djang10";

    private final ArrayList<JSFileLibrary> templateRoots;
    private final ArrayList<JSFileLibrary> moduleRoots;
    private final ArrayList<Pair<JxpSource,Library>> defaultLibraries;

    public JSHelper() {
        templateRoots = new ArrayList<JSFileLibrary>();
        moduleRoots = new ArrayList<JSFileLibrary>();
        defaultLibraries = new ArrayList<Pair<JxpSource,Library>>();
        

        // add the basic helpers
        this.set("loadTemplate", loadPath);
        this.set("addTemplateRoot", addTemplateRoot);

        this.set("addTemplateTagsRoot", addModuleRoot);
        this.set("loadLibrary", loadLibrary);
        this.set("evalLibrary", evalLibrary);
        
        this.set("formatDate", formatDate);

        this.set("Context", Context.CONSTRUCTOR);
        this.set("Library", Library.CONSTRUCTOR);
        this.set("Node", Node.CONSTRUCTOR);
        this.set("TextNode", Node.TextNode.CONSTRUCTOR);
        this.set("VariableNode", Node.VariableNode.CONSTRUCTOR);
        this.set("Expression", Expression.CONSTRUCTOR);
        
        this.set("TEMPLATE_STRING_IF_INVALID", new JSString(""));
        
        this.set("NewTemplateException", new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                return new TemplateException(String.valueOf(p0));
            }
        });
    }

    public static JSHelper install(Scope scope) {
        JSHelper helper = new JSHelper();
        scope.set(NS, helper);
        return helper;
    }
    
    public static JSHelper get(Scope scope) {
        Object temp = scope.get(NS);
        if(!(temp instanceof JSHelper))
            throw new IllegalStateException("Can't find JSHelper not installed");
        return (JSHelper)temp;
    }
    
    public JSFunction formatDate = new JSFunctionCalls2() {
        public Object call(Scope scope, Object p0, Object p1, Object[] extra) {
            JSDate date = (JSDate)p0;
            String format = ((JSString)p1).toString();
            
            return new JSString( Util.formatDate(new Date(date.getTime()), format) );
        };
    };
    
    public final JSFunction loadPath = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {
            if (pathObj == null || pathObj == Expression.UNDEFINED_VALUE)
                throw new NullPointerException("Can't load a null or undefined template");

            if (pathObj instanceof Djang10CompiledScript)
                return pathObj;
            String path = ((JSString) pathObj).toString().trim().replaceAll("/+", "/").replaceAll("\\.\\w*$", "");
            Djang10CompiledScript target = null;

            //absolute path
            if (path.startsWith("/")) {
                String[] newRootPathParts = path.split("/", 3);
                //make there's a file component in the path instead of just /
                if (newRootPathParts.length < 2 || newRootPathParts[1].trim().length() == 0)
                    throw new TemplateDoesNotExist(path);

                //find the root
                String newRootBasePath = newRootPathParts[1];
                Object newRootBaseObj = null;
                if (newRootPathParts.length == 3) {
                    newRootBaseObj = scope.get(newRootBasePath);
                    if (!(newRootBaseObj instanceof JSFileLibrary))
                        newRootBaseObj = null;
                }

                if (newRootBaseObj == null) {
                    // fallback on resolving absolute paths against site
                    newRootBaseObj = ((JSFileLibrary) scope.get("local")).getFromPath(path);

                    target = (Djang10CompiledScript)((newRootBaseObj instanceof Djang10CompiledScript) ? newRootBaseObj : null);
                } else {
                    Object targetObj = ((JSFileLibrary) newRootBaseObj).getFromPath(newRootPathParts[2]);

                    target = (Djang10CompiledScript)((targetObj instanceof Djang10CompiledScript) ? targetObj : null);
                }
            } else {
                for (int i = templateRoots.size() - 1; i >= 0; i--) {
                    JSFileLibrary fileLibrary = templateRoots.get(i);

                    Object targetObj = fileLibrary.getFromPath(path);

                    if (targetObj instanceof Djang10CompiledScript) {
                        target = (Djang10CompiledScript) targetObj;
                        break;
                    }
                }
            }

            if (target == null)
                throw new TemplateDoesNotExist(pathObj.toString());
            return target;
        }
    };

    public final JSFunction addTemplateRoot = new JSFunctionCalls1() {
        public Object call(Scope scope, Object newRoot, Object[] extra) {
            JSFileLibrary templateFileLib;

            if (newRoot instanceof JSString) {
                templateFileLib = resolvePath(scope, newRoot.toString());
            } else if (newRoot instanceof JSFileLibrary) {
                templateFileLib = (JSFileLibrary) newRoot;
            } else {
                throw new IllegalArgumentException("Only Paths and FileLibraries are accepted");
            }

            templateRoots.add(templateFileLib);
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
            } catch(Throwable t) {
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
                TagHandlerWrapper wrapper = new TagHandlerWrapper(tagHandler);
                tagHandlers.set(tagName, wrapper);
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

        ListIterator<JSFileLibrary> iter = moduleRoots.listIterator(moduleRoots.size());

        while(iter.hasPrevious()) {
            JSFileLibrary fileLib = iter.previous();

            Object file = fileLib.get(name);

            if (file instanceof JSCompiledScript) {
                moduleFile = (JSCompiledScript) file;
                break;
            }
        }
        
        //try resolving against /local/templatetags
        if(!(moduleFile instanceof JSCompiledScript)) {
            JSFileLibrary local = (JSFileLibrary)scope.get("local");
            Object templateTagDirObj = local.get("templatetags");
            if(templateTagDirObj instanceof JSFileLibrary) {
                Object file = ((JSFileLibrary)templateTagDirObj).get(name);
                if(file instanceof JSCompiledScript)
                    moduleFile = (JSCompiledScript)file;
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
    
    
    
    //hacks to allow backwards compatibility with print
    private static class TagHandlerWrapper extends JSFunctionCalls2 {
        private final JSFunction tagHandler;
        public TagHandlerWrapper(JSFunction tagHandler) {
            this.tagHandler = tagHandler;
        }
        @Override
        public Object call(Scope scope, Object parserObj, Object tokenObj, Object[] extra) {
            JSObject node = (JSObject)tagHandler.call(scope.child(), parserObj, tokenObj);

            node.set("render", new RenderWrapper((JSFunction)node.get("render")));
            node.set("__render", new __RenderWrapper((JSFunction)node.get("__render")));
            
            return node;
        }
    };
    private static final class RenderWrapper extends JSFunctionCalls1 {
        private final JSFunction renderFunc;
        
        public RenderWrapper(JSFunction renderFunc) {
            this.renderFunc = renderFunc;
        }

        public Object call(Scope scope, Object contextObj, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            
            scope = scope.child();
            PrintWrapper printWrapper = new PrintWrapper();
            scope.set("print", printWrapper);
            
            Object ret = renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj });
            
            if(printWrapper.buffer.length() > 0)
                return printWrapper.buffer + (ret == null? "" : ret.toString());
            else
                return ret;
        }
    };
    private static final class __RenderWrapper extends JSFunctionCalls2 {
        private final JSFunction __renderFunc;
        
        public __RenderWrapper(JSFunction func) {
            __renderFunc = func;
        }
        
        public Object call(Scope scope, Object contextObj, Object printer, Object[] extra) {
            JSObject thisObj = (JSObject)scope.getThis();
            
            scope = scope.child();
            scope.setGlobal(true);
            scope.put("print", printer, true);
            
            __renderFunc.callAndSetThis(scope, thisObj, new Object[] { contextObj, printer });
            
            return null;
        }

        
    };
    
    public static class PrintWrapper extends JSFunctionCalls1 {
        public final StringBuilder buffer = new StringBuilder();
        
        public Object call(Scope scope, Object p0, Object[] extra) {
            String error = "calling print while rendering has undefined behavior which will change in the future.";
            try {
                Object logger = scope.get("log");
                if(logger instanceof Logger)
                    ((Logger)logger).error(error);
            } catch(Throwable t) {
                System.out.println(error);
            }
            
            buffer.append(p0);
            return null;
        }
    }
}
