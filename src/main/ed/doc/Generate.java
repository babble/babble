package ed.doc;

import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ArrayList;

import ed.js.*;
import ed.db.*;
import ed.js.engine.Scope;
import ed.appserver.AppContext;
import ed.appserver.JSFileLibrary;
import ed.appserver.ModuleDirectory;
import ed.io.SysExec;

/** Documentation generator for JavaScript and Java
 * @expose
 */
public class Generate {

    /** Documentation version string... can be anything: "1.3.3", "dev", "BLARGH!", whatever
     */
    private static String version;

    /** Version setter
     */
    public static void setVersion(String v) {
        version = v;
    }

    /** Version getter
     */
    public static String getVersion() {
        if(version == null) return "";
        return version;
    }

    /**
     *  Gets things ready for a "db blob to HTML" generation run.  Ensure
     *  the directory exists, and ensure that it's empty
     */
    public static void setupHTMLGeneration(String path) throws Exception {

        Scope s = Scope.getThreadLocal();
        Object app = s.get("__instance__");

        if(! (app instanceof AppContext)) {
            throw new RuntimeException("your appserver isn't an appserver");
        }

        File check = new File(((AppContext)app).getRoot() + "/" + path + "DOC_DIR");
        File docdir = new File(((AppContext)app).getRoot()+"/" + path);

        if(!docdir.exists()) {
            System.err.println("Creating the directory "+docdir.getCanonicalPath()+" for documentation...");
            docdir.mkdirs();
        }

        if(!check.exists()) {
            check.createNewFile();
        }

        File blobs[] = docdir.listFiles();

        /*
         *   clean out all .out files in the doc directory
         */
        for(int i=0; i<blobs.length; i++) {
            if((blobs[i].getName()).endsWith(".out")) {
                blobs[i].delete();
            }
        }
    }

    /**
     *   Writes all .out files to the database
     * @param path
     */
    public static void postHTMLGeneration(String path) throws Exception {

        Scope s = Scope.getThreadLocal();
        Object app = s.get("__instance__");

        File docdir = new File(((AppContext)app).getRoot()+"/" + path);

        Object dbo = s.get("db");
        if(! (dbo instanceof DBBase)) {
            throw new RuntimeException("your database is not a database");
        }

        if(!docdir.exists()) {
            throw new RuntimeException("Error - doc dir was never setup : " + docdir);
        }

        DBBase db = (DBBase)dbo;
        DBCollection collection = db.getCollection("doc.html");

        File blobs[] = docdir.listFiles();
        for(int i=0; i<blobs.length; i++) {
            if(blobs[i].getName().endsWith(".out")) {

                FileInputStream fis = new FileInputStream(blobs[i]);
                StringBuffer sb = new StringBuffer();

                while(fis.available() > 0) {
                    sb.append((char)(fis.read()));
                }

                JSObjectBase obj = new JSObjectBase();
                obj.set("name", blobs[i].getName().substring(0, blobs[i].getName().indexOf(".out")));
                obj.set("content", sb.toString());

                System.out.println("Generate.postHTMLGeneration() : processing " + blobs[i].getName());

                collection.save(obj);
            }
        }
    }

    /** Takes objects from the db and makes them into HTML pages.
     * @param A jsoned obj from the db
     * @param The output dir
     */
    public static void toHTML(String objStr, String path) {

        System.out.print(".");

        Scope s = Scope.getThreadLocal();
        Object app = s.get("__instance__");

        if(! (app instanceof AppContext)) {
            throw new RuntimeException("your appserver isn't an appserver");
        }

        File docdir = new File(((AppContext)app).getRoot()+"/"+path);

        if(!docdir.exists()) {
            throw new RuntimeException("Error - doc dir was never setup : " + docdir);
        }

        JSObject foo = (JSObject) s.get("core");

        if (foo == null) {
        	throw new RuntimeException("Can't find 'core' in my scope");
        }

        ModuleDirectory md = (ModuleDirectory) foo.get("modules");

        if (md == null) {
        	throw new RuntimeException("Can't find 'modules' directory in my core object");
        }

        JSFileLibrary jsfl = md.getJSFileLibrary("docgen");

        if (jsfl == null) {
        	throw new RuntimeException("Can't find 'docgen' file lib in my module directory");
        }

        SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -d=" + docdir.getAbsolutePath().toString()
                + " -t=templates/jsdoc2", null, jsfl.getRoot(), objStr);

        String out = r.getOut();

        if(!out.trim().equals("")) {
            System.out.println("jsdoc says: "+out);
        }
    }

    private static ArrayList<String> javaSrcs = new ArrayList<String>();

    public static void srcToDb(String path) throws IOException {
        javaSrcs.clear();
        File f = new File(path);
        if(!f.exists()) {
            System.out.println("File does not exist: "+path);
            return;
        }
        if(f.isDirectory()) {
            jsToDb(f.getCanonicalPath());
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                srcToDb(farray[i].getCanonicalPath());
            }
        }
        else {
            processFile(f);
        }
        for(int i=0; i<javaSrcs.size(); i++) {
            javaToDb(javaSrcs.get(i));
        }
    }

    private static void processFile(File f) {

        System.out.println("Generate.processFile() : processing " + f);

        try {
            if((f.getName()).endsWith(".java"))
                javaSrcs.add(f.getCanonicalPath());
        }
        catch(IOException e) {
            System.out.println("error getting the name of file "+f);
            e.printStackTrace();
        }
    }


    /** Takes source files/dirs, generates jsdoc from them, stores resulting js obj in the db
     * @param Path to the file or folder to be documented
     */
    public static void jsToDb(String path) throws IOException {

        System.out.println("Generate.jsToDB() : processing " + path);

        File f = new File(path);

        SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -r -t=templates/json "+f.getCanonicalPath(), null, new File("../core-modules/docgen/"), "");

        Scope s = Scope.getThreadLocal();
        Object dbo = s.get("db");
        if(! (dbo instanceof DBBase)) {
            throw new RuntimeException("your database is not a database");
        }

        DBBase db = (DBBase)dbo;
        DBCollection collection = db.getCollection("doc");

        String rout = r.getOut();
        String jsdocUnits[] = rout.split("---=---");
        System.out.println("classes: "+jsdocUnits.length);
        for(int i=0; i<jsdocUnits.length; i++) {
            JSObject json = (JSObject)JS.eval("("+jsdocUnits[i]+")");
            if(json == null) {
                System.out.println("couldn't parse: "+jsdocUnits[i]);
                continue;
            }

            Iterator k = (json.keySet()).iterator();
            while(k.hasNext()) {
                String name = (k.next()).toString();
                JSObject unit = (JSObject)json.get(name);
                JSString isa = (JSString)unit.get("isa");
                System.out.println("name: "+name+" isa: "+isa);
                if(isa.equals("GLOBAL") || isa.equals("CONSTRUCTOR")) {
                    JSObjectBase ss = new JSObjectBase();
                    ss.set("symbolSet", json);
                    JSObjectBase obj = new JSObjectBase();
                    obj.set("ts", Calendar.getInstance().getTime().toString());
                    obj.set("_index", ss);
                    obj.set("version", Generate.getVersion());
                    obj.set("name", name);

                    if(!name.equals("_global_")) {
                        collection.save(obj);
                    }
                }
            }
        }
    }

    /** Generate a js obj from javadoc
     * @param path to file or folder to be documented
     */
    public static void javaToDb(String path) throws IOException {
        System.out.println("Generate.javaToDB() : processing " + path);
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet", "JavadocToDB", "-docletpath", "./", path } );
    }

    /** Remove old documentation files.
     * The first time docgen is run, existing files in the directory will be ignored.  After that, the entire directory will
     * be wiped each time docgen is run.
     * @param path to documentation directory
     */
    public static void removeOldDocs(String path) throws IOException {
        File check = new File(path+"/DOC_DIR");
        if(!check.exists()) return;

        File f = new File(path);
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                farray[i].delete();
            }
        }
    }
}
