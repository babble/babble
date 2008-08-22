package ed.appserver.templates.djang10;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.testng.annotations.Test;

import ed.js.Encoding;
import ed.appserver.JSFileLibrary;
import ed.appserver.templates.djang10.Printer.RedirectedPrinter;
import ed.js.JSLocalFile;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.log.Level;
import ed.log.Logger;

public class Djang10TemplateReloadTest {
    private static final String TEST_DIR = "/tmp/djang10";
    
    //time to wait between file modifications to allow the fs to update the timestamps
    private static final long SLEEP_MS = 5000;
        
    @Test
    public void test() throws IOException, InterruptedException {
        File testDir = new File(TEST_DIR);
        if(!testDir.mkdir() && !testDir.exists())
            throw new IOException("Failed to create test dir");
        
        Scope globalScope = initScope();
        RedirectedPrinter printer = new RedirectedPrinter();
        globalScope.set("print", printer);
        JSFileLibrary fileLib = (JSFileLibrary)globalScope.get("local");
        
        File parentFile = new File(testDir, "parent.djang10");
        File child1File = new File(testDir, "child1.djang10");
        File child2File = new File(testDir, "child2.djang10");
        
        
        PrintWriter writer = new PrintWriter(parentFile);
        writer.println("[parent] {% block block1 %}{% endblock %} {% block block2 %}{% include '/local/child2' %}{% endblock %}");
        writer.close();
        
        writer = new PrintWriter(child1File);
        writer.println("{% extends \"/local/parent.djang10\" %} {% block block1 %}[child1]{% endblock %}");
        writer.close();
        
        writer = new PrintWriter(child2File);
        writer.println("[child2]");
        writer.close();
        
        
        Scope oldScope = Scope.getThreadLocal();
        globalScope.makeThreadLocal();
        
        Thread.sleep(SLEEP_MS);
        
        try {
            //make sure that the template renders fine to begin with
            Djang10CompiledScript child1 = (Djang10CompiledScript)fileLib.get(child1File.getName());
            child1.call(globalScope, new Context(new JSObjectBase()));
            String result = printer.getJSString().toString();            
            assertEquals("[parent] [child1] [child2]\n\n", result);

            
            //modify the immediate template & see if it changes
            printer = new RedirectedPrinter();
            globalScope.set("print", printer);
            
            writer = new PrintWriter(child1File);
            writer.println("{% extends \"/local/parent.djang10\" %} {% block block1 %}[child1 modified]{% endblock %}");
            writer.close();
            Thread.sleep(SLEEP_MS);           

            
            child1 = (Djang10CompiledScript)fileLib.get(child1File.getName());
            child1.call(globalScope, new Context(new JSObjectBase()));
            result = printer.getJSString().toString();

            assertEquals("[parent] [child1 modified] [child2]\n\n", result);
            
            //modify transitive
            printer = new RedirectedPrinter();
            globalScope.set("print", printer);
            
            writer = new PrintWriter(child2File);
            writer.println("[child2 modified]");
            writer.close();
            Thread.sleep(SLEEP_MS);
            
            
            child1 = (Djang10CompiledScript)fileLib.get(child1File.getName());
            child1.call(globalScope, new Context(new JSObjectBase()));
            result = printer.getJSString().toString();
            
            assertEquals("[parent] [child1 modified] [child2 modified]\n\n", result);
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
        Scope globalScope = Scope.newGlobal().child();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();
        
        try {
            //Load native objects
            Logger log = Logger.getRoot();
            globalScope.set("log", log);
            log.makeThreadLocal();
            
            Encoding.install(globalScope);
            JSHelper helper = Djang10Source.install(globalScope);
            globalScope.set("local", new JSFileLibrary(new File(TEST_DIR), "local", globalScope));
    
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
            helper.addTemplateRoot(globalScope, new JSString("/local"));

        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
        return globalScope;
    }
}
