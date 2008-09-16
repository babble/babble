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

package ed.db.mql;

import ed.js.engine.Scope;
import ed.js.JS;
import ed.js.JSON;
import ed.js.JSObject;
import ed.js.JSDate;
import ed.db.DBCursor;
import ed.db.DBBase;
import ed.db.DBProvider;
import ed.lang.StackTraceHolder;

import java.io.PrintStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.net.UnknownHostException;

import jline.ConsoleReader;
import jline.History;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

/**
 * Mongo db shell, in the style of "mysql" command line tool.
 * <p/>
 * You can use "MQL" (Mongo Query Language) for select, update, delete expressions, and
 * an 'experimental' insert syntax.
 * <p/>
 * insert [into] <collection> {obj} [[,] { obj}]
 */
public class MQLShell {

    private MyPrintStream _out;
    private DBBase _db;
    private Scope _scope;
    private boolean _exit = true;
    private String[] _mqlArgs = new String[0];
    private boolean _dump = false;
    
    MQLShell(PrintStream out, String[] args) throws Exception {

        Options opts = new Options();

        opts.addOption("h", "help", false, "show command line usage");
        opts.addOption("noexit", false, "remain at command prompt after running a script");
        opts.addOption("db", true, "db to connect to");
        opts.addOption("dump", false, "dump database");

        CommandLine cl = (new PosixParser()).parse(opts, args);

        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("mql [options] [script]", opts);
            System.exit(0);
        }

        setDump(cl.hasOption("dump"));

        _out = new MyPrintStream(out);

        // if dumping, try to intercept the spew from the libraries
        
        if (_dump) {
            _out._dropJunk = true;
            _out._comment = "// ";
            System.setOut(_out);
        }

        _scope = Scope.newGlobal().child(new File("."));
        _scope.setGlobal(true);
        _scope.makeThreadLocal();

        if (cl.hasOption("db")) {
            setDB(cl.getOptionValue("db"));
        }

        set_exit(!cl.hasOption("noexit"));

        // now, if we were given a list of files to deal with...

        _mqlArgs = cl.getArgs();
    }

    /**
     *  Main function that does the command line.  Will need to be broken up so
     *  we can handle scripts as well.
     *
     *  TODO - handle scripts
     * 
     * @throws Exception for whatever
     */
    public void go() throws Exception {

        if (_dump) {
            dumpDB();
            return;
        }
        
        if (_mqlArgs.length > 0) {
            processScripts();
            return;
        }

        String line;
        ConsoleReader console = new ConsoleReader();
        console.setHistory(new History(new File(".MQLshell")));

        while ((line = console.readLine(getDBName() + " > ")) != null) {

            if (!processLine(line)) {
                break;
            }
        }            
    }

    boolean processLine(String line) throws Exception {

        if (line == null) {
            return true;
        }
        
        line = line.trim();

        if (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1);
        }

        if (line.length() == 0) {
            return true;
        }

        if( line.startsWith("//")) {
            return true;
        }

        if (line.equals("help")) {
            showHelp();
            return true;
        } else if (line.equals("exit")) {
            return false;
        } else if (line.equals("show collections") || line.equals("show tables")) {
            line = "db.system.namespaces.find({});";
        } else if (line.startsWith("use")) {

            String[] ss = line.split(" ");

            if (ss.length != 2) {
                showHelp();
                return true;
            }

            setDB(ss[1]);
            return true;
        } else if (line.startsWith("select") || line.startsWith("update") || line.startsWith("delete")) {

            // stuff that uses the parser

            MQL parser = new MQL(line);
            try {
                SimpleNode ast = (SimpleNode) parser.parseQuery();
                QueryInfo qi = new QueryInfo("db");

                ast.generateQuery(qi);

                line = qi.toString();
            }
            catch (TokenMgrError e) {
                _out.println("syntax error : " + e.getMessage());
                return true;
            }
            catch (ParseException e) {
                _out.println("syntax error : " + e.getMessage());
                return true;
            }
        } else if (line.startsWith("insert")) {

            // experimental

            try {
                for (String l : handleInsert(line)) {
                    executeLine(l);
                }
            }
            catch (Exception e) {
                _out.println(e.getMessage());
            }
            return true;
        }

        executeLine(line);
        return true;
    }

    /**
     *  Takes a line of JS and executes it.  Will spew out _out whatever it needs to.
     * 
     * @param line  like of js to execute, like "db.foo.save(o)"
     */
    void executeLine(String line) {

        if (_db == null) {
            _out.println("Error - no current db connection.  Use 'use <db>'");
            return;
        }

        _out.println("[" + line + "]");

        try {
            boolean hasReturn[] = new boolean[1];
            
            Object res = _scope.eval(line, "lastline", hasReturn);

            if (hasReturn[0]) {
                if (res instanceof DBCursor) {
                    displayCursor(System.out, (DBCursor) res);
                } else {
                    _out.println(JSON.serialize(res));
                }
            }
        }
        catch (Exception e) {
            if (JS.RAW_EXCPETIONS) {
                e.printStackTrace();
            }
            StackTraceHolder.getInstance().fix(e);
            e.printStackTrace(_out);
            _out.println();
        }
    }

    /**
     * Experiment in mixing ql-like notation w/ object notation
     * 
     * @param line string with the insert statement
     * @return array of "object notation" strings for execution
     * @throws Exception if something is wrong...
     */
    List<String> handleInsert(String line) throws Exception {

        if (!line.contains("{")) {
            throw new Exception("syntax error : no objects specified");
        }

        String s = line;

        s = s.substring("insert".length());
        s = s.trim();

        if (s.startsWith("into")) {
            s = s.substring("into".length());
            s = s.trim();
        }

        int objStart = s.indexOf("{");

        if (objStart == 0) {
            throw new Exception("syntax error : no collection specified");
        }

        String coll = s.substring(0, objStart);
        coll = coll.trim();

        List<String> objs = new ArrayList<String>();

        while (s.length() != 0) {

            int loc = s.indexOf("{");

            if (loc == -1) {
                break;
            }

            s = s.substring(loc);

            boolean searching = false;
            int balance = 0;

            if (s.length() > 0) {
                searching = true;
                balance = 1;
                loc = 1;
            }

            while (searching) {

                char c = s.charAt(loc);

                if (c == '{') {
                    balance++;
                }

                if (c == '}') {
                    balance--;
                }

                if (balance == 0) {
                    String obj = s.substring(0, loc + 1);
                    objs.add(obj);
                    s = s.substring(loc);
                    break;
                }

                loc++;

                if (loc >= s.length()) {
                    throw new Exception("syntax error : unbalanced }");
                }

                searching = (s.length() > 0) && (loc < s.length());
            }
        }

        for (String ss : objs) {
            _out.println("obj : " + ss);
        }

        List<String> funcs = new ArrayList<String>();

        for (String o : objs) {
            funcs.add("db." + coll + ".save(" + o + ");");
        }

        return funcs;
    }

    /**
     * Dumps out help info to _out
     */
    void showHelp() {
        _out.println("MQL Help");
        _out.println("-------------------------------------------------------------------------------------------");
        _out.println("show collections : show all the collections in the current database");
        _out.println("use [database]   : switch to use the database 'database'");
        _out.println("select ...       : get information about a collection.  ex. select a,b from x where a = 2");
        _out.println("update ...       : update the elements of a collection. ex. update x set a = b where n = 4");
        _out.println("insert ...       : insert elements in a collection. ex. insert [into] x {obj} [[,] { obj}]");
        _out.println("exit             : exit the shell");
    }

    /**
     *  Stolen from ed.db.Shell, produces a nice string form of the passed in object
     * @param val JS* object to work with
     * @return nice string form
     */
    String _string(Object val) {

        if (val == null) {
            return "null";
        }

        if (val instanceof JSDate) {
            return ((JSDate) val).strftime("%D %T");
        }

        if (val instanceof JSObject) {
            String s = JSON.serialize(val, true, "");

            // seems like JSON adds a newline for some things
            if (s.endsWith("\n")) {
                s = s.substring(0, (s.length() - 1));
            }
            return s;
        }

        String s = val.toString();

        if (s.length() > 30) {
            return s.substring(0, 27) + "...";
        }
        
        return s;
    }


    /**
     *   pretty print for a cursor.  also stolen from ed.js.Shell w/ some tweaks.
     * @param out stream to print to
     * @param c cursor to print
     * @return size of set returned
     */
    int displayCursor(PrintStream out, DBCursor c) {

        List<JSObject> all = new ArrayList<JSObject>();
        Map<String, Integer> fields = new TreeMap<String, Integer>();

        for (int i = 0; i < 30 && c.hasNext(); i++) {
            JSObject obj = c.next();
            all.add(obj);

            for (String f : obj.keySet(false)) {

                if (JSON.IGNORE_NAMES.contains(f))
                    continue;

                Object blah = obj.get(f);

                Integer old = fields.get(f);
                if (old == null)
                    old = 4;

                fields.put(f, Math.max(_string(f).length(), Math.max(old, _string(blah).length())));
            }
        }

        if (all.size() == 0) {
            return 0;
        }

        for (String f : fields.keySet()) {
            out.printf("%" + fields.get(f) + "s | ", f);
        }

        out.printf("\n");

        for (JSObject obj : all) {
            for (String f : fields.keySet()) {
                out.printf("%" + fields.get(f) + "s | ", _string(obj.get(f)));
            }
            out.printf("\n");
        }

        return all.size();
    }

    public boolean is_exit() {
        return _exit;
    }

    public void set_exit(boolean e) {
        _exit = e;
    }

    public void setDump(boolean d) {
        _dump = d;
    }

    void setDB(String db) throws UnknownHostException {
        _db = DBProvider.get(db);
        _scope.put("db", _db, true);
    }

    String getDBName() {
        String s = "[no db]";

        if (_db != null) {
            s = _db.getConnectPoint();
        }

        return s;
    }

    /**
     *  reads the script names from the command line args and processes each one in turn
     *
     * @throws Exception when things go wrong
     */
    void processScripts() throws Exception {

        for (String script : _mqlArgs) {

            BufferedReader f = new BufferedReader(new FileReader(new File(script)));

            String s;
            while((s = f.readLine()) != null) {
                if (!processLine(s)) {
                    break;
                }
            }
        }
    }

    void dumpDB() throws Exception {

        // get the collections

        try {
            boolean hasReturn[] = new boolean[1];

            Object res = _scope.eval("db.system.namespaces.find({})", "lastline", hasReturn);

            if (hasReturn[0]) {
                if (res instanceof DBCursor) {

                    DBCursor c = (DBCursor) res;

                    while(c.hasNext()) {
                        JSObject o = c.next();
                        String s = o.get("name").toString();

                        int i = s.indexOf(".");
                        if (i == -1 || (i + 1) > s.length()) {
                            throw new Exception("whoops - error in namspaces");
                        }
                        s = s.substring(i+1);
                        dumpCollection(s);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(_out);
        }
    }

    void dumpCollection(String s) {
        _out.commentOut("dumping collection " + s);
        
        try {
            boolean hasReturn[] = new boolean[1];

            Object res = _scope.eval("db."+s +".find({})", "lastline", hasReturn);

            if (hasReturn[0]) {
                if (res instanceof DBCursor) {

                    DBCursor c = (DBCursor) res;

                    while(c.hasNext()) {
                        JSObject o = c.next();
                        String ss = JSON.serialize(o, true, "");
                        if (ss.endsWith("\n")) {
                            ss = ss.substring(0,ss.length()-1);
                        }
                        _out.mqlOut("insert into " + s + " " + ss);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(_out);
        }
    }

    /**
     *  Little adapter for a printstream so we can intercept the crap
     *  that comes out of the various libraries when writing a dump file
     */
    class MyPrintStream extends PrintStream {

        String _comment = "";
        boolean _dropJunk = false;

        MyPrintStream(PrintStream ps) {
            super(ps);
        }

        public void println(String s) {
            if (!_dropJunk) {
                commentOut(s);
            }
        }

        public void mqlOut(String s) {
            super.println(s);
        }
        public void commentOut(String s) {
            super.println(_comment + s);
        }

    }
    
    public static void main(String args[]) throws Exception {

        MQLShell shell = new MQLShell(System.out, args);
        shell.go();
    }
}