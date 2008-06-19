package ed.doc;

import java.io.*;
import java.util.Calendar;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.db.*;
import static ed.js.JSInternalFunctions.*;

/** Documentation generator for JavaScript and Java
 * @expose
 */
public class Generate {

    /** Takes objects from the db and makes them into HTML pages
     * @expose
     */
    public static void toHTML(String objStr) {
        toHTML(objStr, "../../www/html/doc/");
    }

    public static void toHTML(String objStr, String path) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"java", "-jar", "jsrun.jar", "app/run.js", "-d="+path, "-t=templates/jsdoc2"},
                                                  null,
                                                  new File("../core-modules/docgen/")
                                                  );
            BufferedWriter in = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            in.write(objStr);
            in.close();

            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line2;

            while ((line2 = out.readLine()) != null) {
                System.out.println(line2);
            }

            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;

            while ((line = err.readLine()) != null) {
                System.out.println(line);
            }

            p.waitFor();
            System.out.println("exit: "+p.exitValue());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }


    public static void JSToDb(String path) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"java", "-jar", "jsrun.jar", "app/run.js", "-r", "-d=../../www/html/doc", "-o=../../www/html/doc/log", "-t=templates/json", "../../corejs/"},
                                                  null,
                                                  new File("../core-modules/docgen/")
                                                  );
            BufferedWriter in = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            in.close();

            BufferedReader out = new BufferedReader(new InputStreamReader( p.getInputStream()));
            String line2 = "";
            StringBuffer jsdoc = new StringBuffer();
            while ((line2 = out.readLine()) != null){
                jsdoc.append(line2);
            }

            BufferedReader err = new BufferedReader(new InputStreamReader( p.getErrorStream()));
            String line;
            while ((line = err.readLine()) != null) {
                System.out.println(line);
            }

            p.waitFor();
            System.out.println("exit: "+p.exitValue());

            JSObjectBase ss = new JSObjectBase();
            ss.set("symbolSet", jsdoc.toString());
            JSObjectBase obj = new JSObjectBase();
            obj.set("ts", Calendar.getInstance().getTime().toString());
            obj.set("_index", ss);

            DBApiLayer db = DBProvider.get("admin", "127.0.0.1", 27017);
            DBCollection collection = db.getCollection("doc");
            collection.save(obj);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void JavadocToDb(String path) {
        path = "../"+path;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"javadoc",
                                                               "-doclet",
                                                               "JavadocToDB",
                                                               "-docletpath",
                                                               "./", path},
                                                  null,
                                                  new File(".")
                                                  );

            BufferedReader out = new BufferedReader(new InputStreamReader( p.getInputStream()));
            String line2 = "";
            StringBuffer jsdoc = new StringBuffer();
            while ((line2 = out.readLine()) != null){
                System.out.println(line2);
            }

            BufferedReader err = new BufferedReader(new InputStreamReader( p.getErrorStream()));
            String line;
            while ((line = err.readLine()) != null) {
                System.out.println(line);
            }

            p.waitFor();
            System.out.println("exit: "+p.exitValue());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void JavadocArgHelper(String arg) {
        arg = "../"+arg;
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet", "JavadocToDB", "-docletpath", "./", arg } );
    }
}
