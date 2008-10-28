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

public class PythonFileTest extends PythonTestCase {
    private static final String TEST_DIR = "/tmp/pyfile";
    private static final File testDir = new File(TEST_DIR);
    private static final String FILE_TEXT = "Hello from open()!";

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        super.setUp(testDir);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        AppContext ac = initContext(testDir, "python-file-test");
        Scope globalScope = ac.getScope();
        JSFileLibrary fileLib = (JSFileLibrary)globalScope.get("local");

        writeTest1File1();
        writeTest1File2();

        Scope oldScope = Scope.getThreadLocal();
        globalScope.makeThreadLocal();

        try {
            globalScope.eval("local.file1();");
            assertEquals(globalScope.get("text"), FILE_TEXT + "\n");
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
        File f = new File(testDir, "file1.py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        writer.println("_10gen.text = open('file2.txt').read()");
        writer.close();
    }

    private void writeTest1File2() throws IOException{
        File f = new File(testDir, "file2.txt");
        PrintWriter writer = new PrintWriter(f);
        writer.println(FILE_TEXT);
        writer.close();
    }

    public static void main(String [] args){
        (new PythonReloadTest()).runConsole();
    }

}
