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
import org.python.util.*;

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
import ed.lang.python.Python;

public class PythonReloadTest extends PythonTestCase {
    private static final String TEST_DIR = "/tmp/pyreload";
    private static final String TEST_DIR_SUB = "/tmp/pyreload/mymodule";
    private static final File testDir = new File(TEST_DIR);
    private static final File testDirSub = new File(TEST_DIR_SUB);
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 2000;

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        super.setUp(testDir);
        super.setUp(testDirSub);
        // Mark as a module
        new File(testDirSub, "__init__.py").createNewFile();
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        Scope globalScope = initScope(testDir, "python-reload-test");
        JSFileLibrary fileLib = (JSFileLibrary)globalScope.get("local");
        
        writeTest1File1();
        writeTest1File2();
        writeTest1File3();

        Scope oldScope = Scope.getThreadLocal();
        globalScope.makeThreadLocal();
        
        try {
            globalScope.eval("local.file1();");
            assertRan3(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest1File2();

            PyObject m = Py.getSystemState().__findattr__("modules");

            shouldRun2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest2File2();
            shouldRun2(globalScope);

            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest2File2();
            shouldRun2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File2();
            writeTest3File3();

            shouldRun3(globalScope);

            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File2();
            writeTest3File3();

            shouldRun3(globalScope);

            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest3File3();
            shouldRun3(globalScope);

            Thread.sleep(SLEEP_MS);
            // Test 4
            writeTest4File1();
            writeTest4File2();
            writeTest4File3();

            // 1 exec() 2 import 3
            // 1 runs, execs 2 (runs unconditionally), imports 3 (runs once)
            shouldRun3(globalScope);

            shouldRun2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest4File1();
            shouldRun2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest4File2();
            shouldRun2(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest4File3();
            shouldRun3(globalScope);

            // Test 5 -- __import__(file, {}) is tracked
            Thread.sleep(SLEEP_MS);
            writeTest5File1();
            writeTest5File2();
            writeTest5File3();
            shouldRun3(globalScope);

            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest5File2();
            shouldRun2(globalScope);
            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest5File3();
            shouldRun3(globalScope);
            shouldRun1(globalScope);

            // Test 6 -- importing in modules is tracked
            globalScope = initScope(testDir, "python-reload-test6"); // flush sys.modules
            globalScope.makeThreadLocal();
            writeTest6File1();
            writeTest6File2();
            writeTest6File3();
            shouldRun3(globalScope);
            // make sure right module was getting run
            assertEquals(globalScope.get("ranSubFile3"), 100);
            globalScope.set("ranSubFile3", 0);

            shouldRun1(globalScope);
            assertEquals(globalScope.get("ranSubFile3"), 0);

            Thread.sleep(SLEEP_MS);
            writeTest6File2();
            shouldRun2(globalScope);
            shouldRun1(globalScope);

            Thread.sleep(SLEEP_MS);
            writeTest6File3();
            shouldRun3(globalScope);
            shouldRun1(globalScope);

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
        fillFile(testDir, n, importNext);
    }

    private void fillFile(File dir, int n, boolean importNext) throws IOException{
        File f = new File(dir, "file"+n+".py");
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

    private void writeTest4File1() throws IOException {
        File f = new File(testDir, "file1.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile1 = 1");
        writer.println("execfile('"+testDir+"/file2.py', {})");
        writer.close();
    }

    private void writeTest4File2() throws IOException {
        File f = new File(testDir, "file2.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile2 = 1");
        writer.println("import file3");
        writer.close();
    }

    private void writeTest4File3() throws IOException {
        fillFile(3, false);
    }

    private void writeTest5File1() throws IOException {
        fillFile(1, true);
    }

    private void writeTest5File2() throws IOException {
        File f = new File(testDir, "file2.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile2 = 1");
        writer.println("__import__('file3', {})");
        writer.close();
    }

    private void writeTest5File3() throws IOException {
        fillFile(3, false);
    }

    private void writeTest6File1() throws IOException {
        fillFile(1, true);
    }

    private void writeTest6File2() throws IOException {
        File f = new File(testDir, "file2.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile2 = 1");
        writer.println("import mymodule.file3");
        writer.close();
    }

    private void writeTest6File3() throws IOException {
        File f = new File(testDirSub, "file3.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.ranFile3 = 1");
        writer.println("_10gen.ranSubFile3 = 100");
        writer.close();
    }

    public static void main(String [] args){
        (new PythonReloadTest()).runConsole();
    }

}
