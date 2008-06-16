
package ed.appserver.templates.djang10;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ed.appserver.JSFileLibrary;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.JSCompiledScript;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class JSHelper extends JSObjectBase {

    public static final String NS = "djang10";

    private final ArrayList<JSFileLibrary> templateRoots;
    private final ArrayList<JSFileLibrary> moduleRoots;
    private final ArrayList<Library> defaultLibraries;

    public JSHelper() {
        templateRoots = new ArrayList<JSFileLibrary>();
        moduleRoots = new ArrayList<JSFileLibrary>();
        defaultLibraries = new ArrayList<Library>();
        

        // add the basic helpers
        this.set("loadTemplate", loadPath);
        this.set("addTemplateRoot", addTemplateRoot);

        this.set("addTemplateTagsRoot", addModuleRoot);
        this.set("loadLibrary", loadModule);

        this.set("Context", Context.CONSTRUCTOR);
        this.set("Library", Library.CONSTRUCTOR);
        this.set("Node", Node.CONSTRUCTOR);
        this.set("TextNode", Node.TextNode.CONSTRUCTOR);
        this.set("VariableNode", Node.VariableNode.CONSTRUCTOR);
        this.set("Expression", Expression.CONSTRUCTOR);
    }

    public static JSHelper install(Scope scope) {
        JSHelper helper = new JSHelper();
        scope.set(NS, helper);
        return helper;
    }
    
    public final JSFunction loadPath = new JSFunctionCalls1() {
        public Object call(Scope scope, Object pathObj, Object[] extra) {

            if (pathObj == null || pathObj == Expression.UNDEFINED_VALUE)
                return null;

            if (pathObj instanceof Djang10CompiledScript)
                return pathObj;

            String path = ((JSString) pathObj).toString().trim().replaceAll("/+", "/").replaceAll("\\.\\w*$", "");
            Djang10CompiledScript target = null;

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

                    return (newRootBaseObj instanceof Djang10CompiledScript) ? newRootBaseObj : null;
                } else {
                    Object targetObj = ((JSFileLibrary) newRootBaseObj).getFromPath(newRootPathParts[2]);

                    return (targetObj instanceof Djang10CompiledScript) ? targetObj : null;
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
                if (target == null)
                    return null;
            }

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
    private final JSFunction loadModule = new JSFunctionCalls1() {
        public Object call(Scope scope, Object p0, Object[] extra) {
            return loadModule(scope, p0.toString());
        }
    };


    public void addDefaultLibrary(Library library) {
        defaultLibraries.add(library);
    }
    public List<Library> getDefaultLibraries() {
        return defaultLibraries;
    }
    
    public void addModuleRoot(JSFileLibrary newRoot) {
        if (newRoot != null)
            moduleRoots.add(newRoot);
    }

    public Library callModule(Scope scope, String name) {
        JSCompiledScript module = loadModule(scope, name);

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