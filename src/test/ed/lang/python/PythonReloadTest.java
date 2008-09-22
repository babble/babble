package ed.lang.python;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.python.core.*;

import ed.js.Encoding;
import ed.appserver.JSFileLibrary;
import ed.appserver.AppContext;
import ed.appserver.AppRequest;
import ed.net.httpserver.HttpRequest;
import ed.appserver.templates.djang10.Printer.RedirectedPrinter;
import ed.js.JSLocalFile;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.log.Level;
import ed.log.Logger;

public class PythonReloadTest extends ed.TestCase {
    private static final String TEST_DIR = "/tmp/pyreload";
    private static final File testDir = new File(TEST_DIR);
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 5000;

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        if(!testDir.mkdir() && !testDir.exists())
            throw new IOException("Failed to create test dir");
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        Scope globalScope = initScope();
        RedirectedPrinter printer = new RedirectedPrinter();
        globalScope.set("print", printer);
        globalScope.set("counter", 0);
        JSFileLibrary fileLib = (JSFileLibrary)globalScope.get("local");
        
        
        writeTest1File1();
        writeTest1File2();
        writeTest1File3();

        Scope oldScope = Scope.getThreadLocal();
        globalScope.makeThreadLocal();
        
        try {
            globalScope.eval("local.file1();");
            assertRan3(globalScope);

            clearScope(globalScope);
            Thread.sleep(SLEEP_MS);
            writeTest1File2();

            PyObject m = Py.getSystemState().__findattr__("modules");

            globalScope.eval("local.file1();");

            assertRan2(globalScope);

            Thread.sleep(SLEEP_MS);
            clearScope(globalScope);
            writeTest2File2();
            globalScope.eval("local.file1();");

            assertRan2(globalScope);

            clearScope(globalScope);

            globalScope.eval("local.file1();");
            assertRan1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest2File2();
            clearScope(globalScope);
            globalScope.eval("local.file1();");
            assertRan2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File2();
            writeTest3File3();

            clearScope(globalScope);
            globalScope.eval("local.file1();");

            assertRan3(globalScope);

            clearScope(globalScope);
            globalScope.eval("local.file1();");
            assertRan1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File2();
            writeTest3File3();

            clearScope(globalScope);
            globalScope.eval("local.file1();");
            assertRan3(globalScope);


            clearScope(globalScope);
            globalScope.eval("local.file1();");
            assertRan1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File3();
            clearScope(globalScope);
            globalScope.eval("local.file1();");

            assertRan3(globalScope);
        }
        finally {
            if(oldScope != null)
                oldScope.makeThreadLocal();
            else
                Scope.clearThreadLocal();
            
            try {
                rdelete(testDir);
            } catch (Exception e) {
            }
        }
        
    }
    
    private static void rdelete(File f) {
        if(f.isDirectory()) {
            for(File sf : f.listFiles())
                rdelete(sf);
        }
        f.delete();
    }
    
    private Scope initScope() {
        //Initialize Scope ==================================
        Scope oldScope = Scope.getThreadLocal();

        AppContext ac = new AppContext( "python reload test" );
        Scope globalScope = ac.getScope();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();
        
        try {
            //Load native objects
            Logger log = Logger.getRoot();
            globalScope.set("log", log);
            log.makeThreadLocal();
            
            Map<String, JSFileLibrary> rootFiles = new HashMap<String, JSFileLibrary>();
            rootFiles.put("local", new JSFileLibrary(new File(TEST_DIR), "local", ac ));
            for(Map.Entry<String, JSFileLibrary> rootFileLib : rootFiles.entrySet())
                globalScope.set(rootFileLib.getKey(), rootFileLib.getValue());

            Encoding.install(globalScope);
            //JSHelper helper = JSHelper.install(globalScope, rootFiles, log);
    
            globalScope.set("SYSOUT", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    System.out.println(p0);
                    return null;
                }
            });
            globalScope.put("openFile", new JSFunctionCalls1() {
                public Object call(Scope s, Object name, Object extra[]) {
                    return new JSLocalFile(new File(TEST_DIR), name.toString());
                }
            }, true);
            
            //configure Djang10 =====================================
            //helper.addTemplateRoot(globalScope, new JSString("/local"));

        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
        return globalScope;
    }

    private void clearScope(Scope s){
        s.set("ranFile1", 0);
        s.set("ranFile2", 0);
        s.set("ranFile3", 0);
    }

    // file1 -> file2 -> file3
    private void writeTest1File1() throws IOException{
        fillFile(1, true);
    }

    private void writeTest1File2() throws IOException{
        fillFile(2, true);
    }

    private void writeTest1File3() throws IOException{
        fillFile(3, false);
    }

    private void setTime(PrintWriter writer, String name){
        writer.println("_10gen."+name+" = _10gen.counter; _10gen.counter += 1");
    }

    private void fillFile(int n, boolean importNext) throws IOException{
        File f = new File(testDir, "file"+n+".py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile"+n+" = 1");
        setTime(writer, "startFile1");
        if(importNext)
            writer.println("import file"+(n+1));
        setTime(writer, "endFile1");
        writer.close();
    }

    private void writeTest2File2() throws IOException{
        File f = new File(testDir, "file2.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile2 = 1");
        writer.println("import file2");
        writer.close();
    }

    private void writeTest3File2() throws IOException{
        fillFile(2, true);
    }

    private void writeTest3File3() throws IOException{
        File f = new File(testDir, "file3.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile3 = 1");
        writer.println("import file2");
        writer.close();
    }

    private void assertRan1(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 0);
        assertEquals(s.get("ranFile3"), 0);
    }

    private void assertRan2(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 1);
        assertEquals(s.get("ranFile3"), 0);
    }

    private void assertRan3(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 1);
        assertEquals(s.get("ranFile3"), 1);
    }

    public static void main(String [] args){
        (new PythonReloadTest()).runConsole();
    }

}
