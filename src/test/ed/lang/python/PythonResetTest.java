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

public class PythonResetTest extends PythonTestCase {
    private static final String TEST_DIR = "/tmp/pyreset";
    private static final File testDir = new File(TEST_DIR);
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 2000;

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        super.setUp(testDir);
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        AppContext ac = initContext(testDir, "python-reset-test");
        Scope globalScope = ac.getScope();
        JSFileLibrary fileLib = (JSFileLibrary)globalScope.get("local");
        
        writeTest1File1();
        writeTest1File2();
        writeTest1File3();

        Scope oldScope = Scope.getThreadLocal();
        globalScope.makeThreadLocal();
        
        try {
            shouldRun3(globalScope);
            shouldRun1(globalScope);

            // simulate reset without an AppContextHolder
            ac = initContext(testDir, "python-reset-test");
            globalScope = ac.getScope();
            globalScope.makeThreadLocal();
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

    public static void main(String [] args){
        (new PythonResetTest()).runConsole();
    }

}
