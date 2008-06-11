/**
 * 
 */
package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ed.appserver.JSFileLibrary;
import ed.appserver.templates.Djang10Converter;
import ed.appserver.templates.djang10.Parser.Token;
import ed.appserver.templates.djang10.filters.JavaFilter;
import ed.appserver.templates.djang10.tagHandlers.TagHandler;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class JSHelper extends JSObjectBase {

    public static final String ADD_TEMPLATE_ROOT = "addTemplateRoot";
    public static final String CALL_PATH = "callPath";
    public static final String LOAD_PATH = "loadPath";

    public static final String ADD_MODULE_ROOT = "addModuleRoot";
    public static final String CALL_MODULE = "callModule";
    public static final String LOAD_MODULE = "loadModule";

    public static final String CONTEXT_CLASS = "Context";
    public static final String LIBRARY_CLASS = "Library";
    public static final String NS = "__djang10";
    public static final String publicApi = "publicApi";

    private final ArrayList<JSFileLibrary> templateRoots;
    private final ArrayList<JSFileLibrary> moduleRoots;

    public JSHelper() {
        templateRoots = new ArrayList<JSFileLibrary>();
        moduleRoots = new ArrayList<JSFileLibrary>();

        // add the template helpers
        Map<String, JSFunction> helpers = new HashMap<String, JSFunction>();
        helpers.putAll(Expression.getHelpers());
        helpers.putAll(JavaFilter.getHelpers());
        for (TagHandler tagHandler : Djang10Converter.getTagHandlers().values()) {
            helpers.putAll(tagHandler.getHelpers());
        }

        for (String name : helpers.keySet()) {
            this.set(name, helpers.get(name));
        }

        // add the basic helpers
        this.set(LOAD_PATH, loadPath);
        this.set(CALL_PATH, callPath);
        this.set(ADD_TEMPLATE_ROOT, addTemplateRoot);

        this.set(ADD_MODULE_ROOT, addModuleRoot);
        this.set(CALL_MODULE, callModule);
        this.set(LOAD_MODULE, loadModule);

        this.set(CONTEXT_CLASS, Context.CONSTRUCTOR);
        this.set(LIBRARY_CLASS, Library.CONSTRUCTOR);
        this.set(Token.NAME, Token.CONSTRUCTOR);
        
        this.set(publicApi, new PublicApi());
        
        this.lock();
    }

    private class PublicApi extends JSObjectBase {
        public PublicApi() {
            set("addTemplateRoot", addTemplateRoot);
            set("loadTemplate", loadPath);
            set("Context", Context.CONSTRUCTOR);
            
            set("addTemplateTagsRoot", addModuleRoot);
            set("Library", Library.CONSTRUCTOR);
        }
    }
    
    private final JSFunction callPath = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {
            Object loadedObj = loadPath.call(scope, pathObj, extra);

            if (loadedObj instanceof JSCompiledScript)
                return ((JSCompiledScript) loadedObj).call(scope.child(), extra);

            return null;
        }
    };

    private final JSFunction loadPath = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {

            if (pathObj == null || pathObj == Expression.UNDEFINED_VALUE)
                return null;

            if (pathObj instanceof JSCompiledScript)
                return pathObj;

            String path = ((JSString) pathObj).toString().trim().replaceAll("/+", "/").replaceAll("\\.\\w*$", "");
            JSCompiledScript target = null;

            if (path.startsWith("/")) {
                String[] newRootPathParts = path.split("/", 3);
                if (newRootPathParts.length < 2 || newRootPathParts[1].trim().length() == 0)
                    return null;

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

                    return (newRootBaseObj instanceof JSCompiledScript) ? newRootBaseObj : null;
                } else {
                    Object targetObj = ((JSFileLibrary) newRootBaseObj).getFromPath(newRootPathParts[2]);

                    return (targetObj instanceof JSCompiledScript) ? targetObj : null;
                }
            } else {
                for (int i = templateRoots.size() - 1; i >= 0; i--) {
                    JSFileLibrary fileLibrary = templateRoots.get(i);

                    Object targetObj = fileLibrary.getFromPath(path);

                    if (targetObj instanceof JSCompiledScript) {
                        target = (JSCompiledScript) targetObj;
                        break;
                    }
                }
                if (target == null)
                    return null;
            }

            return target;
        }
    };

    private final JSFunction addTemplateRoot = new JSFunctionCalls1() {
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

    private final JSFunction addModuleRoot = new JSFunctionCalls1() {
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

    private final JSFunction callModule = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {

            return callModule(scope, ((JSString) pathObj).toString());
        }
    };

    private final JSFunction loadModule = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {

            return loadModule(((JSString) pathObj).toString());
        }
    };

    public void addModuleRoot(JSFileLibrary newRoot) {
        if (newRoot != null)
            moduleRoots.add(newRoot);
    }

    public Library callModule(Scope scope, String name) {
        JSCompiledScript module = loadModule(name);

        if (module == null)
            return null;

        Scope evalScope = scope.child();
        evalScope.setGlobal(true);
        module.call(evalScope);

        Object registerObj = evalScope.get("register");
        if (!(registerObj instanceof Library))
            return null;

        return (Library) registerObj;
    }

    public JSCompiledScript loadModule(String name) {
        if (name == null)
            return null;

        JSCompiledScript moduleFile = null;

        for (JSFileLibrary fileLib : moduleRoots) {
            Object file = fileLib.get(name);

            if (file instanceof JSCompiledScript) {
                moduleFile = (JSCompiledScript) file;
                break;
            }
        }
        return moduleFile;
    }

    private JSFileLibrary resolvePath(Scope callingScope, String path) {
        JSFileLibrary templateFileLib;

        String newRootPath = path.toString().trim().replaceAll("/+", "/");
        if (!newRootPath.startsWith("/"))
            throw new IllegalArgumentException("Only Absolute paths are allowed");

        String[] newRootPathParts = newRootPath.split("/", 3);

        // find the base file lib
        Object templateFileLibObj = callingScope.get(newRootPathParts[1]);
        if (!(templateFileLibObj instanceof JSFileLibrary))
            throw new IllegalArgumentException("Path not found");
        templateFileLib = (JSFileLibrary) templateFileLibObj;

        if (newRootPathParts.length == 3 && newRootPathParts[2].length() > 0) {
            templateFileLibObj = templateFileLib.getFromPath(newRootPathParts[2]);

            if (!(templateFileLibObj instanceof JSFileLibrary))
                throw new IllegalArgumentException("Path not found: " + path);

            templateFileLib = (JSFileLibrary) templateFileLibObj;
        }

        return templateFileLib;
    }
}