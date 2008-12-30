// TestNGListener.java

package ed;

import java.util.*;

import org.testng.*;
import org.testng.reporters.*;

public class TestNGListener extends TestListenerAdapter {
    
    public void onTestFailure(ITestResult tr) {
        log("F");
    }

//    public void onTestStart(ITestResult tr) {
//        log("Test : " + tr.getName() + "\n");
//    }
//
//    public void onStart(ITestContext tc) {
//        log("TestSet :" + tc.getName());
//    }

    public void onTestSkipped(ITestResult tr) {
        log("S");
    }
    
    public void onTestSuccess(ITestResult tr) {
        log(".");
    }

    private void log(String string) {
        System.out.print(string);
        if ( ++_count % 40 == 0) {
            System.out.println("");
        }
        System.out.flush();
    }

    public void onFinish(ITestContext context){
        System.out.println();
        for ( ITestResult r : context.getFailedTests().getAllResults() ){
            System.out.println(r);
            System.out.println("Exception : ");
            _print( r.getThrowable() );
        }
    }
    
    private void _print( Throwable t ){

        int otcount = 0;
        int jlrcount = 0;

        if (t == null) {
            return;
        }

        System.out.println("-" + t.toString()+ "-");

        for ( StackTraceElement e : t.getStackTrace() ){
            if ( e.getClassName().startsWith( "org.testng.")) {
                if (otcount++ == 0) {
                    System.out.println("  " + e + " (with others of org.testng.* omitted)");
                }
            }
            else if (e.getClassName().startsWith( "java.lang.reflect.") || e.getClassName().startsWith("sun.reflect.") ) {
                if (jlrcount++ == 0) {
                    System.out.println("  " + e  + " (with others of java.lang.reflect.* or sun.reflect.* omitted)");
                }
            }
            else {
                System.out.println("  " +  e );
            }
        }

        if (t.getCause() != null) {
            System.out.println("Caused By : ");
        
            _print(t.getCause());
        }

        System.out.println();
    }
    
    private int _count = 0;
} 
