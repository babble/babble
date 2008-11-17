package ed.appserver.frameworks;

import ed.js.JSON;
import ed.js.JSObjectBase;
import ed.js.JSArray;
import ed.js.JSObject;
import ed.js.JSDict;
import ed.io.StreamUtil;
import ed.appserver.AppContext;
import ed.appserver.adapter.AdapterType;
import ed.log.Level;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PredefinedFrameworks {

    public final static String DOC_FORMAT = "docFormat";
    public final static String ENVIRONMENTS = "environments";
    public final static String DEFAULT_VERSION = "defaultVersion";
    public final static String VERSION = "version";
    public final static String VERSIONS = "versions";
    public final static String ADAPTER_TYPE = "adapterType";
    public final static String PACKAGES = "packages";
    public final static String INIT_FILE = "initFile";
    public final static String MODULE = "module";
    public final static String PATH = "path";
    public final static String NAME = "name";

    protected JSObjectBase _doc;
    protected Map<String, AE> _map = new HashMap<String, AE>();

    /**
     *  Creates the AppEnv object via a InputStream containing the json
     *  object
     *
     * @param is stream of the JSON file that specifies the AppEnv definitions
     * @throws Exception in case of problem
     */
    public PredefinedFrameworks(InputStream is) throws Exception {

        String doc = StreamUtil.readFully(is);
        _doc = (JSObjectBase) JSON.parse(doc);

        digest();
    }

    /**
     *  Setup the context using a "custom" configuration (not one of the preconfigured types)
     *
     * @param customEnv env to setup - same form as a single version in the appenvironments.json file
     * @return framework object for this framework
     */
    public Framework getCustomFramework(JSObjectBase customEnv) {

        final Env env = createEnvVersion(customEnv);

        return new Framework() {
            public void install(AppContext context) throws IOException {
                context.getLogger().log(Level.INFO, "custom environment : " + env.toString());
                _setupContext(context, env, "custom", "-");
            }
        };
    }

    /**
     *  Sets up an app context for a given app environment and version
     *
     * @param name name of framework to setup
     * @param version version of framework to setup.  if null, will use default version
     * @return framework object for this framework
     */
    public Framework getFramework(String name, String version) {

        final String lowerName = name.toLowerCase();
        
        AE ae = _map.get(lowerName);

        if (ae == null) {
            throw new RuntimeException("Error : specified app environment not defined : " + name);
        }
        
        if (version == null || version.equals("")) {
            version = ae.getDefaultVersion();
        }

        final Env env = ae.getEnvironment(version);
        final String ver = version;

        return new Framework() {
            public void install(AppContext context) throws IOException {
                _setupContext(context, env, lowerName, ver);
            }
        };
    }

    protected void _setupContext(AppContext context, Env env, String name, String version) {
        /*
         *   first, set the adapter type directly
         */

        String s = env._adapterType;

        AdapterType at = context.getAdapterTypeFromString(s);

        if (at == null) {
            throw new RuntimeException("Error - no valid adapter type [" + s + "] specified in environment [" + name + ", " + version + "]");
        }

        context.setStaticAdapterTypeValue(at);

        /*
         *  next, if there are any packages, set them as well
         */

        List<Package> pkgs = env._packages;

        if (pkgs.size() > 0) {
            JSObject packages = (JSObject) context.getConfigObject("packages");
            if (packages == null) {
                packages = new JSDict();
                context.setConfigObject("packages", packages);
            }

            for (Package p : pkgs) {
                JSDict d = new JSDict();
                d.set("module", p._module);
                d.set("path", p._path);
                packages.set(p._name, d);  // how do I specify version???
            }
        }

        /*
         *  finally, if there's an init file specified, give it a try...
         */

        if (env._initFile != null && !env._initFile.equals("")) {
            try {
                context.runInitFile(env._initFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     *  Do basic document validation.  Must be of a known format version, and have
     *  environments defined, and they be valid environments
     *
     * @throws Exception if a problem
     */
    public void digest() throws Exception {

        assert(getDocFormat().equals("1"));

        JSObjectBase envs = (JSObjectBase) _doc.get(ENVIRONMENTS);

        assert(envs.keySet().size() > 0);

        for (String s : envs.keySet()) {
            AE ae = createEnv((JSObjectBase) envs.get(s));
            _map.put(s.toLowerCase(), ae);
        }
    }

    /**
     *  Validates a given environment contiained in a JSOBjectBase
     *
     *  Requires that the env defined a defaultVersion, and contains at
     *  least the default version in it's version list
     * 
     * @param env app environment to validate
     * @return fulled AE object
     * @throws Exception if anything amiss
     */
    protected AE createEnv(JSObjectBase env) throws Exception {

        AE ae = new AE();

        ae._defaultVersion = env.getAsString(DEFAULT_VERSION);

        JSArray versions = (JSArray) env.get(VERSIONS);
        assert(versions != null);

        for (String key : versions.keySet()) {

            JSObjectBase verobj = (JSObjectBase) versions.get(key);

            Env ver = createEnvVersion(verobj);
            ae._versions.put(ver._version, ver);
        }

        return ae;
    }

    /**
     *  Creates an evn object from specific env-version information
     * 
     * @param verobj object w/ version env info
     * @return  Env object containing version specific environment info
     */
    protected Env createEnvVersion(JSObjectBase verobj)  {
        Env ver = new Env();

        ver._version = verobj.getAsString(VERSION);
        assert(ver._version != null);

        ver._adapterType = verobj.getAsString(ADAPTER_TYPE);
        assert(ver._adapterType != null);

        ver._initFile = verobj.getAsString(INIT_FILE);

        JSArray arr = (JSArray) verobj.get(PACKAGES);

        if (arr != null) {
            int i=0;
            while (i < arr.size()) {
                JSObjectBase pkg = (JSObjectBase) arr.get(i);
                Package p = new Package();
                ver._packages.add(p);

                p._module = pkg.getAsString(MODULE);
                p._name = pkg.getAsString(NAME);
                p._version = pkg.getAsString(VERSION);
                p._path = pkg.getAsString(PATH);
                i++;
            }
        }

        return ver;
    }

    public String getDocFormat() {
        return _doc.getAsString(DOC_FORMAT);
    }

    public Map<String, AE> getAppEnvironments() {
        return _map;
    }

    public void print() {

        for (String s : _map.keySet()) {

            System.out.println(s  +" : " + _map.get(s).toString());
        }
    }

    public static void main(String[] args) throws Exception {

        PredefinedFrameworks env = new PredefinedFrameworks(
                new FileInputStream("/Users/geir/dev/10gen/ed/conf/appenvironments.json"));

        env.print();
    }

    public static class AE {

        String _defaultVersion;
        Map<String, Env> _versions = new HashMap<String, Env>();

        public String getDefaultVersion() {
            return _defaultVersion;
        }

        public Env getEnvironment() {
            return _versions.get(_defaultVersion);
        }

        public Env getEnvironment(String version) {
            return _versions.get(version);
        }

        public void validate() throws Exception {
            assert(_defaultVersion != null);
        }

        public String toString() {
            String s = "defVer=" + _defaultVersion + " :: ";

            for (String ver : _versions.keySet()) {

                Env env = _versions.get(ver);

                s = s +  env.toString() + "|";
            }

            return s;
        }
    }

    public static class Env {
        String _adapterType;
        String _version;
        String _initFile;
        List<Package> _packages= new ArrayList<Package>();

        public String toString() {
            String s = "ver=" + _version + " adapter=" + _adapterType;

            for (Package p : _packages) {
                s = s + p.toString();
            }

            return s;
        }
    }

    public static class Package {
        String _name;
        String _module;
        String _version;
        String _path;

        public String toString() {
            return "{" + _name + ":" + _module + "|" + _path + ":" + _version + "}";
        }
    }
}
