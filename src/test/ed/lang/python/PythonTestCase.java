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

abstract public class PythonTestCase extends ed.TestCase {
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 5000;

    public void setUp(File dir) throws IOException, InterruptedException {
        if(!dir.mkdir() && !dir.exists())
            throw new IOException("Failed to create test dir " + dir);
    }
        
    protected static void rdelete(File f) {
        if(f.isDirectory()) {
            for(File sf : f.listFiles())
                rdelete(sf);
        }
        f.delete();
    }
    
    protected Scope initScope(final File dir, String name) {
        return initContext( dir, name ).getScope();
    }

    protected AppContext initContext(final File dir, String name){
        //Initialize Scope ==================================
        Scope oldScope = Scope.getThreadLocal();

        AppContext ac = new AppContext( name );
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
            RedirectedPrinter printer = new RedirectedPrinter();
            globalScope.put("print", printer);
            globalScope.put("counter", 0);

        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
        return ac;
    }

    protected void clearScope(Scope s){
        s.set("ranFile1", 0);
        s.set("ranFile2", 0);
        s.set("ranFile3", 0);
    }

    protected void shouldRun1(Scope s){
        clearScope(s);
        s.eval("local.file1();");
        assertRan1(s);
    }

    protected void assertRan1(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 0);
        assertEquals(s.get("ranFile3"), 0);
    }

    protected void shouldRun2(Scope s){
        clearScope(s);
        s.eval("local.file1();");
        assertRan2(s);
    }

    protected void assertRan2(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 1);
        assertEquals(s.get("ranFile3"), 0);
    }

    protected void shouldRun3(Scope s){
        clearScope(s);
        s.eval("local.file1();");
        assertRan3(s);
    }

    protected void assertRan3(Scope s){
        assertEquals(s.get("ranFile1"), 1);
        assertEquals(s.get("ranFile2"), 1);
        assertEquals(s.get("ranFile3"), 1);
    }

    protected void setTime(PrintWriter writer, String name){
        writer.println("_10gen."+name+" = _10gen.counter; _10gen.counter += 1");
    }

}
