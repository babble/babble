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

public class PythonCollideTest extends ed.TestCase {
    private static final String TEST_DIR_1 = "/tmp/pycontext1";
    private static final String TEST_DIR_2 = "/tmp/pycontext2";
    private static final File testDir1 = new File(TEST_DIR_1);
    private static final File testDir2 = new File(TEST_DIR_2);
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 5000;

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        if(!testDir1.mkdir() && !testDir1.exists())
            throw new IOException("Failed to create test dir");
        if(!testDir2.mkdir() && !testDir2.exists())
            throw new IOException("Failed to create test dir");
    }
        
    @Test
    public void test() throws IOException, InterruptedException {
        final Scope site1Scope = initScope(testDir1);
        final Scope site2Scope = initScope(testDir2);
        site1Scope.set( "counter", 0 );
        site2Scope.set( "counter", 1000 );
        writeTest1Files(true);

        Scope oldScope = Scope.getThreadLocal();
        
        try {
            Object module2_start = site2Scope.eval("moduleVal");
            assertEquals( module2_start, null );

            Thread t1 = new Thread() {
                    public void run(){
                        site1Scope.makeThreadLocal();
                        site1Scope.eval("local.file1();");
                    }
                };
            
            Thread t2 = new Thread() {
                    public void run(){
                        site2Scope.makeThreadLocal();
                        site2Scope.eval("local.file1();");
                    }
                };
            t1.start();
            t2.start();

            t1.join();
            t2.join();
            Object module1 = site1Scope.eval("moduleVal");
            assertEquals( module1, 1 );

            Object module2 = site2Scope.eval("moduleVal");
            assertEquals( module2, 2 );

            site1Scope.makeThreadLocal();
            site1Scope.set( "moduleVal", -1 );
            site2Scope.makeThreadLocal();
            site2Scope.set( "moduleVal", -1 );
            
            // Shouldn't need to reload -- same context, files not newer
            site1Scope.makeThreadLocal();
            site1Scope.eval("local.file1();");
            module1 = site1Scope.eval("moduleVal");
            assertEquals( module1, -1 );

            site2Scope.eval("local.file1();");
            module2 = site2Scope.eval("moduleVal");
            assertEquals( module2, -1 );

            Thread.sleep(SLEEP_MS);
            refreshTest1Once();

            site1Scope.makeThreadLocal();
            site1Scope.eval("local.file1();");
            module1 = site1Scope.eval("moduleVal");
            assertEquals( module1, 1 );

            site2Scope.makeThreadLocal();
            module2_start = site2Scope.eval("moduleVal");
            assertEquals( module2_start, -1 );

            site2Scope.eval("local.file1();");
            module2 = site2Scope.eval("moduleVal");
            assertEquals( module2, -1 );


            Thread.sleep(SLEEP_MS);
            refreshTest1Files(); // should prompt reload of everything


            site1Scope.makeThreadLocal();
            site1Scope.set( "moduleVal", -1 );
            site1Scope.eval("local.file1();");
            module1 = site1Scope.eval("moduleVal");
            assertEquals( module1, 1 );

            site2Scope.makeThreadLocal();
            module2_start = site2Scope.eval("moduleVal");
            assertEquals( module2_start, -1 );

            site2Scope.eval("local.file1();");
            module2 = site2Scope.eval("moduleVal");
            assertEquals( module2, 2 );


        }
        finally {
            if(oldScope != null)
                oldScope.makeThreadLocal();
            else
                Scope.clearThreadLocal();
            
            try {
                rdelete(testDir1);
                rdelete(testDir2);
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
    
    private Scope initScope(final File dir) {
        //Initialize Scope ==================================
        Scope oldScope = Scope.getThreadLocal();

        AppContext ac = new AppContext( "python-collision-test-"+dir );
        Scope globalScope = ac.getScope();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();
        
        try {
            //Load native objects
            Logger log = Logger.getRoot();
            globalScope.set("log", log);
            log.makeThreadLocal();
            
            Map<String, JSFileLibrary> rootFiles = new HashMap<String, JSFileLibrary>();
            rootFiles.put("local", new JSFileLibrary(dir, "local", ac ));
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
                    return new JSLocalFile(dir, name.toString());
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

    // file1 -> file2 -> file3
    private void writeTest1Files(boolean sleep) throws IOException{
        fillFile(testDir1, 1, 0, sleep, true);
        fillFile(testDir1, 2, 1, false, true);
        fillFile(testDir1, 3, 0, false, false);
        fillFile(testDir2, 1, 0, sleep, true);
        fillFile(testDir2, 2, 2, false, true);
        fillFile(testDir2, 3, 0, false, false);
    }

    private void refreshTest1Once() throws IOException{
        fillFile(testDir1, 3, 0, false, false);
    }

    private void refreshTest1Files() throws IOException{
        fillFile(testDir1, 3, 0, false, false);
        fillFile(testDir2, 3, 0, false, false);
    }

    private void setTime(PrintWriter writer, String name){
        writer.println("_10gen."+name+" = _10gen.counter; _10gen.counter += 1");
    }

    private void fillFile(File dir, int n, int val, boolean sleep, boolean importNext) throws IOException{
        File f = new File(dir, "file"+n+".py");
        PrintWriter writer = new PrintWriter(f);
        writer.println("import _10gen");
        setTime(writer, "startFile"+n);
        if(importNext){
            if(sleep){
                //                writer.println("print 'feeling sleepy'");
                writer.println("import time");
                writer.println("time.sleep(2)");
                writer.println("import sys");
                //writer.println("print 'ok that was fun', id(sys.modules), __import__");
            }
            writer.println("import file"+(n+1));
        }
        if(val != 0){
            writer.println("_10gen.moduleVal = "+val);
        }
        setTime(writer, "endFile"+n);
        writer.close();
    }

    public static void main(String [] args){
        (new PythonReloadTest()).runConsole();
    }

}
