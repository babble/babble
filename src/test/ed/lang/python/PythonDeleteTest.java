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

public class PythonDeleteTest extends PythonTestCase {
    private static final String TEST_DIR = "/tmp/pydelete";
    private static final File testDir = new File(TEST_DIR);
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 5000;


    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        super.setUp(testDir);
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        Scope globalScope = initScope(testDir, "python-delete-test");
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
            Python.deleteCachedJythonFiles(testDir);
            assert(! foundClassFile(testDir));

            Thread.sleep(SLEEP_MS);
            writeTest2File1();
            writeTest2File2();
            globalScope.eval("local.file1();");
            Python.deleteCachedJythonFiles(testDir);
            assert(! foundClassFile(testDir));
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

    private void writeTest2File1() throws IOException{
        File f = new File(testDir, "file1.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile1 = 1");
        writer.println("import lib.file2");
        writer.close();
    }

    private void writeTest2File2() throws IOException{
        File lib = new File(testDir, "lib");
        if(! lib.exists() ) lib.mkdir();

        File init = new File(lib, "__init__.py");
        PrintWriter writer_i = new PrintWriter(init);
        writer_i.println("# nothing to see here...");
        writer_i.close();

        File f = new File(lib, "file2.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile2 = 1");
        writer.println("import file2");
        writer.close();
    }

    public boolean foundClassFile(File dir){
        for(File child : dir.listFiles()){
            if(child.getName().endsWith(".class")) return true;
            if(child.isDirectory() && foundClassFile(child))
                return true;
        }
        return false;
    }

    public static void main(String [] args){
        (new PythonReloadTest()).runConsole();
    }

}
