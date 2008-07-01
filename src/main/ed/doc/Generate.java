package ed.doc;

import java.io.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.db.*;
import static ed.js.JSInternalFunctions.*;
import ed.js.engine.Scope;
import ed.appserver.AppContext;
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


    /** Takes objects from the db and makes them into HTML pages.  Uses a default output directory.
     * @param A jsoned obj from the db
     */
    public static void toHTML(String objStr) {
        toHTML(objStr, "../../www/html/doc/");
    }

    /** Takes objects from the db and makes them into HTML pages.
     * @param A jsoned obj from the db
     * @param The output dir
     */
    public static void toHTML(String objStr, String path) {
        try {
            System.out.print(".");
            Scope s = Scope.getThreadLocal();
            Object app = s.get("__instance__");
            if(! (app instanceof AppContext)) throw new RuntimeException("your appserver isn't an appserver");
            File check = new File(((AppContext)app).getRoot()+"/"+path+"DOC_DIR");
            File docdir = new File(((AppContext)app).getRoot()+"/"+path);
            if(!docdir.exists()) {
                System.err.println("Creating the directory "+docdir.getCanonicalPath()+" for documentation...");
                docdir.mkdirs();
            }
            if(!check.exists())
                check.createNewFile();

            SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -d=../"+((AppContext)app).getRoot()+"/"+path+" -t=templates/jsdoc2", null, new File("../core-modules/docgen/"), objStr);
            String out = r.getOut();
            if(!out.trim().equals("")) {
                System.out.println("jsdoc says: "+out);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
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
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                processFile(farray[i]);
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
        try {
            if((f.getName()).endsWith(".js"))
                jsToDb(f.getCanonicalPath());
            else if((f.getName()).endsWith(".java"))
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
        System.out.println(path);
        File f = new File(path);
        try {
            SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -r -t=templates/json "+f.getCanonicalPath(), null, new File("../core-modules/docgen/"), "");

            Scope s = Scope.getThreadLocal();
            Object dbo = s.get("db");
            if(! (dbo instanceof DBApiLayer)) throw new RuntimeException("your database is not a database");

            DBApiLayer db = (DBApiLayer)dbo;
            DBCollection collection = db.getCollection("doc");

            String rout = r.getOut();
            String jsdocUnits[] = rout.split("---=---");
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
                    if(isa.equals("GLOBAL") || isa.equals("CONSTRUCTOR")) {
                        JSObjectBase ss = new JSObjectBase();
                        ss.set("symbolSet", json);
                        JSObjectBase obj = new JSObjectBase();
                        obj.set("ts", Calendar.getInstance().getTime().toString());
                        obj.set("_index", ss);
                        obj.set("version", Generate.getVersion());
                        obj.set("name", name);

                        if(!name.equals("_global_"))
                            collection.save(obj);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** Generate a js obj from javadoc
     * @param path to file or folder to be documented
     */
    public static void javaToDb(String path) throws IOException {
        System.out.println(path);
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
