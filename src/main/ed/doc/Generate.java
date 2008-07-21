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

package ed.doc;

import java.io.*;
import java.util.*;

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

    private static boolean debug = false;

    /** Documentation version string... can be anything: "1.3.3", "dev", "BLARGH!", whatever
     */
    private static String version;
    private static ArrayList<String> processedFiles = new ArrayList<String>();

    private static boolean connected = false;
    private static DBBase db;
    private static DBCollection codedb;
    private static DBCollection docdb;

    public static boolean generateInProgress = false;

    public static void initialize() {
        javaSrcs.clear();
        processedFiles.clear();
    }

    public static void connectToDb() {
        if ( connected ) return;

        Scope s = Scope.getThreadLocal();
        Object app = s.get("__instance__");
        Object dbo = s.get("db");
        if(! (dbo instanceof DBBase)) {
            throw new RuntimeException("your database is not a database");
        }

        db = (DBBase)dbo;
        docdb = db.getCollection("doc");
        codedb = db.getCollection("doc.code");
        connected = true;
    }

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

    public static JSObjectBase _global;

    /**
     *  Gets things ready for a "db blob to HTML" generation run.  Ensure
     *  the directory exists, and ensure that it's empty
     */
    public static void setupHTMLGeneration(String path) throws Exception {
        generateInProgress = true;

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

        if(!docdir.exists()) {
            throw new RuntimeException("Error - doc dir was never setup : " + docdir);
        }

        File blobs[] = docdir.listFiles();
        for(int i=0; i<blobs.length; i++) {
            if(blobs[i].getName().endsWith(".out")) {

                FileInputStream fis = new FileInputStream(blobs[i]);
                StringBuffer sb = new StringBuffer();

                while(fis.available() > 0) {
                    sb.append((char)(fis.read()));
                }

                JSObjectBase q = new JSObjectBase();
                q.set("alias", blobs[i].getName().substring(0, blobs[i].getName().indexOf(".out")));
                q.set("version", Generate.getVersion());
                Iterator it = docdb.find(q);
                while(it != null && it.hasNext()) {
                    JSObject next = (JSObject)it.next();
                    next.set("content", sb.toString());
                    docdb.save(next);
                }

                if(debug)
                    System.out.println("Generate.postHTMLGeneration() : processing " + blobs[i].getName());

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

    public static void globalToDb() {
        JSObjectBase ss = new JSObjectBase();
        ss.set("symbolSet", _global);
        JSObjectBase newGlobal = new JSObjectBase();
        newGlobal.set("_index", ss);
        newGlobal.set("ts", Calendar.getInstance().getTime().toString());
        newGlobal.set("version", Generate.getVersion());
        newGlobal.set("name", "global");

        JSObjectBase oldGlobal = new JSObjectBase();
        oldGlobal.set("name", "global");
        oldGlobal.set("version", Generate.getVersion());

        docdb.remove(oldGlobal);
        docdb.save(newGlobal);
    }

    public static void globalToHTML(String path) {
        JSObjectBase ss = new JSObjectBase();
        ss.set("symbolSet", _global);
        toHTML(JSON.serialize(ss), path);
    }

    /** javaSrcs is a list of Java classes to be processed.  In order to be merged correctly when
     * there is a Java and JS class with the same name, java classes must be processed second.
     */
    private static ArrayList<String> javaSrcs = new ArrayList<String>();

    // Once javaSrcs has been filled in, process the java files
    public static void javaSrcsToDb() throws IOException {
        for(int i=0; i<javaSrcs.size(); i++) {
            javaToDb(javaSrcs.get(i));
        }
    }

    public static void srcToDb(String path) throws IOException {
        File f = new File(path);

        // check for invalid paths and hidden files
        if(!f.exists()) {
            System.out.println("File does not exist: "+path);
            return;
        }
        if(f.getName().charAt(0) == '.') {
            System.out.println("Ignoring hidden file "+path);
            return;
        }


        // if it hasn't been done already, process any .js files
        if(!processedFiles.contains(path)) {
            jsToDb(f.getCanonicalPath());
        }

        // if it's a directory, process all its files
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                srcToDb(farray[i].getCanonicalPath());
            }
        }
        // if it's a java file, add it to the list
        else if((f.getName()).endsWith(".java") && !javaSrcs.contains(f.getCanonicalPath())) {
            addToCodeCollection(f);
            javaSrcs.add(f.getCanonicalPath());
        }
    }

    /** Recursively adds a folder and all subfolders/files to the "done" list
     * @param Canonical pathname of a file/directory
     */
    private static void addToProcessedFiles(String path) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            System.out.println("File does not exist: "+path);
            return;
        }
        if(f.isDirectory()) {
            File farray[] = f.listFiles();
            for(int i=0; i<farray.length; i++) {
                addToProcessedFiles(farray[i].getCanonicalPath());
            }
        }
        if(!processedFiles.contains(f.getCanonicalPath())) {
            addToCodeCollection(f);
            processedFiles.add(path);
        }
    }

    public static void addToCodeCollection(File f) throws IOException {
        if(f.isDirectory() || (!f.getName().endsWith(".js") && !f.getName().endsWith(".java"))) return;

        StringBuffer buff = new StringBuffer("");
        Scanner sc = new Scanner(f);
        char ch;
        while(sc.hasNextLine()) {
            buff.append(sc.nextLine()+"\n");
        }
        sc.close();
        JSObjectBase obj = new JSObjectBase();
        obj.set("filename", f.getCanonicalPath());
        obj.set("name", f.getName());
        obj.set("version", Generate.getVersion());
        obj.set("ts", Calendar.getInstance().getTime().toString());
        obj.set("content", buff.toString());
        codedb.save(obj);
    }

    /** Takes source files/dirs, generates jsdoc from them, stores resulting js obj in the db
     * @param Path to the file or folder to be documented
     */
    public static void jsToDb(String path) throws IOException {

        if(debug)
            System.out.println("Generate.jsToDB() : processing " + path);

        File f = new File(path);
        addToProcessedFiles(path);

        Scope s = Scope.getThreadLocal();

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

        SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -r -t=templates/json "+f.getCanonicalPath(),
        		null, jsfl.getRoot(), "");

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
                boolean isNamespace = ((Boolean)unit.get("isNamespace")).booleanValue();
                if(isa.equals("GLOBAL") || isa.equals("CONSTRUCTOR") || isNamespace) {
                    JSObjectBase ss = new JSObjectBase();
                    ss.set("symbolSet", json);
                    JSObjectBase obj = new JSObjectBase();
                    obj.set("ts", Calendar.getInstance().getTime().toString());
                    obj.set("_index", ss);
                    obj.set("version", Generate.getVersion());
                    obj.set("name", name);

                    // if one exists, get the class description
                    String desc;
                    if(unit.get("classDesc") != null) {
                        desc = ((JSString)unit.get("classDesc")).toString();
                    }
                    else if(unit.get("desc") != null) {
                        desc = ((JSString)unit.get("desc")).toString();
                    }
                    else {
                        desc = "";
                    }
                    obj.set("desc", desc.substring(0,desc.indexOf(".")+1));

                    if(name.equals("_global_")) {
                        if(_global == null) {
                            _global = (JSObjectBase)json;
                        }
                        else {
                            addToGlobal("methods", (JSArray)unit.get("methods"));
                            addToGlobal("properties", (JSArray)unit.get("properties"));
                        }
                    }
                    else {
                        docdb.save(obj);
                    }
                }
            }
        }
    }

    public static void addToGlobal(String field, JSArray obj) {
        JSObjectBase gobj = (JSObjectBase)_global.get("_global_");
        JSArray garray = (JSArray)gobj.get(field);
        if(garray == null) garray = new JSArray();

        for ( String key : obj.keySet() ) {
            garray.add( obj.get( key ) );
        }
        gobj.set(field, garray);
        _global.set("_global_", gobj);
    }


    /** Generate a js obj from javadoc
     * @param path to file or folder to be documented
     */
    public static void javaToDb(String path) throws IOException {
        if(debug)
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
