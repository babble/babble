package ed.appserver.templates.djang10;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import ed.TestCase;
import ed.appserver.JSFileLibrary;
import ed.io.StreamUtil;
import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSFunction;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;

public class Djang10TemplateTest extends TestCase {
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.TEMPLATES" );
    
    private List<File> files = new ArrayList<File>();
    private  ArrayList<TestCase> _all = new ArrayList<TestCase>();
    
    
    public Djang10TemplateTest() {
        File dir = new File("src/test/ed/appserver/templates/djang10");
        
        for(File f : dir.listFiles()) { 
            if(f.getPath().endsWith(".djang10")) {
//                if(!f.getPath().endsWith("defaultFilterTest.djang10"))
//                    continue;
                
                
                FileTest fileTest = new FileTest(f);
                _all.add(fileTest);
                add(fileTest);
                files.add(f);
            }
        }
        return;
    }

    @Factory 
    public Object[] getAllTests(){
        return _all.toArray();
    }
    
    
    Scope initScope() {
        Scope scope = Scope.getAScope().child();
        scope.makeThreadLocal();
        
        Encoding.install(scope);
        
        JSFileLibrary localLib = new JSFileLibrary(new File("src/test/ed/appserver/templates/djang10"), "local", scope);
        scope.put("local", localLib, true);

        JSHelper helper = Djang10Source.install(scope);
        helper.addTemplateRoot.call(scope, new JSString("/local"));
        
        helper.addModuleRoot.call(scope, new JSString("/local/support"));
        
        return scope;
    }

    public void cleanup() {
        Scope.clearThreadLocal();
    }

    Context getArgs(Scope testScope){
        Context o = new Context(new JSObjectBase());
        o.set( "foo" , "17" );
        o.set( "a" , "A" );
        o.set( "b" , "B" );
        o.set( "c" , "A" );
        
        JSObjectBase nested = new JSObjectBase();
        final JSObjectBase nested3 = new JSObjectBase();
        JSFunction nested2Fn = new JSFunctionCalls0() {
            @Override
            public Object call(Scope scope, Object[] extra) {
                return nested3;
            }
            
        };
        
        o.set("nested", nested);
        nested.set("nested2Fn", nested2Fn);
        nested3.set("last", "moo");
        
        
        JSArray array = new JSArray();
        for(int i=0; i<5; i++)
            array.add(i);
        o.set("array", array);
        
        
        o.set("urlParam", "?\\/~!.,&<>");
        
        Calendar cal = new GregorianCalendar();
        cal.set(1981, 12 - 1, 20, 15, 11, 37);

        
        o.set("date", new JSDate(cal));  
        
        o.set("includedTemplate", "/local/djang10-if");
        
        JSArray array2 = new JSArray();
        int[] array2values = new int[] { 5,4,3,2,1,6,7,8,9,10 };
        for(int val : array2values) {
            JSObjectBase obj = new JSObjectBase();
            obj.set("myProp", val);
            array2.add(obj);
        }
        o.set("array2", array2);
        
        /* DISABLED
        JSFileLibrary localLib = (JSFileLibrary)testScope.get("local");
        o.set("includedTemplateJsFunction", localLib.get("djang10-if"));
        */
        o.set("echoFunc", new JSFunctionCalls1() {
           public Object call(Scope scope, Object in, Object[] extra) {
               return in;
           }
        });
        
        o.set("prototypedObj", testScope.eval("function PrototypedClazz() {}; PrototypedClazz.prototype.getProp = function() { return 'moo'; }; return new PrototypedClazz();") );

        return o;
    }

    public class FileTest extends TestCase {
        private File file;

        public FileTest(File file) {
            this.file = file;
        }
        
        @Test
        public void test() throws Throwable {
            Scope scope = initScope();
            final StringBuilder output = new StringBuilder();
            
            JSFunction myout = new JSFunctionCalls1() {
                public Object call(Scope scope, Object o, Object extra[]) {
                    output.append(o).append("\n");
                    return null;
                }
            };

            
            
            scope.put("print", myout, true);
            scope.put("SYSOUT", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    System.out.println(p0);
                    return null;
                }
            }, true);

            Context args = getArgs(scope);
            
            // Convert to Javascript
            if (DEBUG) {
                System.out.println("*********************");
                System.out.println(file);
                System.out.println("*********************");
            }
            System.out.println("Djang10TemplateTest : testing = " + file);
            
            
            try {
                Djang10Source source = new Djang10Source(file);
                NodeList nodes = source.compile(scope);
                nodes.__render(scope, args , myout);
            } catch(Throwable t) {
                while(t != null) {
                    t.printStackTrace(System.err);
                 
                    t=t.getCause();
                    if(t != null) {
                        System.err.println("caused by:");
                    }
                }
                throw t;
            }
            
            String got = _clean( output.toString() );
            
            File resultFile = new File( file.getAbsolutePath().replaceAll( ".djang10$" , ".out" ) );
            if ( ! resultFile.exists() )
                resultFile = new File( file.getAbsolutePath() + ".out" );
            String expected = _clean( StreamUtil.readFully( new FileInputStream( resultFile ) ) );
            
            assertClose( expected , got, "Error : " + file + " : " );
            
            
            return;
        }

    }
    
    static String _clean( String s ){
        s = s.replaceAll( "[\\s\r\n]+" , "" );
        s = s.replaceAll( " +>" , ">" );
        return s;
    }
    
    public static void main(String[] args) {
        Djang10TemplateTest test = new Djang10TemplateTest();
        test.run(new String[0]);
    }
}
