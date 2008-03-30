// TestNGListener.java

package ed;

import java.util.*;

import org.testng.*;
import org.testng.reporters.*;

public class TestNGListener extends TestListenerAdapter {
    
    public void onTestFailure(ITestResult tr) {
        log("F");
    }
    
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
    }

    public void onFinish(ITestContext context){
        System.out.println();
        for ( ITestResult r : context.getFailedTests().getAllResults() ){
            System.out.println( r );
            if ( r.getThrowable() != null )
                _print( r.getThrowable() );
        }
    }
    
    private void _print( Throwable t ){
        for ( StackTraceElement e : t.getStackTrace() ){
            if ( e.getClassName().startsWith( "org.testng." ) ||
                 e.getClassName().startsWith( "sun.reflect." ) )
                continue;

            System.out.println( e );
        }
    }
    
    private int _count = 0;
} 
