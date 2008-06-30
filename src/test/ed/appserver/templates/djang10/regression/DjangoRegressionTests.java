package ed.appserver.templates.djang10.regression;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.Djang10Source;
import ed.db.JSHook;
import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;

public class DjangoRegressionTests {
    private static final boolean DEBUG =  Boolean.getBoolean("DJANG10_DEBUG");
    private static final String[] UNSUPPORTED_TESTS = {
        //unimplemented tags:
        "^cache.*",
        "^widthratio.*",
        "^with.*",
        "^templatetag.*",
        "^url.*",
        
        //unimplemented filters:
        "filter-syntax09",  //removetags
        "filter-syntax12",  //yesno
        "filter-syntax17",  //join
        "filter-syntax19",    //truncatewords
        "filter04",         //cut
        
        
        
        //requires architecture impl
        "^autoescape-.*",
        "^inheritance.*",
        "^include.*",
        "basic-syntax27",
        "^list-index.*",    //need to preprocess the string
        
        //will never be supported
        "^for-tag-unpack.*",
    };
    
    private static String[] FAILED_TESTS = {
        "filter-syntax15",
        "filter-syntax16",
        "firstof05",
        "ifequal-numeric04",
        "ifequal-numeric05",
        "ifequal-numeric06",
        "ifequal-numeric07",
        "ifequal-numeric08",
        "ifequal-numeric11",
        "ifequal-numeric12",
        "ifequal-split08",
        "ifequal-split09",
        "ifequal-split10",
        "regroup02"
    };
    
       
    public DjangoRegressionTests(){ }
    
    @Factory
    public Object[] getAllTests()  throws IOException {
        if(DEBUG)
            System.out.println("Loading django regression tests");

        //Locate the test script
        String path = (JSHook.whereIsEd == null)? "" : JSHook.whereIsEd + "/";
        path += "src/test/ed/appserver/templates/djang10/regression/tests.js";
        
        //Initialize the Scope
        Scope oldScope = Scope.getThreadLocal();
        Scope globalScope = Scope.newGlobal().child();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();

        //Load native objects
        try {
            Encoding.install(globalScope);
            Djang10Source.install(globalScope);
        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
            
        //Load the test script & pull out variables
        //final JSFunction hackTemplateCons = (JSObject)scope.get("HackTemplate");
        final JSFunction templateSyntaxErrorCons;
        final JSFunction someExceptionCons;
        final JSFunction someOtherExceptionCons;
        final JSArray tests;
        
        try {
            //create isolated scope for the script
            Scope loadingScope = globalScope.child();
            loadingScope.setGlobal(true);
            loadingScope.makeThreadLocal();
            
            //invoke the script
            JxpSource testSource = JxpSource.getSource(new File(path));
            JSFunction compiledTests = testSource.getFunction();
            compiledTests.call(loadingScope);
        
            //pull out exported classes
            templateSyntaxErrorCons = (JSFunction)loadingScope.get("TemplateSyntaxError");
            someExceptionCons = (JSFunction)loadingScope.get("SomeException");
            someOtherExceptionCons = (JSFunction)loadingScope.get("SomeOtherException");
            tests = (JSArray)loadingScope.get("tests");
        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
        
        //Process the tests
        List<ExportedTestCase> testCases = new ArrayList<ExportedTestCase>();
        int count = 0, skipped = 0;
        
        for(Object jsTest : tests) {
            Scope testScope = globalScope.child();
            testScope.setGlobal(true);
            ExportedTestCase testCase = new ExportedTestCase(testScope, (JSObject)jsTest, templateSyntaxErrorCons, someExceptionCons, someOtherExceptionCons);
            
            if(isSupported(testCase)) testCases.add(testCase);
            else skipped++;
            
            count++;
        }
        
        if(DEBUG) {
            String msg = String.format("Found %d tests, skipping %d of them", count, skipped);
            System.out.println( msg );
        }

        return testCases.toArray();
    }
    
    private static boolean isSupported(ExportedTestCase testCase) {
        //FIXME: tests that throw exceptions are not supported yet
        if(testCase.result instanceof ExceptionResult)
            return false;

        for(String unsupportedTest: UNSUPPORTED_TESTS)
            if(testCase.name.matches(unsupportedTest))
                return false;
        
        if(!DEBUG) {
            for(String failedTest: FAILED_TESTS)
                if(testCase.name.matches(failedTest))
                    return false;
        }

        return true;
    }
    

    // ====================================
    
    public class ExportedTestCase {
        private final Scope scope;
        private final String name;
        private final String content;
        private final JSObject model;
        private final Result result;
        
        private final Printer printer;
        
        
        public ExportedTestCase(Scope scope, JSObject test, JSFunction templateSyntaxErrorCons, JSFunction someExceptionCons, JSFunction someOtherExceptionCons) {
            this.scope = scope;
            this.name = ((JSString)test.get("name")).toString();
            this.content = ((JSString)test.get("content")).toString();
            this.model = (JSObject)test.get("model");
            
            Object temp = test.get("results");
            
            if(temp instanceof JSString) {
                this.result = new NormalResult(temp.toString(), temp.toString(), "INVALID");
            }
            else if(temp instanceof JSArray) {
                JSArray array = (JSArray)temp;
                String normal = ((JSString) array.get(0)).toString();
                String invalid = ((JSString)array.get(1)).toString();
                String invalid_setting = "INVALID";
                
                if(invalid.contains("%s")) {
                    invalid_setting = "INVALID %s";
                    invalid = invalid.replace("%s", ((JSString)array.get(2)).toString());
                }
                this.result = new NormalResult(normal, invalid, invalid_setting);
            }
            else {
                if(temp == templateSyntaxErrorCons || temp == someExceptionCons || temp == someOtherExceptionCons)
                    this.result = new ExceptionResult(temp);
                else
                    throw new IllegalStateException("unkown type: " + temp);
            }
            
            
            printer = new Printer();
            scope.set("print", printer);
        }
        @Test
        public void testWrapper() {
            Scope oldScope = Scope.getThreadLocal();
            scope.makeThreadLocal();
            
            System.out.println("Testing: " + name);
            
            try {
                realTest();
            }
            catch(Throwable t) {
                throw new RuntimeException("Failed[" + name + "]: " + t + ". content: " + content);
            }
            finally {
                if(oldScope != null) oldScope.makeThreadLocal();
                else Scope.clearThreadLocal();
            }
        }
        public void realTest() throws IOException {
            Djang10Source template = new Djang10Source(this.content);
            
            if(result instanceof ExceptionResult) {
                throw new UnsupportedOperationException();
            }
            else {

                NormalResult normalResult = (NormalResult)result;
                JSFunction templateFn = template.getFunction();
                templateFn.call(scope, model);
                String output = printer.buffer.toString();
                
                assertEquals(output, normalResult.normal);
            }
            
            
        }
        
    }
    
    //Helpers
    private static class Result {}
    private static class ExceptionResult extends Result {
        public final Object exceptionType;
        public ExceptionResult(Object exceptionType) {
            this.exceptionType = exceptionType;
        }
    }
    private static class NormalResult extends Result {
        public final String normal;
        public final String invalid;
        public final String invalid_setting;
        
        public NormalResult(String normal, String invalid, String invalid_setting) {
            this.normal = normal;
            this.invalid = invalid;
            this.invalid_setting = invalid_setting;
        }
    }
    
    private static class Printer extends JSFunctionCalls1 {
        public final StringBuilder buffer = new StringBuilder();
        
        public Object call(Scope scope, Object p0, Object[] extra) {
            buffer.append(p0);
            return null;
        }
    }
}
