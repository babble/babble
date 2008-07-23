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
import ed.appserver.templates.djang10.JSHelper;
import ed.appserver.templates.djang10.Printer;
import ed.db.JSHook;
import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls2;

public class DjangoRegressionTests {
    private static final boolean DEBUG =  Boolean.getBoolean("DJANG10_DEBUG");
    private static final String[] UNSUPPORTED_TESTS = {
        //unimplemented tags:
        "^cache.*",
        "^url.*",

        //requires architecture impl
        "basic-syntax27",   //need support for translating _( )
        "^list-index.*",    //need to preprocess the string
        "ifequal-numeric07", //need to preprocess: throw error on 2.
        "^filter-truncatewords02",  //needs autoescape

        //django doesn't escape strings
        "ifequal-split09",
        "ifequal-split10",
        "filter-syntax15",
        "filter-syntax16",

        //need to add custom tags to test
        "inheritance17",
        "inheritance18",
        "inheritance19",

        //requires block.super
        "inheritance20",
        "inheritance21",
        "inheritance22",
        "inheritance23",

        //need to add template wrapper to test
        "inheritance24",
        "inheritance25",

        //will never be supported
        "^for-tag-unpack.*",
        
        //unimplemented filters
        "chaining(07|08|09|10)",   //missing force_escape
        "chaining(11|14|12|13)",       //missing safe
        "autoescape-filtertag01$",  //missing safe
        "autoescape-stringfilter0[2-4]",    //missing safe
        "filter-cut.*",
        "filter-escape.*",
        "filter-first.*",
        "filter-force-escape.*",
        "filter-iriencode.*",
        "filter-last.*",
        "filter-make_list0[3,4]",   //needs stringformat 
        "filter-phone2numeric.*",
        "filter-random.*",
        "filter-safe.*",
        "filter-slice.*",
        "filter-slugify.*",
        "filter-stringformat.*",
        "filter-timesince.*",
        "filter-timeuntil.*",
        "filter-unordered.*",
        "filter-urlize.*",
        "filter-wordwrap.*",

        
        //broken filters
        "chaining0[35]",
        "filter-make_list0[1,2]", //js & python print different representations of arrays
    };    
       
    public DjangoRegressionTests(){ }
    
    @Factory
    public Object[] getAllTests()  throws IOException {
        if(DEBUG)
            System.out.println("Loading django regression tests");

        //Locate the test script
        String basePath = (JSHook.whereIsEd == null)? "" : JSHook.whereIsEd + "/";
        basePath += "src/test/ed/appserver/templates/djang10/regression/";
        
        //Initialize the Scope
        Scope oldScope = Scope.getThreadLocal();
        Scope globalScope = Scope.newGlobal().child();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();

        //Load native objects
        JSHelper jsHelper;
        try {
            Encoding.install(globalScope);
            jsHelper = Djang10Source.install(globalScope);
        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }

        //Load the test scripts & pull out variables
        final List<ExportedTestCase> testCases = new ArrayList<ExportedTestCase>();
        int count = 0, skipped = 0;
        
        //FIXME: enable the filter tests
        for(String testFilename : new String[] {"tests.js", "filter_tests.js"}) {
            String path = basePath + testFilename;
            JSTestScript testScript = new JSTestScript(globalScope, path);
            
            for(Object jsTest : testScript.tests) {
                Scope testScope = globalScope.child();
                testScope.setGlobal(true);
                
                ExportedTestCase testCase = new ExportedTestCase(testScope, (JSObject)jsTest, 
                        testScript.templateSyntaxErrorCons, testScript.someExceptionCons, testScript.someOtherExceptionCons);

                if(isSupported(testCase)) testCases.add(testCase);
                else skipped++;
                
                count++;
            }
        }

        //Create a custom template loader
        JSFunctionCalls2 custom_loader = new JSFunctionCalls2() {
            public Object call(Scope scope, Object templateNameObj, Object p1, Object[] extra) {
                for(Object testObj : testCases) {
                    ExportedTestCase testCase = (ExportedTestCase)testObj;
                    if(templateNameObj.toString().equals(testCase.name))
                        try {
                            return (new Djang10Source(testCase.content)).getFunction();
                        } catch(Throwable t) {
                            throw new RuntimeException(t);
                        }
                }
                throw new JSException(new JSString("Template not found") );
            }
        };
        JSArray loaders = (JSArray) jsHelper.get("TEMPLATE_LOADERS");
        loaders.clear();
        loaders.add(custom_loader);
        
        if(DEBUG) {
            String msg = String.format("Found %d tests, skipping %d of them", count, skipped);
            System.out.println( msg );
        }

        return testCases.toArray();
    }
    
    private static class JSTestScript {
        //final JSFunction hackTemplateCons = (JSObject)scope.get("HackTemplate");
        public final JSFunction templateSyntaxErrorCons;
        public final JSFunction someExceptionCons;
        public final JSFunction someOtherExceptionCons;
        public final JSArray tests;
        
        public JSTestScript(Scope globalScope, String path) throws IOException {
            Scope oldScope = Scope.getThreadLocal();
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
        }
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
    

    // ====================================
    
    public class ExportedTestCase {
        private final Scope scope;
        private final String name;
        private final String content;
        private final JSObject model;
        private final Result result;
        
        private final Printer.RedirectedPrinter printer;
        
        
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
            
            
            printer = new Printer.RedirectedPrinter();
            scope.set("print", printer);
        }
        @Test
        public void testWrapper() {
            Scope oldScope = Scope.getThreadLocal();
            scope.makeThreadLocal();

            if(DEBUG)
                System.out.println("Testing: " + name);
            
            try {
                realTest();
            }
            catch(Throwable t) {
                throw new RuntimeException("Failed[" + name + "]: " + t + ". content: " + content, t);
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
                String output = printer.getJSString().toString();
                
                assertEquals(normalResult.normal, output);
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
}
