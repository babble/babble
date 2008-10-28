// AppContext.java

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

package ed.appserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ed.appserver.jxp.*;
import ed.appserver.adapter.AdapterType;
import ed.db.*;
import ed.log.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.lang.*;
import ed.net.httpserver.*;
import ed.util.*;
import ed.lang.python.*;

/**
 * This is the container for an instance of a site on a single server.
 * This can be access via __instance__
 *
 * @anonymous name : {local}, isField : {true}, desc : {Refers to the site being run.}, type: {library}
 * @anonymous name : {core}, isField : {true}, desc : {Refers to corejs.} example : {core.core.mail() calls corejs/core/mail.js}, type : {library}
 * @anonymous name : {external}  isField : {true}, desc : {Refers to the external libraries.}, type : {library}
 * @anonymous name : {db}, isField : {true}, desc : {Refers to the database.}, type : {database}
 * @anonymous name : {setDB} desc : {changes <tt>db</tt> to refer to a different database.} param : {type : (string) name : (dbname) desc : (name of the database to which to connect)}
 * @anonymous name : {SYSOUT} desc : {Prints a string.} param : {type : (string) name : (str) desc : (the string to print)}
 * @anonymous name : {log} desc : {Global logger.} param : {type : (string) name : (str) desc : (the string to log)}
 * @expose
 * @docmodule system.system.__instance__
 */
public class AppContext extends ServletContextBase implements JSObject, Sizable {

    /**
     * @unexpose
     */
    static final boolean DEBUG = AppServer.D;
    /**
     * If these files exist in the directory or parent directories of a file being run, run these files first. Includes _init.js and /~~/core/init.js.
     */

    static final String INIT_FILES[] = new String[]{"/~~/core/init.js", "PREFIX_init"};

    /**
     * Initializes a new context for a given site directory.
     *
     * @param f the file to run
     */
    public AppContext(File f) {
        this(f.toString());
    }

    /**
     * Initializes a new context for a given site's path.
     *
     * @param root the path to the site from where ed is being run
     */
    public AppContext(String root) {
        this(root, guessNameAndEnv(root).name, guessNameAndEnv(root).env);
    }

    /**
     * Initializes a new context.
     *
     * @param root        the path to the site
     * @param name        the name of the site
     * @param environment the version of the site
     */
    public AppContext(String root, String name, String environment) {
        this(root, new File(root), name, environment);
    }

    /**
     * Initializes a new context.
     *
     * @param root        the path to the site
     * @param rootFile    the directory in which the site resides
     * @param name        the name of the site
     * @param environment the version of the site
     */
    public AppContext(String root, File rootFile, String name, String environment) {
        this(root, rootFile, name, environment, null);
    }

    private AppContext(String root, File rootFile, String name, String environment, AppContext nonAdminParent) {
        super(name + ":" + environment);
        if (root == null)
            throw new NullPointerException("AppContext root can't be null");

        if (rootFile == null)
            throw new NullPointerException("AppContext rootFile can't be null");

        if (name == null)
            name = guessNameAndEnv(root).name;

        if (name == null)
            throw new NullPointerException("how could name be null");

        _root = root;
        _rootFile = rootFile;
        _name = name;
        _environment = environment;
        _nonAdminParent = nonAdminParent;
        _admin = _nonAdminParent != null;
        _codePrefix = _admin ? "/~~/modules/admin/" : "";

        _gitBranch = GitUtils.hasGit(_rootFile) ? GitUtils.getBranchOrTagName(_rootFile) : null;

        _isGrid = name.equals("grid");

        _scope = new Scope("AppContext:" + root + (_admin ? ":admin" : ""), _isGrid ? ed.cloud.Cloud.getInstance().getScope() : Scope.newGlobal(), null, Language.JS, _rootFile);
        _scope.setGlobal(true);
        _initScope = _scope.child("_init");

        _usage = new UsageTracker(this);

        _baseScopeInit();

        _adminContext = _admin ? null : new AppContext(root, rootFile, name, environment, this);

        _contextReachable = new IdentitySet();

        _logger.info("Started Context.  root:" + _root + " environment:" + environment + " git branch: " + _gitBranch);

    }

    /**
     *  Returns the adapter type for the given file.  Will first use the
     *  adapter selector function if it was specified in init.js, otherwise
     *  will use the static type (either set in _init file, as a server-wide
     *  override in 10gen.properties, or default of DIRECT_10GEN)
     *
     * @param file to produce type for
     * @return adapter type for the specified file
     */
    public AdapterType getAdapterType(File file) {

        // Q : I think this is the right thing to do
        if (inScopeSetup()) {
            return AdapterType.DIRECT_10GEN;
        }

        /*
         * cheap hack - prevent any _init.* file from getting run as anythign but DIRECT_10GEN
         */

        if (file != null && file.getName().indexOf("_init.") != -1) {
            return AdapterType.DIRECT_10GEN;
        }

        if (_adapterSelector == null) {
            return _staticAdapterType;
        }

        /*
         *  only let the app select type if file is part of application (i.e.
         *  don't do it for corejs, core modules, etc...
         */

        String fp = file.getAbsolutePath();
        String fullRoot = _rootFile.getAbsolutePath();  // there must be a nicer way to do this?
        
        if (!fp.startsWith(fullRoot)) {
            return AdapterType.DIRECT_10GEN;
        }

        Object o = _adapterSelector.call(_initScope, new JSString(fp.substring(fullRoot.length())));

        if (o == null) {
            return _staticAdapterType;
        }

        if (!(o instanceof JSString)) {
            log("Error : adapter selector not returning string.  Ignoring and using static adapter type");
            return _staticAdapterType;
        }

        AdapterType t = _getAdapterTypeFromString(o.toString());
        
        return (t == null ? _staticAdapterType : t);
    }

    /**
     * Creates a copy of this context.
     *
     * @return an identical context
     */
    AppContext newCopy() {
        return new AppContext(_root, _rootFile, _name, _environment, _nonAdminParent);
    }

    /**
     * Initializes the base scope for the application
     */
    private void _baseScopeInit() {
        // --- libraries

        if (_admin)
            _scope.put("local", new JSObjectBase(), true);
        else
            _setLocalObject(new JSFileLibrary(_rootFile, "local", this));

        _loadConfig();

        _core = CoreJS.get().getLibrary(getCoreJSVersion(), this, null, true);
        _logger.info("corejs : " + _core.getRoot());
        _scope.put("core", _core, true);

        _external = Module.getModule("external").getLibrary(null, this, null, true);
        _scope.put("external", _external, true);

        _scope.put("__instance__", this, true);
        _scope.lock("__instance__");

        // --- db

        if (!_isGrid) {
            _scope.put("db", DBProvider.get(this), true);
            _scope.put("setDB", new JSFunctionCalls1() {

                public Object call(Scope s, Object name, Object extra[]) {
                    if (name.equals(_lastSetTo))
                        return true;

                    DBBase db = (DBBase) AppContext.this._scope.get("db");
                    if (!db.allowedToAccess(name.toString()))
                        throw new JSException("you are not allowed to access db [" + name + "]");

                    if (name.equals(db.getName()))
                        return true;

                    AppContext.this._scope.put("db", DBProvider.get(AppContext.this, name.toString()), false);
                    _lastSetTo = name.toString();

                    if (_adminContext != null) {
                        // yes, i do want a new copy so Constructors don't get copied for both
                        _adminContext._scope.put("db", DBProvider.get(AppContext.this, name.toString()), false);
                    }

                    return true;
                }

                String _lastSetTo = null;

            }, true);
        }

        // --- output

        _scope.put("SYSOUT", new JSFunctionCalls1() {
            public Object call(Scope s, Object str, Object foo[]) {
                System.out.println(AppContext.this._name + " \t " + str);
                return true;
            }
        }, true);

        _scope.put("log", _logger, true);

        // --- random?

        _scope.put("openFile", new JSFunctionCalls1() {
            public Object call(Scope s, Object name, Object extra[]) {
                return new JSLocalFile(_rootFile, name.toString());
            }
        }, true);

        _scope.put("globalHead", _globalHead, true);

        Map<String, JSFileLibrary> rootFileMap = new HashMap<String, JSFileLibrary>();
        for (String rootKey : new String[]{"local", "core", "external"}) {
            Object temp = _scope.get(rootKey);
            if (temp instanceof JSFileLibrary)
                rootFileMap.put(rootKey, (JSFileLibrary) temp);
        }

        _scope.put("fork", new JSFunctionCalls1() {
            public Object call(final Scope scope, final Object funcJS, final Object extra[]) {

                if (!(funcJS instanceof JSFunction))
                    throw new JSException("fork has to take a function");

                return queueWork("forked", (JSFunction) funcJS, extra);
            }
        }
        );
        _scope.lock("fork");

        ed.appserver.templates.djang10.JSHelper.install(_scope, rootFileMap, _logger);

        _scope.lock("user"); // protection against global user object

    }

    private void _loadConfig() {
        try {

            File f;
            if (!_admin)
                f = getFileSafe("_config.js");
            else
                f = new File(Module.getModule("core-modules/admin").getRootFile(getVersionForLibrary("admin")), "_config.js");

            _libraryLogger.info("config file [" + f + "] exists:" + f.exists());

            if (f == null || !f.exists())
                return;

            Set<String> newThings = new HashSet<String>(_scope.keySet());

            Convert c = new Convert(f);
            c.get().call(_scope);

            newThings.removeAll(_scope.keySet());

            for (String newKey : newThings) {
                Object val = _scope.get(newKey);
                if (val instanceof String || val instanceof JSString)
                    _initParams.put(newKey, val.toString());
            }

        }
        catch (Exception e) {
            throw new RuntimeException("couldn't load config", e);
        }

    }

    /**
     * Get the version of corejs to run for this AppContext.
     *
     * @return the version of corejs as a string. null if should use default
     */
    public String getCoreJSVersion() {
        Object o = _scope.get("corejsversion");
        if (o != null) {
            _logger.error("you are using corejsversion which is deprecated.  please use version.corejs");
            return JS.toString(o);
        }

        return getVersionForLibrary("corejs");
    }

    /**
     * Get the version of a library to run.
     *
     * @param name the name of the library to look up
     * @return the version of the library to run as a string.  null if should use default
     */
    public String getVersionForLibrary(String name) {
        String version = getVersionForLibrary(_scope, name, this);
        _libraryVersions.set(name, version);
        return version;
    }

    public JSObject getLibraryVersionsLoaded() {
        return _libraryVersions;
    }

    /**
     * @unexpose
     */
    public static String getVersionForLibrary(Scope s, String name) {
        AppRequest ar = AppRequest.getThreadLocal();
        return getVersionForLibrary(s, name, ar == null ? null : ar.getContext());
    }


    /**
     * @unexpose
     */
    public static String getVersionForLibrary(Scope s, String name, AppContext ctxt) {
        final String version = _getVersionForLibrary(s, name, ctxt);
        _libraryLogger.log(ctxt != null && !ctxt._admin ? Level.DEBUG : Level.INFO, ctxt + "\t" + name + "\t" + version);
        return version;
    }

    private static String _getVersionForLibrary(Scope s, String name, AppContext ctxt) {
        final JSObject o1 = ctxt == null ? null : (JSObject) (s.get("version_" + ctxt.getEnvironmentName()));
        final JSObject o2 = (JSObject) s.get("version");

        _libraryLogger.debug(ctxt + "\t versionConfig:" + (o1 != null) + " config:" + (o2 != null));

        String version = _getString(name, o1, o2);
        if (version != null)
            return version;

        if (ctxt == null || ctxt._nonAdminParent == null)
            return null;

        return ctxt._nonAdminParent.getVersionForLibrary(name);
    }

    private static String _getString(String name, JSObject... places) {
        for (JSObject o : places) {
            if (o == null)
                continue;
            Object temp = o.get(name);
            if (temp == null)
                continue;
            return temp.toString();
        }
        return null;
    }


    /**
     * @return [ <name> , <env> ]
     */
    static NameAndEnv guessNameAndEnv(String root) {
        root = ed.io.FileUtil.clean(root);
        root = root.replaceAll("\\.+/", "");
        String pcs[] = root.split("/+");

        if (pcs.length == 0)
            throw new RuntimeException("no root for : " + root);

        // handle anything with sites/foo
        for (int i = 0; i < pcs.length - 1; i++)
            if (pcs[i].equals("sites")) {
                return new NameAndEnv(pcs[i + 1], i + 2 < pcs.length ? pcs[i + 2] : null);
            }

        final int start = pcs.length - 1;
        for (int i = start; i > 0; i--) {
            String s = pcs[i];

            if (i == start &&
                    (s.equals("master") ||
                            s.equals("test") ||
                            s.equals("www") ||
                            s.equals("staging") ||
                            //s.equals("stage") ||
                            s.equals("dev")))
                continue;

            return new NameAndEnv(s, i + 1 < pcs.length ? pcs[i + 1] : null);
        }

        return new NameAndEnv(pcs[0], pcs.length > 1 ? pcs[1] : null);
    }

    static class NameAndEnv {
        NameAndEnv(String name, String env) {
            this.name = name;
            this.env = env;
        }

        final String name;
        final String env;

    }

    /**
     * Returns the name of the site being run.
     *
     * @return the name of the site
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the database being used.
     *
     * @return The database being used
     */
    public DBBase getDB() {
        return (DBBase) _scope.get("db");
    }

    /**
     * Given the _id of a JSFile, return the file.
     *
     * @param id _id of the file to find
     * @return The file, if found, otherwise null
     */
    JSFile getJSFile(String id) {

        if (id == null)
            return null;

        DBCollection f = getDB().getCollection("_files");
        return (JSFile) (f.find(new ObjectId(id)));
    }

    /**
     * Returns (and if necessary, reinitializes) the scope this context is using.
     *
     * @return the scope
     */
    public Scope getScope() {
        return _scope();
    }

    public Scope getInitScope() {
        return _initScope;
    }

    Object getFromInitScope(String what) {
        if (!_knownInitScopeThings.contains(what))
            System.err.println("*** Unknown thing requested from initScope [" + what + "]");
        return _initScope.get(what);
    }

    void setTLPreferredScope(AppRequest req, Scope s) {
        _scope.setTLPreferred(s);
    }

    private synchronized Scope _scope() {

        if (_inScopeSetup)
            return _scope;

        if (_getScopeTime() > _lastScopeInitTime)
            _scopeInited = false;

        if (_scopeInited)
            return _scope;

        _scopeInited = true;
        _lastScopeInitTime = System.currentTimeMillis();

        _setupScope();

        _setStaticAdapterType();
        _setAdapterSelectorFunction();

        return _scope;
    }

    protected void _setAdapterSelectorFunction() {

        Object o = this.getFromInitScope(INIT_ADAPTER_SELECTOR);

        if (o == null) {
            log("Adapter selector function not specified in _init file");
            return;
        }

        if (!(o instanceof JSFunction)) {
            log("Adapter selector function specified in _init file  not a function.  Ignoring. [" + o.getClass() + "]");
            return;
        }

        _adapterSelector = (JSFunction) o;
        log("Adapter selector function specified in _init file");
    }
    
    /**
     *  Figure out what kind of static adapter type was specified.
     *  By default it's a 10genDEFAULT app
     */
    protected void _setStaticAdapterType() {

        /*
         * check to see if overridden in 10gen.properties
         */
        String override = Config.get().getProperty(INIT_ADAPTER_TYPE);

        if (override != null) {
            AdapterType t = _getAdapterTypeFromString(override);

            if (t == null){
                log("Static adapter type specified as override [" + override + "] unknown - will use _init file specified or default");
            }
            else {
                log("Static adapter type overridden by 10gen.properties or env. Value : " + override);
                _staticAdapterType = t;
                return;
            }
        }

        /*
         *  if not, use the one from _init file if specified
         */

        _staticAdapterType = AdapterType.DIRECT_10GEN;
        Object o = getFromInitScope(INIT_ADAPTER_TYPE);

        if (o == null) {
            log("Static adapter type not specified in _init file - using default value of DIRECT_10GEN");
            return;
        }

        if (!(o instanceof JSString)) {
            log("Static adapter type from _init file not a string - using default value of DIRECT_10GEN");
            return;
        }

        _staticAdapterType = _getAdapterTypeFromString(o.toString());

        if(_staticAdapterType == null) {
            log("Static adapter type from _init file [" + o.toString() + "] unknown - using default value of DIRECT_10GEN");
            _staticAdapterType = AdapterType.DIRECT_10GEN;
            return;
        }

        log("Static adapter type specified in _init file = " + _staticAdapterType);

        return;
    }

    protected AdapterType _getAdapterTypeFromString(String s) {

        if (AdapterType.DIRECT_10GEN.toString().equals(s.toUpperCase())) {
            return AdapterType.DIRECT_10GEN;
        }

        if (AdapterType.CGI.toString().equals(s.toUpperCase())) {
            return AdapterType.CGI;
        }

        if (AdapterType.WSGI.toString().equals(s.toUpperCase())) {
            return AdapterType.WSGI;
        }

        return null;
    }

    /**
     * @unexpose
     */
    public File getFileSafe(final String uri) {
        try {
            return getFile(uri);
        }
        catch (FileNotFoundException fnf) {
            return null;
        }
    }

    /**
     * @unexpose
     */
    public File getFile(final String uri)
            throws FileNotFoundException {
        File f = _files.get(uri);

        if (f != null)
            return f;

        if (uri.startsWith("/~~/") || uri.startsWith("~~/"))
            f = _core.getFileFromPath(uri.substring(3));
        else if (uri.startsWith("/admin/"))
            f = _core.getFileFromPath("/modules" + uri);
        else if (uri.startsWith("/@@/") || uri.startsWith("@@/"))
            f = _external.getFileFromPath(uri.substring(3));
        else if (_localObject != null && uri.startsWith("/modules/"))
            f = _localObject.getFileFromPath(uri);
        else
            f = new File(_rootFile, uri);

        if (f == null)
            throw new FileNotFoundException(uri);

        _files.put(uri, f);
        return f;
    }

    public String getRealPath(String path) {
        try {
            return getFile(path).getAbsolutePath();
        }
        catch (FileNotFoundException fnf) {
            throw new RuntimeException("file not found [" + path + "]");
        }
    }

    public URL getResource(String path) {
        try {
            File f = getFile(path);
            if (!f.exists())
                return null;
            return f.toURL();
        }
        catch (FileNotFoundException fnf) {
            // the spec says to return null if we can't find it
            // even though this is weird...
            return null;
        }
        catch (IOException ioe) {
            throw new RuntimeException("error opening [" + path + "]", ioe);
        }
    }

    public InputStream getResourceAsStream(String path) {
        URL url = getResource(path);
        if (url == null)
            return null;
        try {
            return url.openStream();
        }
        catch (IOException ioe) {
            throw new RuntimeException("can't getResourceAsStream [" + path + "]", ioe);
        }
    }

    /**
     * This causes the AppContext to be started over.
     * All context level variable will be lost.
     * If code is being managed, will cause it to check that its up to date.
     */
    public void reset() {
        _reset = true;
    }

    /**
     * Checks if this context has been reset.
     */
    public boolean isReset() {
        return _reset;
    }

    /**
     * Returns the path to the directory the appserver is running. (For example, site/version.)
     *
     * @return the path
     */
    public String getRoot() {
        return _root;
    }

    /**
     * Creates an new request for the app server from an HTTP request.
     *
     * @param request HTTP request to create
     * @return the request
     */
    public AppRequest createRequest(HttpRequest request) {
        return createRequest(request, request.getHost(), request.getURI());
    }

    /**
     * Creates an new request for the app server from an HTTP request.
     *
     * @param request HTTP request to create
     * @param uri     the URI requested
     * @return the request
     */
    public AppRequest createRequest(HttpRequest request, String host, String uri) {
        _numRequests++;

        if (AppRequest.isAdmin(request))
            return new AppRequest(_adminContext, request, host, uri);


        return new AppRequest(this, request, host, uri);
    }

    /**
     * Tries to find the given file, assuming that it's missing the ".jxp" extension
     *
     * @param f File to check
     * @return same file if not found to be missing the .jxp, or a new File w/ the .jxp appended
     */
    File tryNoJXP(File f) {
        if (f.exists())
            return f;

        if (f.getName().indexOf(".") >= 0)
            return f;

        File temp = new File(f.toString() + ".jxp");
        return temp.exists() ? temp : f;
    }

    File tryOtherExtensions(File f) {
        if (f.exists())
            return f;

        if (f.getName().indexOf(".") >= 0)
            return f;

        for (int i = 0; i < JSFileLibrary._srcExtensions.length; i++) {
            File temp = new File(f.toString() + JSFileLibrary._srcExtensions[i]);
            if (temp.exists())
                return temp;
        }

        return f;
    }

    /**
     * Maps a servlet-like URI to a jxp file.
     *
     * @param f File to check
     * @return new File with <root>.jxp if exists, orig file if not
     * @example /wiki/geir  ->  maps to wiki.jxp if exists
     */
    File tryServlet(File f) {
        if (f.exists())
            return f;

        String uri = f.toString();

        if (uri.startsWith(_rootFile.toString()))
            uri = uri.substring(_rootFile.toString().length());

        if (_core != null && uri.startsWith(_core._base.toString()))
            uri = "/~~" + uri.substring(_core._base.toString().length());

        while (uri.startsWith("/"))
            uri = uri.substring(1);

        int start = 0;
        while (true) {

            int idx = uri.indexOf("/", start);
            if (idx < 0)
                break;
            String foo = uri.substring(0, idx);

            File temp = getFileSafe(foo + ".jxp");

            if (temp != null && temp.exists())
                f = temp;

            start = idx + 1;
        }

        return f;
    }

    /**
     * Returns the index.jxp for the File argument if it's an existing directory,
     * and the index.jxp file exists
     *
     * @param f directory to check
     * @return new File for index.jxp in that directory, or same file object if not
     */
    File tryIndex(File f) {

        if (!(f.isDirectory() && f.exists()))
            return f;

        for (int i = 0; i < JSFileLibrary._srcExtensions.length; i++) {
            File temp = new File(f, "index" + JSFileLibrary._srcExtensions[i]);
            if (temp.exists())
                return temp;
        }

        return f;
    }


    JxpSource getSource(File f)
            throws IOException {

        if (DEBUG) System.err.println("getSource\n\t " + f);

        File temp = _findFile(f);

        if (DEBUG) System.err.println("\t " + temp);

        if (!temp.exists())
            return handleFileNotFound(f);

        //  if it's a directory (and we know we can't find the index file)
        //  TODO : at some point, do something where we return an index for the dir?
        if (temp.isDirectory())
            return null;

        // if we are at init time, save it as an initializaiton file
        loadedFile(temp);

        // Ensure that this is w/in the right tree for the context
        if (_localObject != null && _localObject.isIn(temp))
            return _localObject.getSource(temp);

        // if not, is it core?
        if (_core.isIn(temp))
            return _core.getSource(temp);

        throw new RuntimeException("what?  can't find:" + f);
    }

    /**
     * Finds the appropriate file for the given path.
     * <p/>
     * We have a hierarchy of attempts as we try to find a file :
     * <p/>
     * 1) first, see if it exists as is, or if it's really a .jxp w/o the extension
     * 2) next, see if it can be deconstructed as a servlet such that /foo/bar maps to /foo.jxp
     * 3) See if we can find the index file for it if a directory
     */
    File _findFile(File f) {

        File temp;

        if ((temp = tryNoJXP(f)) != f) {
            return temp;
        }

        if ((temp = tryOtherExtensions(f)) != f) {
            return temp;
        }

        if ((temp = tryServlet(f)) != f) {
            return temp;
        }

        if ((temp = tryIndex(f)) != f) {
            return temp;
        }


        return f;
    }

    public void loadedFile(File f) {
        if (_inScopeSetup)
            _initFlies.add(f);
    }

    public void addInitDependency(File f) {
        _initFlies.add(f);
    }

    JxpServlet getServlet(File f)
            throws IOException {
        JxpSource source = getSource(f);
        if (source == null)
            return null;
        return source.getServlet(this);
    }

    private void _setupScope() {
        if (_inScopeSetup)
            return;

        final Scope saveTLPref = _scope.getTLPreferred();
        _scope.setTLPreferred(null);

        final Scope saveTL = Scope.getThreadLocal();
        _scope.makeThreadLocal();

        _inScopeSetup = true;

        try {
            _runInitFiles(INIT_FILES);

            if (_adminContext != null) {
                _adminContext._scope.set("siteScope", _scope);
                _adminContext._setLocalObject(_localObject);
            }

            _lastScopeInitTime = _getScopeTime();
        }
        catch (RuntimeException re) {
            _scopeInited = false;
            throw re;
        }
        catch (Exception e) {
            _scopeInited = false;
            throw new RuntimeException(e);
        }
        finally {
            _inScopeSetup = false;
            _scope.setTLPreferred(saveTLPref);

            if (saveTL != null)
                saveTL.makeThreadLocal();

            this.approxSize(_contextReachable);
        }

    }

    public boolean inScopeSetup() {
        return _inScopeSetup;
    }

    private void _runInitFiles(String[] files)
            throws IOException {

        if (files == null)
            return;

        for (int i = 0; i < files.length; i++)
            _runInitFile(tryOtherExtensions(getFile(files[i].replaceAll("PREFIX", _codePrefix))));
    }

    private void _runInitFile(File f)
            throws IOException {
        if (f == null)
            return;

        if (!f.exists())
            return;

        _initFlies.add(f);
        JxpSource s = getSource(f);
        JSFunction func = s.getFunction();
        func.setUsePassedInScope(true);
        func.call(_initScope);
    }

    long _getScopeTime() {
        long last = 0;
        for (File f : _initFlies)
            if (f.exists())
                last = Math.max(last, f.lastModified());
        return last;
    }


    /**
     * Convert this AppContext to a string by returning the name of
     * the directory it's running in.
     *
     * @return the filename of its root directory
     */
    public String toString() {
        return _rootFile.toString();
    }

    public String debugInfo() {
        return _rootFile + " admin:" + _admin;
    }

    public void fix(Throwable t) {
        StackTraceHolder.getInstance().fix(t);
    }

    /**
     * Get a "global" head array. This array contains HTML that will
     * be inserted into the head of every request served by this app
     * context. It's analagous to the <tt>head</tt> array, but
     * persistent.
     *
     * @return a mutable array
     */
    public JSArray getGlobalHead() {
        return _globalHead;
    }

    /**
     * Gets the date of creation for this app context.
     *
     * @return the creation date as a JS Date.
     */
    public JSDate getWhenCreated() {
        return _created;
    }

    /**
     * Gets the number of requests served by this app context.
     *
     * @return the number of requests served
     */
    public int getNumRequests() {
        return _numRequests;
    }

    /**
     * Get the name of the git branch we think we're running.
     *
     * @return the name of the git branch, as a string
     */
    public String getGitBranch() {
        return _gitBranch;
    }

    /**
     * Update the git branch that we're running and return it.
     *
     * @return the name of the git branch, or null if there isn't any
     */
    public String getCurrentGitBranch() {
        if (_gitBranch == null)
            return null;

        if (_gitFile == null)
            _gitFile = new File(_rootFile, ".git/HEAD");

        if (!_gitFile.exists())
            throw new RuntimeException("this should be impossible");

        if (_lastScopeInitTime < _gitFile.lastModified())
            _gitBranch = GitUtils.getBranchOrTagName(_rootFile);

        return _gitBranch;
    }

    /**
     * Get the environment in which this site is running
     *
     * @return the environment name as a string
     */
    public String getEnvironmentName() {
        return _environment;
    }


    /**
     * updates the context to the correct branch based on environment
     * and to the latest version of the code
     * if name or environemnt is missing, does nothing
     */
    public String updateCode() {

        if (!GitUtils.isSourceDirectory(_rootFile))
            throw new RuntimeException(_rootFile + " is not a git repo");

        _logger.info("going to update code");
        GitUtils.fullUpdate(_rootFile);

        if (_name == null || _environment == null)
            return getCurrentGitBranch();

        JSObject env = AppContextHolder.getEnvironmentFromCloud(_name, _environment);
        if (env == null)
            return null;


        String branch = env.get("branch").toString();
        _logger.info("updating to [" + branch + "]");
        AppContextHolder._checkout(_rootFile, branch);
        Python.deleteCachedJythonFiles(_rootFile);

        return getCurrentGitBranch();
    }

    private void _setLocalObject(JSFileLibrary local) {
        _localObject = local;
        _scope.put("local", _localObject, true);
        _scope.put("jxp", _localObject, true);
        _scope.warn("jxp");
    }

    JxpSource handleFileNotFound(File f) {
        String name = f.getName();
        if (name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
            return getJxpServlet(name);
        }

        return null;
    }

    public JxpSource getJxpServlet(String name) {
        JxpSource source = _httpServlets.get(name);
        if (source != null)
            return source;

        try {
            Class c = Class.forName(name);
            Object n = c.newInstance();
            if (!(n instanceof HttpServlet))
                throw new RuntimeException("class [" + name + "] is not a HttpServlet");

            HttpServlet servlet = (HttpServlet) n;
            servlet.init(createServletConfig(name));
            source = new ServletSource(servlet);
            _httpServlets.put(name, source);
            return source;
        }
        catch (Exception e) {
            throw new RuntimeException("can't load [" + name + "]", e);
        }

    }

    ServletConfig createServletConfig(final String name) {
        final Object rawServletConfigs = _scope.get("servletConfigs");
        final Object servletConfigObject = rawServletConfigs instanceof JSObject ? ((JSObject) rawServletConfigs).get(name) : null;
        final JSObject servletConfig;
        if (servletConfigObject instanceof JSObject)
            servletConfig = (JSObject) servletConfigObject;
        else
            servletConfig = null;

        return new ServletConfig() {
            public String getInitParameter(String name) {
                if (servletConfig == null)
                    return null;
                Object foo = servletConfig.get(name);
                if (foo == null)
                    return null;
                return foo.toString();
            }

            public Enumeration getInitParameterNames() {
                Collection keys;
                if (servletConfig == null)
                    keys = new LinkedList();
                else
                    keys = servletConfig.keySet();
                return new CollectionEnumeration(keys);
            }

            public ServletContext getServletContext() {
                return AppContext.this;
            }

            public String getServletName() {
                return name;
            }
        };
    }

    public static AppContext findThreadLocal() {
        AppRequest req = AppRequest.getThreadLocal();
        if (req != null)
            return req._context;

        Scope s = Scope.getThreadLocal();
        if (s != null) {
            Object foo = s.get("__instance__");
            if (foo instanceof AppContext)
                return (AppContext) foo;
        }

        return null;
    }

    public String getInitParameter(String name) {
        return _initParams.get(name);
    }

    public Enumeration getInitParameterNames() {
        return new CollectionEnumeration(_initParams.keySet());
    }

    public String getContextPath() {
        return "";
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        throw new RuntimeException("getNamedDispatcher not implemented");
    }

    public RequestDispatcher getRequestDispatcher(String name) {
        throw new RuntimeException("getRequestDispatcher not implemented");
    }

    public Set getResourcePaths(String path) {
        throw new RuntimeException("getResourcePaths not implemented");
    }

    public AppContext getSiteInstance() {
        if (_nonAdminParent == null)
            return this;
        return _nonAdminParent;
    }

    public long approxSize() {
        return approxSize(new IdentitySet());
    }

    public long approxSize(IdentitySet seen) {
        long size = 0;

        size += _scope.approxSize(seen, false, true);
        size += _initScope.approxSize(seen, true, false);

        size += JSObjectSize.size(_localObject, seen);
        size += JSObjectSize.size(_core, seen);
        size += JSObjectSize.size(_external, seen);

        if (_adminContext != null)
            size += _adminContext.approxSize(seen);

        return size;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public AppWork queueWork(String identifier, JSFunction work, Object... params) {
        return queueWork(new AppWork.FunctionAppWork(this, identifier, work, params));
    }

    public AppWork queueWork(AppWork work) {
        if (_workQueue == null) {
            _workQueue = new ArrayBlockingQueue<AppWork>(100);
            AppWork.addQueue(_workQueue);
        }

        if (_workQueue.offer(work))
            return work;

        throw new RuntimeException("work queue full!");
    }

    public Logger getLogger(String sub) {
        return _logger.getChild(sub);
    }

    // ----  START JSObject INTERFACE

    public Object get(Object n) {
        return _scope.get(n);
    }

    public JSFunction getFunction(String name) {
        return _scope.getFunction(name);
    }

    public final Set<String> keySet() {
        return _scope.keySet();
    }

    public Set<String> keySet(boolean includePrototype) {
        return _scope.keySet(includePrototype);
    }

    public boolean containsKey(String s) {
        return _scope.containsKey(s);
    }

    public boolean containsKey(String s, boolean includePrototype) {
        return _scope.containsKey(s, includePrototype);
    }

    public Object set(Object n, Object v) {
        return _scope.putExplicit(n.toString(), v);
    }

    public Object setInt(int n, Object v) {
        throw new RuntimeException("not allowed");
    }

    public Object getInt(int n) {
        return _scope.getInt(n);
    }

    public Object removeField(Object n) {
        return _scope.removeField(n);
    }

    public JSFunction getConstructor() {
        return null;
    }

    public JSObject getSuper() {
        return null;
    }

    // ----  END BROKEN JSOBJET INTERFACE

    final String _name;
    final String _root;
    final File _rootFile;

    private String _gitBranch;
    final String _environment;
    final boolean _admin;

    final AppContext _adminContext;
    final String _codePrefix;

    final AppContext _nonAdminParent;

    private JSFileLibrary _localObject;
    private JSFileLibrary _core;
    private JSFileLibrary _external;

    final Scope _scope;
    final IdentitySet _contextReachable;
    final Scope _initScope;
    final UsageTracker _usage;

    final JSArray _globalHead = new JSArray();

    private final Map<String, String> _initParams = new HashMap<String, String>();
    private final Map<String, File> _files = Collections.synchronizedMap(new HashMap<String, File>());
    private final Set<File> _initFlies = new HashSet<File>();
    private final Map<String, JxpSource> _httpServlets = Collections.synchronizedMap(new HashMap<String, JxpSource>());
    private final JSObject _libraryVersions = new JSObjectBase();

    private Queue<AppWork> _workQueue;

    boolean _scopeInited = false;
    boolean _inScopeSetup = false;
    long _lastScopeInitTime = 0;

    final boolean _isGrid;

    boolean _reset = false;
    int _numRequests = 0;
    final JSDate _created = new JSDate();


    private File _gitFile = null;
    private long _lastGitCheckTime = 0;

    /*
     *  adapter type - can have either a static ("all files in this app are X")
     *  or dynamic - the provided selector function dynamically chooses, falling
     *  back to the static if it returns null
     */
    public final static String INIT_ADAPTER_TYPE = "adapterType";
    public final static String INIT_ADAPTER_SELECTOR = "adapterSelector";

    private AdapterType _staticAdapterType = AdapterType.DIRECT_10GEN;
    private JSFunction _adapterSelector = null;

    private static Logger _libraryLogger = Logger.getLogger("library.load");

    static {
        _libraryLogger.setLevel(Level.INFO);
    }

    private static final Set<String> _knownInitScopeThings = new HashSet<String>();

    static {
        _knownInitScopeThings.add("mapUrlToJxpFileCore");
        _knownInitScopeThings.add("mapUrlToJxpFile");
        _knownInitScopeThings.add("allowed");
        _knownInitScopeThings.add("staticCacheTime");
        _knownInitScopeThings.add("handle404");
        _knownInitScopeThings.add(INIT_ADAPTER_TYPE);
        _knownInitScopeThings.add(INIT_ADAPTER_SELECTOR);
    }

}
