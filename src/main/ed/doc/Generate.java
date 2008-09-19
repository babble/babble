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

    static final boolean D = Boolean.getBoolean( "DEBUG.JSFL" );

    private static ArrayList<String> processedFiles = new ArrayList<String>();

    private static boolean connected = false;
    private static DBBase db;
    private static DBCollection codedb;
    private static DBCollection docdb;
    private static DBCollection srcdb;

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
        srcdb = db.getCollection("doc.src");
        connected = true;
    }

    public static JSObject mergeClasses(JSObject master, JSObject child) {
        // merge constructors
        JSArray javadocCons = (JSArray)master.get("constructors");
        if(javadocCons == null) javadocCons = new JSArray();
        JSArray jsCons = (JSArray)child.get("constructors");
        if(jsCons != null) {
            Iterator p = jsCons.iterator();
            while(p.hasNext())
                javadocCons.add(p.next());
        }

        // merge methods
        JSArray javadocMethod = (JSArray)master.get("methods");
        Iterator p = ((JSArray)child.get("methods")).iterator();
        while(p.hasNext())
            javadocMethod.add(p.next());

        // merge props
        JSArray javadocProp = (JSArray)master.get("properties");
        p = ((JSArray)child.get("properties")).iterator();
        while(p.hasNext())
            javadocProp.add(p.next());

        // add src file, if it exists
        if(child.get("srcFile") != null) {
            master.set("srcFile", (child.get("srcFile")).toString());
        }
        return master;
    }

    public static void addToModule( JSObject jsobj, String modname ) {
        JSObject query = new JSObjectBase();
        query.set( "name", modname );
        Iterator it = docdb.find( query );

        // set the name on the jsobj
        jsobj.set( "name" , modname );

        // create a new module
        if(it == null) {
            JSObject topLevel = new JSObjectBase();
            topLevel.set( "symbolSet" , jsobj );
            topLevel.set( "ts" , Calendar.getInstance().getTime().toString() );
            topLevel.set( "name" , modname );

            String classDesc = jsobj.get("classDesc").toString();
            int summarylen = classDesc.indexOf(". ")+1;
            if(summarylen == 0) 
                summarylen = classDesc.indexOf(".\n")+1;
            if(summarylen == 0) 
                summarylen = classDesc.length();
            topLevel.set("desc", classDesc.substring(0, summarylen));

            docdb.save( topLevel );
        }
        //add to an existing
        else {
            JSObject existing = (JSObject)it.next();
            mergeClasses( (JSObject)existing.get( "symbolSet" ) , jsobj );
            docdb.save( existing );
        }
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
        obj.set("ts", Calendar.getInstance().getTime().toString());
        obj.set("content", buff.toString());
        codedb.save(obj);
    }

    /** Takes source files/dirs, generates jsdoc from them, stores resulting js obj in the db
     * @param Path to the file or folder to be documented
     */
    public static void jsToDb(String path) throws IOException {

        if( D )
            System.out.println("Generate.jsToDB() : processing " + path);

        File f = new File(path);
        addToProcessedFiles(path);

        Scope s = Scope.getThreadLocal();

        JSObject foo = (JSObject) s.get( "core" );
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

        String classlist = "";
        if( classes.size() > 0 ) {
            for( String cls : classes ) {
                classlist += cls + ",";
            }
            classlist = " -b=" + classlist.substring( 0, classlist.length() - 1 );
        }

        SysExec.Result r = SysExec.exec("java -jar jsrun.jar app/run.js -r"+classlist+" -t=templates/json "+f.getCanonicalPath(),
        		null, jsfl.getRoot(), "");

        String rout = r.getOut();
        String jsdocUnits[] = rout.split("---=---");
        for(int i=0; i<jsdocUnits.length; i++) {
            // as far as I know, jsdocUnits[i] has never been null, but it has been "", which eval doesn't like
            if(jsdocUnits[i] == null || jsdocUnits[i].trim().equals("")) continue;

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
                if( isa.equals("CONSTRUCTOR") || ( isNamespace && !isa.equals( "GLOBAL" ) ) ) {
                    JSArray modules = (JSArray)unit.get( "docmodule" );
                    if( modules != null && modules.size() > 0 ) {
                        for( Object m : modules ) {
                            addToModule( unit, m.toString() );
                        }
                    }
                    else {
                        addToModule( unit, name );
                    }
                }
            }
        }
    }

    private static String getClassDesc( JSObject unit ) {
        if(unit.get("classDesc") != null) {
            return ((JSString)unit.get("classDesc")).toString();
        }
        else if(unit.get("desc") != null) {
            return ((JSString)unit.get("desc")).toString();
        }
        else {
            return "";
        }
    }

    /** Generate a js obj from javadoc
     * @param path to file or folder to be documented
     */
    public static void javaToDb(String path) throws IOException {
        if( D )
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

    public static ArrayList<String> classes = new ArrayList<String>(); 

    public static void getClassesList( String filename ) {
        if( filename == null ) 
            return;

        try {
            Scanner sc = new Scanner( new File( filename ) );
            while( sc.hasNext() ) {
                classes.add( sc.next() );
            }
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }


    private static ArrayList<String> getSourcePath( String[] args ) {
        ArrayList<String> paths = new ArrayList<String>();
        String path = args.length > 0 && args.length % 2 == 1 ? args[ args.length - 1 ] : ".";

        try {
            File file = new File( path );

            if( file.isDirectory() ) {
                paths.add( file.getCanonicalPath() );
            }
            else {
                Scanner sc = new Scanner( file );
                while( sc.hasNext() ) {
                    paths.add( sc.next() );
                }
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        return paths;
    }

    public static Hashtable<String,Object> processArgs( String[] args ) {
        Hashtable<String,Object> argTable = new Hashtable<String,Object>();
        for( int i=0; i<args.length-1; i+=2 ) {
            if( args[i].equals("--classlist") || args[i].equals("-c") ) {
                getClassesList( args[ i+1 ] );
            }
            if( args[i].equals("--db" ) ) {
                argTable.put( "db" , args[ i+1 ] );
            }
            if( args[i].equals("--db_ip" ) ) {
                argTable.put( "db_ip" , args[ i+1 ] );
            }
        }
        argTable.put( "path" , getSourcePath( args ) );
        return argTable;
    }

    public static void main( String[] args ) throws IOException, Exception {
        Hashtable<String,Object> argTable = processArgs( args );
        getClassesList( (String)argTable.get( "classlist" ) );

        initialize();

        // connect to db
        if( argTable.containsKey( "db" ) ) {
            String url = (String)argTable.get( "db" );
            if ( argTable.containsKey( "db_ip" ) ) 
                url = argTable.get( "db_ip" ) + "/" + url;
        
            try {
                db = DBProvider.get( url );
            }
            catch ( java.net.UnknownHostException un ){
                throw new RuntimeException( "bad db url [" + url + "]" );
            }
        }
        else {
            printUsage();
            return;
        }

        // create scope
        Scope s = Scope.newGlobal().child( new File("." ) );
        s.setGlobal( true );
        s.makeThreadLocal();
        s.put( "__instance__" , new AppContext( (new File( "." )).getCanonicalPath() ) , true );
        s.put( "core" , CoreJS.get().getLibrary( null , null , s , false ) , true );
        s.put( "db" , db , true );

        // set up collections
        docdb = db.getCollection("doc");
        codedb = db.getCollection("doc.code");
        srcdb = db.getCollection("doc.src");
        connected = true;

        docdb.remove(  new JSObjectBase() );
        codedb.remove( new JSObjectBase() );

        if( D ) 
            System.out.println( "adding doc for file(s in): " + argTable.get( "path" ) );
        // srcs to db
        ArrayList filelist = (ArrayList)argTable.get( "path" );
        for( Object name : filelist ) {
            srcToDb( (String)name );
        }
        javaSrcsToDb();

        JSObjectBase nameIdx = new JSObjectBase();
        nameIdx.set( "name" , 1 );
        docdb.ensureIndex(nameIdx);
    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\tjava Generate --db dbname [--db_ip dbip] [--classlist|-c path/to/classlist/file] [path/to/doc/dir]");
    }
}
