package ed.appserver.templates.djang10.regression;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import ed.TestCase;
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

public class DjangoRegressionTests extends TestCase {
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
        "truncatewords",    //truncatewords
        "filter04",         //cut
        
        
        
        //requires architecture impl
        "^autoescape-.*",
        "^inheritance.*",
        "^include.*",
        "basic-syntax27",
        
        //will never be supported
        "^for-tag-unpack.*",
    };
    
    private final Scope scope;
    //private final JSObject HackTemplate;
    private final JSFunction TemplateSyntaxError, SomeException, SomeOtherException;

    private List<ExportedTestCase> testCases = new ArrayList<ExportedTestCase>(); 
    
    public DjangoRegressionTests() throws IOException {
        scope = initScope();
        
        //load the js tests
        String path = JSHook.whereIsEd;
        if ( path == null ) 
            path = "";
        else
            path += "/";
        path += "src/test/ed/appserver/templates/djang10/regression/tests.js";
        
        JxpSource exportedTestsSource = JxpSource.getSource(new File(path));
        JSFunction exportedTests = exportedTestsSource.getFunction();
        exportedTests.call(scope.child());
        
        //pull out exported classes
        //HackTemplate = (JSObject)scope.get("HackTemplate");
        TemplateSyntaxError = (JSFunction)scope.get("TemplateSyntaxError");
        SomeException = (JSFunction)scope.get("SomeException");
        SomeOtherException = (JSFunction)scope.get("SomeOtherException");
        
        //pull out the tests
        int unsupportedCount = 0;
        
        JSArray jsTestArr = (JSArray)scope.get("tests");
        for(Object jsTest : jsTestArr) {
            ExportedTestCase testCase = new ExportedTestCase(scope.child(), (JSObject)jsTest);
            
            if(!isSupported(testCase)) {
                unsupportedCount++;
                continue;
            }
            
            testCases.add(testCase);
            add(testCase);
        }
        System.out.println("Skipping " + unsupportedCount + " tests");
    }
    
    private static boolean isSupported(ExportedTestCase testCase) {
        //FIXME: tests that throw exceptions are not supported yet
        if(testCase.result instanceof ExceptionResult)
            return false;

        for(String unsupportedTest: UNSUPPORTED_TESTS)
            if(testCase.name.matches(unsupportedTest))
                return false;
        return true;
    }
    @Factory
    public Object[] getAllTests() {
        return testCases.toArray();
    }
    
    Scope initScope() {
        Scope scope = Scope.getAScope().child();
        scope.makeThreadLocal();
        
        Encoding.install(scope);
        
        Djang10Source.install(scope);
        
        return scope;
    }
    
    // ====================================
    
    public class ExportedTestCase extends TestCase {
        private final Scope scope;
        private final String name;
        private final String content;
        private final JSObject model;
        private final Result result;
        
        private final Printer printer;
        
        
        public ExportedTestCase(Scope scope, JSObject test) {
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
                if(temp == TemplateSyntaxError || temp == SomeException || temp == SomeOtherException)
                    this.result = new ExceptionResult(temp);
                else
                    throw new IllegalStateException("unkown type: " + temp);
            }
            
            
            printer = new Printer();
            scope.set("print", printer);
        }
        @Test
        public void testWrapper() {
            try {
                realTest();
            } catch(Throwable t) {
                throw new RuntimeException("Failed[" + name + "]: " + t + ". content: " + content);
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
    
    
    public static void main(String[] args) throws IOException {
        DjangoRegressionTests tests = new DjangoRegressionTests();
        
        tests.runConsole();
        return;
    }
}
