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

import java.lang.ref.WeakReference;
import java.io.*;
import java.util.*;

import org.jruby.*;
import org.jruby.ast.Node;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.IdUtil;
import org.jruby.util.KCode;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.AppContext;
import ed.appserver.AppRequest;
import ed.appserver.JSFileLibrary;
import ed.io.StreamUtil;
import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.lang.cgi.CGIGateway;
import ed.lang.cgi.EnvMap;
import ed.net.httpserver.HttpResponse;
import ed.util.Dependency;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

public class RubyJxpSource extends CGIGateway {

    public static final String XGEN_MODULE_NAME = "XGen";
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final Map<Ruby, Set<IRubyObject>> _requiredJSFileLibFiles = new WeakHashMap<Ruby, Set<IRubyObject>>();
    static final Map<AppContext, WeakReference<Ruby>> _runtimes = new WeakHashMap<AppContext, WeakReference<Ruby>>();
    static final Map<String, Long> _localFileLastModTimes = new HashMap<String, Long>();
    static final Ruby PARSE_RUNTIME = Ruby.newInstance();

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB");
    static final boolean SKIP_REQUIRED_LIBS = Boolean.getBoolean("DEBUG.RB.SKIP.REQ.LIBS");
    /** Scope top-level functions to avoid loading. */
    static final Collection<String> DO_NOT_LOAD_FUNCS;
    // TODO for now, we just add the local site dir to the load path
//     static final String[] BUILTIN_JS_FILE_LIBRARIES = {"local", "core", "external"};
    static final String[] BUILTIN_JS_FILE_LIBRARIES = {"local"};
    static final RubyInstanceConfig config = new RubyInstanceConfig();

    static {
        // Don't pre-load XGen::Mongo::Base because it uses /core/js/sql, and
        // that isn't on the path this early in the game. Since Base was the
        // only thing that we pre-loaded here, I've commented this out.
        // Uncomment and re-add xgen.rb if there's anything we always want
        // required.
//         if (!SKIP_REQUIRED_LIBS)
//             config.requiredLibraries().add("xgen");
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

    /**
     * Creates Ruby classes and XGen module methods from any new JavaScript
     * classes and functions found in the top level of <var>scope</var>.
     * Called immediately after loading a file using a JSFileLibrary.
     */
    public static void createNewClassesAndXGenMethods(Scope scope, Ruby runtime) {
        RubyModule xgen = runtime.getOrCreateModule(XGEN_MODULE_NAME);
        Set<String> alreadySeen = new HashSet<String>();
        Scope s = scope;
        while (s != null) {
            for (String key : s.keySet()) {
                if (alreadySeen.contains(key) || DO_NOT_LOAD_FUNCS.contains(key))
                    continue;
                final Object obj = s.get(key);
                if (isCallableJSFunction(obj)) {
                    if (DEBUG)
                        System.err.println("adding top-level method " + key);
                    alreadySeen.add(key);
                    // Creates method and attaches to the module. Also creates a new Ruby class if appropriate.
                    RubyObjectWrapper.createRubyMethod(scope, runtime, (JSFunction)obj, key, xgen, null);
                }
            }
            s = s.getParent();
        }
    }

    public static synchronized Ruby getRuntimeInstance(AppContext ac) {
        if (ac == null)
            return Ruby.newInstance(config);
        WeakReference<Ruby> ref = null;
        ref = _runtimes.get(ac);
        if (ref == null) {
            Ruby runtime = Ruby.newInstance(config);
            _runtimes.put(ac, ref = new WeakReference<Ruby>(runtime));
        }
        return ref.get();
    }

    public static synchronized void forgetRuntimeInstance(AppContext ac) {
        if (ac != null) {
            WeakReference<Ruby> ref = _runtimes.remove(ac);
            if (ref != null) {
                Ruby runtime = ref.get();
                _requiredJSFileLibFiles.remove(runtime);
            }
        }
    }

    public RubyJxpSource(File f , JSFileLibrary lib, boolean isCGI) {
        this(f, lib, isCGI, null);
    }

    /** For testing and {@link RubyLanguage} use. */
    protected RubyJxpSource(File f, JSFileLibrary lib, boolean isCGI, Ruby runtime) {
        _file = f;
        _lib = lib;
        _isCGI = isCGI;
        _runtime = runtime;
    }

    public synchronized Ruby getRuntime(Scope s) {
        return s == null ? getRuntime((AppContext)null) : getRuntime((AppContext)s.get("__instance__"));
    }

    public synchronized Ruby getRuntime(AppContext ac) {
        if (_runtime == null)
            _runtime = getRuntimeInstance(ac);
        return _runtime;
    }

    /**
     * Really only for testing. Always use {@link #getRuntime(Scope)} or
     * {@link #getRuntime(AppContext)} unless you know that a runtime has
     * already been assigned.
     */
    protected Ruby getRuntime() {
        return getRuntime((AppContext)null);
    }

    protected void forgetRuntime(Scope s) {
        _runtime = null;
        if (s != null)
            forgetRuntimeInstance((AppContext)s.get("__instance__"));
    }

    protected String getContent() throws IOException {
        return StreamUtil.readFully(_file);
    }

    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(_file);
    }

    public long lastUpdated(Set<Dependency> visitedDeps) {
        return _file.lastModified();
    }

    public String getName() {
        return _file.toString();
    }

    public File getFile() {
        return _file;
    }

    public JSFunction getFunction() throws IOException {
        if (_isCGI)
            return super.getFunction();

        final Node node = _parseCode();
        return new ed.js.func.JSFunctionCalls0() {
            public Object call(Scope s, Object unused[]) { return RubyObjectWrapper.toJS(s, _doCall(node, s, unused)); }
        };
    }

    protected IRubyObject _doCall(Node node, Scope s, Object unused[]) {
        if (_anyLocalFileChanged(s)) {
            if (DEBUG)
                System.err.println("new file or file mod time changed; resetting Ruby runtime");
            forgetRuntime(s);
        }

        _addJSFileLibrariesToPath(s);

        Ruby runtime = getRuntime(s);
        runtime.setGlobalVariables(new ScopeGlobalVariables(s, runtime));
        _setOutput(s);
        _exposeScopeFunctions(s);
        _patchRequireAndLoad(s);

        // See the second part of JRuby's Ruby.executeScript(String, String)
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

    public void handle(EnvMap env, InputStream stdin, OutputStream stdout, AppRequest ar) {
        Scope s = ar.getScope();

        if (_anyLocalFileChanged(s)) {
            if (DEBUG)
                System.err.println("new file or file mod time changed; resetting Ruby runtime");
            forgetRuntime(s);
        }

        _addJSFileLibrariesToPath(s);
        _addCGIEnv(s, env);

        Ruby runtime = getRuntime(s);
        runtime.setGlobalVariables(new ScopeGlobalVariables(s, runtime));
        _setIO(s, stdin, stdout);
        _exposeScopeFunctions(s);
        _patchRequireAndLoad(s);

        // See the second part of JRuby's Ruby.executeScript(String, String)
        ThreadContext context = runtime.getCurrentContext();

        String oldFile = context.getFile();
        int oldLine = context.getLine();
        try {
            Node node = _parseCode();
            context.setFileAndLine(node.getPosition().getFile(), node.getPosition().getStartLine());
            runtime.runNormally(node, YARV_COMPILE);
        }
        catch (IOException e) {
            System.err.println("RubyJxpSonrce.handle: " + e);
        }
        finally {
            context.setFile(oldFile);
            context.setLine(oldLine);
        }
    }

    protected synchronized Node _parseCode() throws IOException {
        final long lastModified = _file.lastModified();
        if (_node == null || _lastCompile < lastModified) {
            _node = _parseContent(_file.getPath());
            _lastCompile = lastModified;
        }
        return _node;
    }

    protected Node _parseContent(String filePath) throws IOException {
        // See the first part of JRuby's Ruby.executeScript(String, String)
        String script = getContent();
        byte[] bytes;
        try {
            bytes = script.getBytes(KCode.NONE.getKCode());
        } catch (UnsupportedEncodingException e) {
            bytes = script.getBytes();
        }
        return PARSE_RUNTIME.parseFile(new ByteArrayInputStream(bytes), filePath, null);
    }

    protected void _addJSFileLibrariesToPath(Scope s) {
        Ruby runtime = getRuntime(s);
        RubyArray loadPath = (RubyArray)runtime.getLoadService().getLoadPath();
        for (String libName : BUILTIN_JS_FILE_LIBRARIES) {
            Object val = s.get(libName);
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

    /** Copies <var>env</var> into ENV[]. */
    protected void _addCGIEnv(Scope s, EnvMap env) {
        Ruby runtime = getRuntime(s);
        ThreadContext context = runtime.getCurrentContext();
        RubyHash envHash = (RubyHash)runtime.getObject().fastGetConstant("ENV");
        for (String key : env.keySet())
            envHash.op_aset(context, runtime.newString(key), runtime.newString(env.get(key).toString()));
    }

    /**
     * Set Ruby's $stdout so that print/puts statements output to the right
     * place. If we have no HttpResponse (for example, we're being run outside
     * the app server), then nothing happens.
     */
    protected void _setOutput(Scope s) {
        HttpResponse response = (HttpResponse)s.get("response");
        if (response != null) {
            Ruby runtime = getRuntime(s);
            runtime.getGlobalVariables().set("$stdout", new RubyIO(runtime, new RubyJxpOutputStream(response.getJxpWriter())));
        }
    }

    /**
     * Set Ruby's $stdin and $stdout so that reading and writing go to the
     * right place. Called from {@link handle} which is called from the CGI
     * gateway.
     */
    protected void _setIO(Scope s, InputStream stdin, OutputStream stdout) {
        HttpResponse response = (HttpResponse)s.get("response");
        if (response != null) {
            Ruby runtime = getRuntime(s);
            runtime.getGlobalVariables().set("$stdin", new RubyIO(runtime, stdin));
            runtime.getGlobalVariables().set("$stdout", new RubyIO(runtime, stdout));
        }
    }

    /**
     * Creates the $scope global object and sets up the XGen module with
     * top-level functions defined in the scope.
     */
    protected void _exposeScopeFunctions(Scope scope) {
        Ruby runtime = getRuntime(scope);
        runtime.getGlobalVariables().set("$scope", toRuby(scope, runtime, scope));

        // Creates a module named XGen, includes it in the Object class (just
        // like Kernel), and adds all top-level JavaScript methods to the
        // module.
        RubyModule xgen = runtime.getOrCreateModule(XGEN_MODULE_NAME);
        runtime.getObject().includeModule(xgen);
        createNewClassesAndXGenMethods(scope, runtime);
    }

    protected void _patchRequireAndLoad(final Scope scope) {
        RubyModule kernel = getRuntime(scope).getKernel();
        kernel.addMethod("require", new JavaMethod(kernel, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    Ruby runtime = self.getRuntime();
                    String file = args[0].convertToString().toString();

                    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("require " + file);
                    try {
                        return runtime.getLoadService().require(file) ? runtime.getTrue() : runtime.getFalse();
                    }
                    catch (RaiseException re) {
                        if (_notAlreadyRequired(runtime, args[0])) {
                            loadLibraryFile(scope, runtime, self, file, re);
                            _rememberAlreadyRequired(runtime, args[0]);
                        }
                        return runtime.getTrue();
                    }
                }
            });
        kernel.addMethod("load", new JavaMethod(kernel, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    Ruby runtime = self.getRuntime();
                    RubyString file = args[0].convertToString();
                    boolean wrap = args.length == 2 ? args[1].isTrue() : false;

                    if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("load " + file);
                    try {
                        runtime.getLoadService().load(file.getByteList().toString(), wrap);
                        return runtime.getTrue();
                    }
                    catch (RaiseException re) {
                        return loadLibraryFile(scope, runtime, self, file.toString(), re);
                    }
                }
            });
    }

    protected IRubyObject loadLibraryFile(Scope scope, Ruby runtime, IRubyObject recv, String path, RaiseException re) {
        if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("going to compile and run library file " + path + "; runtime = " + runtime);

        JSFileLibrary lib = getLibFromPath(path, scope);
        if (lib == null)
            lib = (JSFileLibrary)scope.get("local");
        else
            path = removeLibName(path);

        try {
            Object o = lib.getFromPath(path);
            if (isCallableJSFunction(o)) {
                try {
                    ((JSFunction)o).call(scope, EMPTY_OBJECT_ARRAY);
                    createNewClassesAndXGenMethods(scope, runtime);
                }
                catch (Exception e) {
                    if (DEBUG || RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                        System.err.println("problem loading JSFileLibrary file: " + e + "; going to raise Ruby error after printing the stack trace here");
                        e.printStackTrace();
                    }
                    recv.callMethod(runtime.getCurrentContext(), "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
                }
                return runtime.getTrue();
            }
        }
        catch (Exception e) {
            if (DEBUG || RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                System.err.println("problem loading JSFileLibrary file: " + e + "; going to re-throw original Ruby RaiseException after printing the stack trace here");
                e.printStackTrace();
            }
            /* fall through to throw re */
        }
        if (DEBUG || RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("problem loading file " + path + " from lib " + lib + "; throwing original Ruby error " + re);
        throw re;
    }

    /**
     * Returns a JSFileLibrary named at the start of <var>path</var>, which is
     * something like "local/foo" or "/core/core/routes". The first word
     * ("local" or "core") must be the name of a JSFileLibrary that is in the
     * scope. Returns <code>null</code> if no library is found.
     */
    public JSFileLibrary getLibFromPath(String path, Scope scope) {
        String libName = libNameFromPath(path);
        return (JSFileLibrary)scope.get(libName);
    }

    public String libNameFromPath(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        int loc = path.indexOf("/");
        return (loc == -1) ? path : path.substring(0, loc);
    }

    /**
     * Returns a new copy of <var>path</var> with the first part of the path
     * stripped off.
     */
    public String removeLibName(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        int loc = path.indexOf("/");
        return (loc == -1) ? "" : path.substring(loc + 1);
    }

    protected boolean _notAlreadyRequired(Ruby runtime, IRubyObject arg) {
        synchronized (_requiredJSFileLibFiles) {
            Set<IRubyObject> reqs = _requiredJSFileLibFiles.get(runtime);
            return reqs == null || !reqs.contains(arg);
        }
    }

    protected void _rememberAlreadyRequired(Ruby runtime, IRubyObject arg) {
        synchronized (_requiredJSFileLibFiles) {
            Set<IRubyObject> reqs = _requiredJSFileLibFiles.get(runtime);
            if (reqs == null) {
                reqs = new HashSet<IRubyObject>();
                _requiredJSFileLibFiles.put(runtime, reqs);
            }
            reqs.add(arg);
        }
    }

    private boolean _anyLocalFileChanged(Scope s) {
        return false;
        // This code is commented out for now because when run against an app
        // with 1,000 froze Rails files, doing this for every request is too
        // slow.
//         if (s == null)
//             return false;
//         JSFileLibrary lib = (JSFileLibrary)s.get("local");
//         if (lib == null)
//             return false;
//         synchronized (_localFileLastModTimes) {
//             return _anyLocalFileChanged(lib.getRoot(), false);
//         }
    }

    private boolean _anyLocalFileChanged(File dir, boolean changed) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                changed = _anyLocalFileChanged(f, changed);
            else {
                String path = f.getPath();
                Long newModTime = new Long(f.lastModified());
                Long oldModTime = _localFileLastModTimes.get(path);
                if (oldModTime == null || !oldModTime.equals(newModTime)) {
                    changed = true;
                    _localFileLastModTimes.put(path, newModTime);
                }
            }
        }
        return changed;
    }

    protected final File _file;
    protected final JSFileLibrary _lib;
    protected final boolean _isCGI;
    protected Ruby _runtime;

    protected Node _node;
    protected long _lastCompile;
}
