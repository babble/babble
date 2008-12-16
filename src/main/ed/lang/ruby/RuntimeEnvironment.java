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

package ed.lang.ruby;

import java.io.*;
import java.util.*;

import org.jruby.*;
import org.jruby.ast.Node;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.KCode;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.AppContext;
import ed.appserver.JSFileLibrary;
import ed.appserver.adapter.cgi.EnvMap;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RuntimeEnvironment {

    public static final String XGEN_MODULE_NAME = "XGen";
    public static final String APP_REQ_RUBY_RUNTIME_KEY = "ruby.runtime";
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final Ruby PARSE_RUNTIME = Ruby.newInstance();

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");
    /** Scope top-level functions to avoid loading. */
    static final Collection<String> DO_NOT_LOAD_FUNCS;
    // TODO for now, we just add the local site dir to the load path
    static final String[] BUILTIN_JS_FILE_LIBRARIES = {"local" /* , "core", "external" */};
    static final RubyInstanceConfig CONFIG = new RubyInstanceConfig();
    static final ThreadLocal<RuntimeEnvironment> threadLocalRuntimeEnvironment = new ThreadLocal<RuntimeEnvironment>();

    static {
        DO_NOT_LOAD_FUNCS = new ArrayList<String>();
        DO_NOT_LOAD_FUNCS.add("print");
        DO_NOT_LOAD_FUNCS.add("sleep");
        DO_NOT_LOAD_FUNCS.add("fork");
        DO_NOT_LOAD_FUNCS.add("eval");
        DO_NOT_LOAD_FUNCS.add("exit");
        for (int i = 0; i < RubyJSFunctionWrapper.IGNORE_JS_CLASSES.length; ++i)
            DO_NOT_LOAD_FUNCS.add(RubyJSFunctionWrapper.IGNORE_JS_CLASSES[i]);
    }

    /** Determines what major version of Ruby to compile: 1.8 (false) or YARV/1.9 (true). **/
    public static final boolean YARV_COMPILE = false;

    protected AppContext appContext;
    protected Ruby runtime;
    protected Scope scope;
    protected Map<String, Class> functionDefs = new HashMap<String, Class>();

    public static RuntimeEnvironment getCurrentRuntimeEnvironment() { return threadLocalRuntimeEnvironment.get(); }

    /**
     * Creates Ruby classes and XGen module methods from any new JavaScript
     * classes and functions found in the top level of <var>scope</var>.
     * Called immediately after loading a file using a JSFileLibrary.
     */
    public static void createNewClassesAndXGenMethods() {
        RuntimeEnvironment runenv = RuntimeEnvironment.getCurrentRuntimeEnvironment();
        Ruby runtime = runenv.getRuntime();

        RubyModule xgen = runtime.getOrCreateModule(XGEN_MODULE_NAME);
        Set<String> alreadySeen = new HashSet<String>();
        Scope s = runenv.scope;
        while (s != null) {
            for (String key : s.keySet()) {
                if (alreadySeen.contains(key) || DO_NOT_LOAD_FUNCS.contains(key))
                    continue;
                final Object obj = s.get(key);
                if (isCallableJSFunction(obj)) {
                    /* Each function is a separate subclass of JSFunction. If
                     * we already have a function with the same name and the
                     * same class, then we don't need to re-wrap it. */
                    Class existingFuncClass = runenv.functionDefs.get(key.toString());
                    if (existingFuncClass != null && existingFuncClass.equals(obj.getClass()))
                        continue;

                    if (DEBUG)
                        System.err.println("adding top-level method " + key);
                    alreadySeen.add(key);
                    runenv.functionDefs.put(key.toString(), obj.getClass());
 
                    /* Creates method and attaches to the module. Also creates a new Ruby class if appropriate. */
                    RubyObjectWrapper.createRubyMethod(runenv.scope, runtime, (JSFunction)obj, key, xgen, null);
                }
            }
            s = s.getParent();
        }
    }

    static Node parse(String script, String filePath) {
        /* See the first part of JRuby's Ruby.executeScript(String, String). */
        byte[] bytes;
        try {
            bytes = script.getBytes(KCode.NONE.getKCode());
        } catch (UnsupportedEncodingException e) {
            bytes = script.getBytes();
        }
        return RuntimeEnvironment.PARSE_RUNTIME.parseFile(new ByteArrayInputStream(bytes), filePath, null);
    }

    RuntimeEnvironment(AppContext appContext) {
        this.appContext = appContext;
        runtime = Ruby.newInstance(CONFIG);

        /* Create a module named XGen and include it in the Object class (just
         * like Kernel). */
        RubyModule xgen = runtime.getOrCreateModule(XGEN_MODULE_NAME);
        runtime.getObject().includeModule(xgen);

        /* Create class objects that might be needed in Ruby code before ever
         * being loaded from Java. Note: could use const_missing instead. */
        RubyObjectIdWrapper.getObjectIdClass(runtime);
    }

    public void setup(Scope scope, InputStream stdin, OutputStream stdout) {
        threadLocalRuntimeEnvironment.set(this);
        this.scope = scope;
        addJSFileLibrariesToPath();
        runtime.setGlobalVariables(new ScopeGlobalVariables(scope, runtime));
        createNewClassesAndXGenMethods();
        patchRequireAndLoad();
        setIO(stdin, stdout);
        disallowNewThreads();
    }

    public AppContext getAppContext() { return appContext; }
    public Ruby getRuntime() { return runtime; }

    IRubyObject commonRun(Node node) {
        /* See the second part of JRuby's Ruby.executeScript(String, String). */
        ThreadContext context = runtime.getCurrentContext();

        String oldFile = context.getFile();
        int oldLine = context.getLine();
        try {
            context.setFileAndLine(node.getPosition().getFile(), node.getPosition().getStartLine());
            return runtime.runNormally(node, YARV_COMPILE);
        } finally {
            context.setFile(oldFile);
            context.setLine(oldLine);
        }
    }

    void addCGIEnv(EnvMap env) {
        ThreadContext context = runtime.getCurrentContext();
        RubyHash envHash = (RubyHash)runtime.getObject().fastGetConstant("ENV");
        for (String key : env.keySet())
            envHash.op_aset(context, runtime.newString(key), runtime.newString(env.get(key).toString()));
    }

    private void addJSFileLibrariesToPath() {
        RubyArray loadPath = (RubyArray)runtime.getLoadService().getLoadPath();
        for (String libName : BUILTIN_JS_FILE_LIBRARIES) {
            Object val = scope.get(libName);
            if (!(val instanceof JSFileLibrary))
                continue;
            File root = ((JSFileLibrary)val).getRoot();
            RubyString rubyRoot = runtime.newString(root.getPath().replace('\\', '/'));
            if (loadPath.include_p(runtime.getCurrentContext(), rubyRoot).isFalse()) {
                if (DEBUG)
                    System.err.println("adding file library " + val.toString() + " root " + rubyRoot);
                loadPath.append(rubyRoot);
            }
        }
    }

    private void patchRequireAndLoad() {
        RubyModule kernel = runtime.getKernel();
        kernel.addMethod("require", new JavaMethod(kernel, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    return new Loader(scope).require(context, self, module, name, args, block);
                }
            });
        kernel.addMethod("load", new JavaMethod(kernel, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    return new Loader(scope).load(context, self, module, name, args, block);
                }
            });
    }


    /**
     * Set Ruby's $stdin, $stdout, STDIN, and STDOUT so that reading and
     * writing go to the right place.
     */
    private void setIO(InputStream stdin, OutputStream stdout) {
        GlobalVariables gvars = runtime.getGlobalVariables();

        /* Ignore warnings about redefining STDIN and STDOUT by sending
         * $stderr to a dummy output stream. */
        IRubyObject oldStderr = gvars.get("$stderr");
        gvars.set("$stderr", new RubyIO(runtime, new ByteArrayOutputStream()));

        if (stdin != null)  {
            RubyIO rstdin = new RubyIO(runtime, stdin);
            runtime.getObject().setConstant("STDIN", rstdin);
            gvars.set("$stdin", rstdin);
        }
        if (stdout != null) {
            RubyIO rstdout = new RubyIO(runtime, stdout);
            runtime.getObject().setConstant("STDOUT", rstdout);
            gvars.set("$stdout", rstdout);
        }

        gvars.set("$stderr", oldStderr);
    }

    private void disallowNewThreads() {
        JavaMethod m = new JavaMethod(runtime.getThread(), PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    throw context.getRuntime().newRuntimeError("Thread.new is not allowed. Use XGen::BabbleThread instead.");
                }
            };
        runtime.getThread().addModuleFunction("new", m);
        runtime.getThread().addModuleFunction("fork", m);
    }
}
