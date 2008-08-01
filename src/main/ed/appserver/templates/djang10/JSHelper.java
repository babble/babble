
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JSRegex;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.js.func.JSFunctionCalls3;
import ed.log.Logger;
import ed.util.Pair;

public class JSHelper extends JSObjectBase {

    public static final String NS = "djang10";

    private final ArrayList<JSFileLibrary> moduleRoots;
    private final ArrayList<Pair<JxpSource,Library>> defaultLibraries;

    public JSHelper() {
        moduleRoots = new ArrayList<JSFileLibrary>();
        defaultLibraries = new ArrayList<Pair<JxpSource,Library>>();

        // add the basic helpers
        this.set("TEMPLATE_DIRS", new JSArray());
        this.set("TEMPLATE_LOADERS", new JSArray());
        this.set("get_template", get_template);
        //backwards compat
        this.set("loadTemplate", get_template);
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
        this.set("split_str", split_str);
        
        this.set("DEBUG", Djang10Source.DEBUG);
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
    
    public final JSFunction get_template = new JSFunctionCalls2() {
        public Object call(Scope scope, Object pathObj, Object dirsObj, Object[] extra) {
            if (pathObj == null || pathObj == Expression.UNDEFINED_VALUE)
                throw new NullPointerException("Can't load a null or undefined template");

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
            if(Djang10Source.DEBUG)
                System.out.println("All loaders failed to load: " + pathObj);
            
            return null;
        }
    };

    public final JSFunction addTemplateRoot = new JSFunctionCalls1() {
        public Object call(Scope scope, Object newRoot, Object[] extra) {
            JSArray template_dirs = (JSArray)JSHelper.this.get("TEMPLATE_DIRS");
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
    
    
    //hacks to allow backwards compatibility with print & DEBUGING of render calls
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
            
            if(Djang10Source.DEBUG) {
                String selfRepr = "Unkown";
                try {
                    selfRepr = thisObj.toString();
                }
                catch(Throwable t) {}
                
                System.out.println("Rendering: " + selfRepr);
            }
            
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
            
            if(Djang10Source.DEBUG) {
                String selfRepr = "Unkown";
                try {
                    selfRepr = thisObj.toString();
                }
                catch(Throwable t) {}
                
                System.out.println("Rendering: " + selfRepr);
            }
            
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
