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

package ed.appserver.templates.djang10;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import ed.TestCase;
import ed.appserver.JSFileLibrary;
import ed.io.StreamUtil;
import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSFunction;
import ed.js.JSLocalFile;
import ed.js.JSObjectBase;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.log.Logger;


public class Djang10TemplateTest {
    public Djang10TemplateTest() { }
    
    @Factory
    public Object[] getAllTests(){
        if(Djang10Source.DEBUG)
            System.out.println("Loading Djang10Template Tests");

        //Initialize Scope ==================================
        Scope oldScope = Scope.getThreadLocal();
        Scope globalScope = Scope.newGlobal().child();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();
        
        try {
            //Load native objects
            globalScope.set("log", Logger.getLogger("django test"));            
            Encoding.install(globalScope);
            JSHelper helper = Djang10Source.install(globalScope);
            globalScope.set("local", new JSFileLibrary(new File("src/test/ed/appserver/templates/djang10"), "local", globalScope));
    
            globalScope.set("SYSOUT", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    System.out.println(p0);
                    return null;
                }
            });
            globalScope.put("openFile", new JSFunctionCalls1() {
                public Object call(Scope s, Object name, Object extra[]) {
                    return new JSLocalFile(new File("src/test/ed/appserver/templates/djang10"), name.toString());
                }
            }, true);
            
            //configure Djang10 =====================================
            helper.addTemplateRoot.call(globalScope, new JSString("/local"));
            helper.addModuleRoot.call(globalScope, new JSString("/local/support"));
    
            
            
            //setup context ==========================================
            Context context = new Context(new JSObjectBase());
            context.set( "foo" , "17" );
            context.set( "a" , "A" );
            context.set( "b" , "B" );
            context.set( "c" , "A" );
            
            JSObjectBase nested = new JSObjectBase();
            final JSObjectBase nested3 = new JSObjectBase();
            JSFunction nested2Fn = new JSFunctionCalls0() {
                public Object call(Scope scope, Object[] extra) {
                    return nested3;
                }
            };
            
            context.set("nested", nested);
            nested.set("nested2Fn", nested2Fn);
            nested3.set("last", "moo");
            
            
            JSArray array = new JSArray();
            for(int i=0; i<5; i++)
                array.add(i);
            context.set("array", array);
            
            context.set("urlParam", "?\\/~!.,&<>");
            
            Calendar cal = new GregorianCalendar();
            cal.set(1981, 12 - 1, 20, 15, 11, 37);
            context.set("date", new JSDate(cal));  
            
            context.set("includedTemplate", "/local/djang10-if");
            
            JSArray array2 = new JSArray();
            int[] array2values = new int[] { 5,4,3,2,1,6,7,8,9,10 };
            for(int val : array2values) {
                JSObjectBase obj = new JSObjectBase();
                obj.set("myProp", val);
                array2.add(obj);
            }
            context.set("array2", array2);
            
            /* DISABLED
            JSFileLibrary localLib = (JSFileLibrary)testScope.get("local");
            o.set("includedTemplateJsFunction", localLib.get("djang10-if"));
            */
            context.set("echoFunc", new JSFunctionCalls1() {
               public Object call(Scope scope, Object in, Object[] extra) {
                   return in;
               }
            });
            
            context.set("prototypedObj", globalScope.child().eval("var PrototypedClazz = function() {}; PrototypedClazz.prototype.getProp = function() { return 'moo'; }; return new PrototypedClazz();") );
    
            JSArray regroup_list = new JSArray();
            for(int groupId=0; groupId<4; groupId ++) {
                for(int objId = 0; objId < 3; objId ++) {
                    JSObjectBase obj = new JSObjectBase();
                    obj.set("prop1", "group#"+groupId);
                    obj.set("prop2", "group#"+groupId + "obj#" + objId);
                    regroup_list.add(obj);
                }
            }
            context.set("regroup_list", regroup_list); 
            
            
            //load the tests =========================
            final File dir = new File("src/test/ed/appserver/templates/djang10");
            final List<FileTest> tests = new ArrayList<FileTest>();
            for(File f : dir.listFiles()) { 
                if(f.getPath().endsWith(".djang10")) {
                    Scope testScope = globalScope.child();
                    testScope.setGlobal(true);
                    
                    FileTest fileTest = new FileTest(testScope, context, f);
                    tests.add(fileTest);
                }
            }
            
            return tests.toArray();

        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
    }


    public static class FileTest implements ITest {
        private final File file;
        private final Scope scope;
        private final Context context;
        private StringBuilder output;
        private Scope oldScope;
        
        public FileTest(Scope scope, Context context, File file) {
            this.scope = scope;
            this.context = context;
            this.file = file;
            oldScope = null;
        }
        
        @BeforeClass()
        public void setup() {
            //Setup scope
            oldScope = Scope.getThreadLocal();
            scope.makeThreadLocal();
            
            output = new StringBuilder();
            scope.set("print", new JSFunctionCalls1() {
                public Object call(Scope scope, Object p0, Object[] extra) {
                    output.append(p0);
                    return null;
                }
            });
            
            context.push();
        }
        
        @AfterClass
        public void teardown() {
            context.pop();
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
            
            oldScope = null;
            output = null;
        }
        
        @Test
        public void test() throws Throwable {           
            if(Djang10Source.DEBUG) {
                System.out.println("Testing: " + file.getName());
            }
            
            try {
                Djang10Source source = new Djang10Source(file);
                Djang10CompiledScript compiled = (Djang10CompiledScript)source.getFunction();
                compiled.call(scope.child(), context);
            } catch(Throwable t) {
                throw new Exception("For file " + file.toString(), t);
            }
            
            String got = _clean( output.toString() );
            
            File resultFile = new File( file.getAbsolutePath().replaceAll( ".djang10$" , ".out" ) );
            if ( ! resultFile.exists() )
                resultFile = new File( file.getAbsolutePath() + ".out" );
            String expected = _clean( StreamUtil.readFully( new FileInputStream( resultFile ) ) );
            
            TestCase.assertClose( expected , got, "Error : " + file + " : " );
        }

        public String getTestName() {
            return file.getName();
        }
    }
    
    static String _clean( String s ){
        s = s.replaceAll( "[\\s\r\n]+" , "" );
        s = s.replaceAll( " +>" , ">" );
        return s;
    }
}
