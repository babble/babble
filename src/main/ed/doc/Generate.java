package ed.doc;

import java.io.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

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

    private static String version;

    public static void setVersion(String v) {
        version = v;
    }

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
            Scope s = Scope.getThreadLocal();
            Object dbo = s.get("__instance__");
            if(! (dbo instanceof AppContext)) throw new RuntimeException("your appserver is having an identity crisis");
            String instanceName = ((AppContext)dbo).getName();

            SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -d=../../"+instanceName+"/"+path+" -t=templates/jsdoc2", null, new File("../core-modules/docgen/"), objStr);
            String out = r.getOut();
            if(!out.trim().equals("")) {
                System.out.println("jsdoc says: "+out);
            }

            File check = new File("../"+instanceName+"/"+path+"DOC_DIR");
            if(!check.exists())
                check.createNewFile();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }


    /** Takes source files/dirs, generates jsdoc from them, stores resulting js obj in the db
     * @param Path to the file or folder to be documented
     */
    public static void JSToDb(String path) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            System.out.println("File does not exist: "+path);
            return;
        }
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                JSToDb(farray[i].getCanonicalPath());
            }
        }
        else {
            try {
                SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -r -t=templates/json ../"+path, null, new File("../core-modules/docgen/"), "");

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

                            collection.save(obj);
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Generate a js obj from javadoc
     * @param Path to file or folder to be documented
     */
    public static void JavadocArgHelper(String arg) throws IOException {
        File f = new File(arg);
        if(!f.exists()) return;
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                JavadocArgHelper(farray[i].getCanonicalPath());
            }
        }
        else {
            com.sun.tools.javadoc.Main.execute(new String[]{"-doclet", "JavadocToDB", "-docletpath", "./", arg } );
        }
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

        f = new File(path+"/symbols");
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                farray[i].delete();
            }
        }
    }
}
