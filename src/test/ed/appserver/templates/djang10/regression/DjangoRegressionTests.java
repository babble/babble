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
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.Djang10Source;
import ed.appserver.templates.djang10.JSHelper;
import ed.db.JSHook;
import ed.js.Encoding;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSException;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.JSRegex;
import ed.js.JSString;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import ed.js.func.JSFunctionCalls1;
import ed.js.func.JSFunctionCalls2;
import ed.log.Appender;
import ed.log.Level;
import ed.log.Logger;

public class DjangoRegressionTests {
    private static final String TEST_DIR =  "src/test/ed/appserver/templates/djang10/regression/";
    
    private static final String[] UNSUPPORTED_TESTS = {
        //unimplemented tags:
        "^cache.*",
        "^url.*",

        //requires architecture impl
        "^i18n.*",
        "basic-syntax27",   //need support for translating _( )
        "ifequal-numeric07", //need to preprocess: throw error on 2.

        //need to add custom tags to test
        "inheritance17",
        "inheritance18",
        "inheritance19",

        //need to add template wrapper to test
        "inheritance24",
        "inheritance25",

       
        //python & js have different string representations of types
        "filter-make_list0[1-4]", 
        
        //all keys are strings in js
        "list-index07",
        
        
        //allowed
        "filter-syntax03",  //spaces are allowed around filter seperators
        "filter-syntax04",  //spaces are allowed around filter seperators
        "exception01",      //throwing a render exception is more appropriate
        "exception02",      //throwing a render exception is more appropriate
        "autoescape-filtertag01", //why shouldn't you be able to apply safe & escape filters?
        "widthratio10",     //floats allowed
        
        "basic-syntax12",   //dunno
        "basic-syntax14",   //dunno
        
        //fix exceptions
        "filter-syntax14",
    };    
       
    public DjangoRegressionTests(){ }
    
    private String getBasePath() {
        String basePath = (JSHook.whereIsEd == null)? "" : JSHook.whereIsEd + "/";
        basePath += TEST_DIR;
        
        return basePath;
    }
    
    private Scope initScope() throws IOException {
        Scope oldScope = Scope.getThreadLocal();
        Scope globalScope = Scope.newGlobal().child();
        globalScope.setGlobal(true);
        globalScope.makeThreadLocal();

        //Load native objects
        Logger log = Logger.getRoot();
        JSArray defaultAppenders = ((JSArray) Logger.getRoot().get("appenders"));
        final List<Appender> oldAppenders = new ArrayList<Appender>(defaultAppenders);

        defaultAppenders.clear();
        defaultAppenders.add(new Appender() {
            public void append(String loggerName, JSDate date, Level level, String msg, Throwable throwable, Thread thread) {
                if(!loggerName.contains("djang10") || (Level.INFO.compareTo(level) < 0)) {
                    for(Appender appender : oldAppenders)
                        appender.append(loggerName, date, level, msg, throwable, thread);
                }
            }
        });
        
        globalScope.set("log", log);
        log.makeThreadLocal();        
        
        //override the Date object
        final long now_ms = System.currentTimeMillis();
        globalScope.set("OldDate", globalScope.get("Date"));
        globalScope.set("Date", new JSFunctionCalls0() {
            public Object call(Scope scope, Object[] extra) {
                Object thisObj = scope.getThis();
                
                if((extra != null && extra.length > 0) || !(thisObj instanceof JSDate))
                    throw new IllegalStateException("Date has been intentionally crippled & can only be used as paramless constructor ");
                
                JSDate thisDate = (JSDate)thisObj;
                thisDate.setTime(now_ms);
                return null;
            }
            public JSObject newOne() {
                return JSDate._cons.newOne();
            }
        });
        
        try {
            Encoding.install(globalScope);
            JSHelper.install(globalScope, Collections.EMPTY_MAP, Logger.getRoot());
            
            JxpSource preambleSource = JxpSource.getSource(new File(getBasePath(), "preamble.js"));
            preambleSource.getFunction().call(globalScope);
        }
        finally {
            if(oldScope != null) oldScope.makeThreadLocal();
            else Scope.clearThreadLocal();
        }
        
        log.getChild("djang10").setLevel(Level.DEBUG);
        
        return globalScope;
    }
    
    @Factory
    public Object[] getAllTests()  throws IOException, ClassNotFoundException {
        Scope globalScope = initScope();
        
        //Load the test scripts & pull out variables
        final List<TestCase> testCases = new ArrayList<TestCase>();
        int count = 0, skipped = 0;
        
        
        for(String testFilename : new String[] {"tests.js", "filter_tests.js", "missing_tests.js"}) {
            String path = new File(getBasePath(), testFilename).getAbsolutePath();
            JSTestScript testScript = new JSTestScript(globalScope, path);
            
            for(Object jsTestObj : testScript.tests) {
                JSObject jsTest = (JSObject)jsTestObj;
                JSObject results = (JSObject)jsTest.get("results");
                
                TestCase testCase;
                
                try {
                    if(results == null)
                        throw new NullPointerException("Results were null");
    
                    else if(testScript.exceptionCons.values().contains(results) 
                            || results.getConstructor() == testScript.nativeExceptionCons 
                            || results.getConstructor() == testScript.exceptionStackCons)
                        testCase = new ExpectedErrorTestCase(globalScope, testScript, jsTest);
                    
                    else
                        testCase = new NormalTestCase(globalScope, testScript, jsTest);
                }
                catch(Exception e) {
                    throw new RuntimeException("Failed to load test: " + jsTest.get("name") + ", in script: " + path, e);
                }
                
                if(isSupported(testScript, testCase)) testCases.add(testCase);
                else skipped++;
                
                count++;
            }
        }

        //Create a custom template loader
        JSFunctionCalls2 custom_loader = new JSFunctionCalls2() {
            public Object call(Scope scope, Object templateNameObj, Object p1, Object[] extra) {
                for(Object testObj : testCases) {
                    TestCase testCase = (TestCase)testObj;
                    if(templateNameObj.toString().equals(testCase.name))
                        try {
                            return (new Djang10Source(testCase.content)).getFunction();
                        } catch(Throwable t) {
                            throw new RuntimeException(t);
                        }
                }
                return null;
            }
        };
        
        JSHelper jsHelper = JSHelper.get(globalScope);
        JSArray loaders = (JSArray) jsHelper.get("TEMPLATE_LOADERS");
        loaders.clear();
        loaders.add(custom_loader);
       
        return testCases.toArray();
    }
        
    
    
    // ==============================================
    
    private static class JSTestScript {
        public enum JSExceptionName {
            TemplateSyntaxError,
            SomeException,
            SomeOtherException
        }
        public final File file; 
        public final Hashtable<JSExceptionName,JSFunction> exceptionCons;
        public final JSFunction nativeExceptionCons, exceptionStackCons;
        public final JSArray tests;
        
        public JSTestScript(Scope globalScope, String path) throws IOException {
            Scope oldScope = Scope.getThreadLocal();
            try {
                //create isolated scope for the script
                Scope loadingScope = globalScope.child();
                loadingScope.setGlobal(true);
                loadingScope.makeThreadLocal();
                
                //invoke the script
                this.file = new File(path);
                JxpSource testSource = JxpSource.getSource(this.file);
                JSFunction compiledTests = testSource.getFunction();
                compiledTests.call(loadingScope);
            
                //pull out exported classes
                exceptionCons = new Hashtable<JSExceptionName, JSFunction>();
                for(JSExceptionName name : JSExceptionName.values()) {
                    JSFunction cons = (JSFunction)loadingScope.get(name.name());
                    exceptionCons.put(name, cons);
                }
                nativeExceptionCons = (JSFunction)loadingScope.get("NativeExceptionWrapper");
                exceptionStackCons = (JSFunction)loadingScope.get("ExceptionStack");
                
                tests = (JSArray)loadingScope.get("tests");
            }
            finally {
                if(oldScope != null) oldScope.makeThreadLocal();
                else Scope.clearThreadLocal();
            }
        }
    }
    
    private static boolean isSupported(JSTestScript script, TestCase testCase) {
        for(String unsupportedTest: UNSUPPORTED_TESTS)
            if(testCase.name.matches(unsupportedTest))
                return false;
        
        return true;
    }
    

    // ====================================
    public static abstract class TestCase implements ITest {
        protected final Scope globalScope;
        protected final JSTestScript script;
        protected final String name;
        protected final String content;
        protected final JSObject model;
        protected final List<JSRegex> expectedLogMessages, unexpectedLogMessages;
        
        
        protected Scope preTestScope;
        protected Scope testScope;
        protected Djang10Source source;
        protected StringBuilder outputBuffer;
        protected StringBuilder logMessages;
        
        public TestCase(Scope globalScope, JSTestScript script, JSObject test) {
            this.globalScope = globalScope.child();
            this.globalScope.lock();

            this.script = script;
            this.name = ((JSString)test.get("name")).toString();
            this.content = ((JSString)test.get("content")).toString();
            this.model = (JSObject)test.get("model");
            
            //Expected Log Messages
            JSArray expectedLogMessagesJsArr = (JSArray)test.get("logResults");
            expectedLogMessages = new ArrayList<JSRegex>();
            if(expectedLogMessagesJsArr != null) {
                for(Object expectedLogMsgObj : expectedLogMessagesJsArr) {
                    expectedLogMessages.add((JSRegex)expectedLogMsgObj);
                }
            }
            
            //Unexpected Log Messages
            JSArray unexpectedLogMessagesJsArr = (JSArray)test.get("unexpectedLogResults");
            unexpectedLogMessages = new ArrayList<JSRegex>();
            if(unexpectedLogMessagesJsArr != null) {
                for(Object unexpectedLogMsgObj : unexpectedLogMessagesJsArr) {
                    unexpectedLogMessages.add((JSRegex)unexpectedLogMsgObj);
                }
            }
        }
        @BeforeMethod
        public void setup() {
            preTestScope = Scope.getThreadLocal();
            testScope = globalScope.child();
            testScope.makeThreadLocal();
            
            this.source = new Djang10Source(this.content);
            
            outputBuffer = new StringBuilder();
            testScope.set("print", printer);
            logMessages = new StringBuilder();
            ((JSArray) Logger.getRoot().get("appenders")).add(appender);
        }
        @AfterMethod
        public void teardown() {
            if(preTestScope != null) preTestScope.makeThreadLocal();
            else Scope.clearThreadLocal();
            testScope = null;
            
            this.source = null;
            
            ((JSArray) Logger.getRoot().get("appenders")).remove(appender);
            outputBuffer = null;
            logMessages = null;
        }
        
        @Test
        public void test() throws IOException {
            try {
                JSFunction fn = source.getFunction();
                fn.call(testScope, model);
                
                handleSuccess();
            } catch(RuntimeException e) {
                handleError(e);
            }
            //make sure that the expected log messages were logged
            String log = logMessages.toString();
            for(JSRegex r : expectedLogMessages) {
                if(!r.test(log))
                    fail("Log message not found: " + r.toPrettyString() + ", actual messages:\n" + log);
            }
        }
        
        public String getTestName() {
            return name;
        }
        
        public abstract void handleSuccess();
        public abstract void handleError(RuntimeException e);
        
        private final JSFunctionCalls1 printer = new JSFunctionCalls1() {
            public Object call(Scope scope, Object p0, Object[] extra) {
                outputBuffer.append(p0);
                return null;
            };
        };
        private final Appender appender = new Appender() {
            public void append(String loggerName, JSDate date, Level level, String msg, Throwable throwable, Thread thread) {
                logMessages.append(loggerName)
                    .append(' ')
                    .append(level)
                    .append(' ')
                    .append(msg)
                    .append('\n');
                
                if(throwable != null) {
                    StringWriter buffer = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(buffer));
                    logMessages.append(buffer);
                }
            };
        };       
    }
    
    
    // ====================================
    public static class NormalTestCase extends TestCase {
        public final String normal;
        public final String invalid;
        public final String invalid_setting;
        
        public NormalTestCase(Scope globalScope, JSTestScript script, JSObject test) {
            super(globalScope, script, test);
            
            JSObject results = (JSObject)test.get("results");
            if(results instanceof JSString) {
                normal = invalid = results.toString();
                invalid_setting = "INVALID";
            }
            else if(results instanceof JSArray) {
                JSArray array = (JSArray)test.get("results");
                normal = ((JSString) array.get(0)).toString();
                
                String temp = ((JSString)array.get(1)).toString(); 
                
                if(temp.contains("%s")) {
                    invalid_setting = "INVALID %s";
                    invalid = temp.replace("%s", ((JSString)array.get(2)).toString());
                }
                else {
                    invalid = temp;
                    invalid_setting = "INVALID";
                }
            }
            else {
                throw new IllegalStateException("Don't know what to do with the normal result: " + results.getClass().getName());
            }
            
            
        }
        
        public void handleSuccess() {
            assertEquals(normal, outputBuffer.toString());
        }
        
        public void handleError(RuntimeException arg0) {
            throw arg0;
        }
    }
    
    // ====================================
    public static class ExpectedErrorTestCase extends TestCase {
        private List<Object> exceptionStack;
        
        public ExpectedErrorTestCase(Scope globalScope, JSTestScript script, JSObject test) throws ClassNotFoundException {
            super(globalScope, script, test);
            
            exceptionStack = new ArrayList<Object>();
            
            
            JSObject resultsObj = (JSObject)test.get("results");
            JSArray resultsArray;
            
            //handle cause chain
            if(resultsObj.getConstructor() == script.exceptionStackCons) {
                resultsArray = (JSArray)resultsObj.get("stack");
            }
            //top level exception only
            else {
                resultsArray = new JSArray();
                resultsArray.add(resultsObj);
            }
            
            for(Object result : resultsArray) {
                JSObject jsResult = (JSObject)result;
                
                //unpack native exception class
                if(jsResult.getConstructor() == script.nativeExceptionCons) {
                    Class expectedEClass = Class.forName(jsResult.get("className").toString());
                    exceptionStack.add(expectedEClass);
                }
                //js exception constructor
                else if(jsResult instanceof JSFunction){
                    exceptionStack.add(jsResult);
                }
                else {
                    throw new IllegalStateException("Don't know what to do with a an expected expected exception of type: " + jsResult.getClass().getName());
                }
            }
        }
        public void handleError(RuntimeException actucalE) {
            for(Object expectedE : exceptionStack) {
                //Native exception
                if(expectedE instanceof Class) {
                    Class<RuntimeException> expectedEClass = (Class<RuntimeException>)expectedE;
                    if(!expectedEClass.isAssignableFrom(actucalE.getClass()))
                        fail("Expected Native exception: " + expectedEClass.getName() + ", but got: " + actucalE.getClass().getName());
                }
                //JSException
                else if(expectedE instanceof JSFunction){
                    JSFunction expectedECons = (JSFunction)expectedE;
                    
                    JSObject actualJsE;
                    if(actucalE instanceof JSException)
                        actualJsE = (JSObject) ((actucalE.getCause() instanceof JSObject)? actucalE.getCause() : ((JSException)actucalE).getObject());
                    else if(actucalE instanceof JSObject)
                        actualJsE = (JSObject)actucalE;
                    else
                        throw new IllegalStateException("Don't know what to do with the expected exception " + actucalE);
                    
                    assertEquals(expectedECons, actualJsE.getConstructor());
                }            }
        }
        public void handleSuccess() {
            fail("Expected exception not thrown");
        }
    }
}
