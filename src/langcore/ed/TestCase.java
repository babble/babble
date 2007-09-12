// TestCase.java

package ed;

import java.util.*;
import java.lang.reflect.*;

public class TestCase extends MyAsserts {
    
    static class Test {
        Test( Object o , Method m ){
            _o = o;
            _m = m;
        }
        
        Result run(){
            try {
                _m.invoke( _o );
                return new Result( this );
            }
            catch ( IllegalAccessException e ){
                return new Result( this , e );
            }
            catch ( InvocationTargetException ite ){
                return new Result( this , ite.getTargetException() );
            }
        }

        public String toString(){
            return _o.getClass().getName() + "." + _m.getName();
        }

        final Object _o;
        final Method _m;
    }

    static class Result {
        Result( Test t ){
            this( t , null );
        }

        Result( Test t , Throwable error ){
            _test = t;
            _error = error;
        }

        boolean ok(){
            return _error == null;
        }

        public String toString(){
            return _test.toString() + "\t " + _error.toString();
        }

        final Test _test;
        final Throwable _error;
    }


    /**
     * this is for normal class tests
     */
    protected TestCase(){
        for ( Method m : getClass().getDeclaredMethods() ){
            if ( m.getName().startsWith( "test" ) )
                _tests.add( new Test( this , m ) );
        }
        System.out.println( _tests );
    }

    public TestCase( Object o , String m )
        throws NoSuchMethodException {
        this( o , o.getClass().getDeclaredMethod( m ) );
    }

    public TestCase( Object o , Method m ){
        _tests.add( new Test( o , m ) );
    }

    public void runConsole(){
        List<Result> errors = new ArrayList<Result>();
        List<Result> fails = new ArrayList<Result>();

        System.out.println( "Num Tests : " + _tests.size() );
        System.out.println( "----" );

        for ( Test t : _tests ){
            Result r = t.run();
            if ( r.ok() ){
                System.out.print(".");
                continue;
            }
            
            System.out.print( "x" );

            if ( r._error instanceof MyAssert )
                fails.add( r );
            else
                errors.add( r );
        }
        
        System.out.println( "\n----" );

        int pass = _tests.size() - ( errors.size() + fails.size() );

        System.out.println( "% Pass : " + ( ((double)pass) / _tests.size() ) );
        if ( pass == _tests.size() ){
            System.out.println( "SUCCESS" );
            return;
        }
        
        System.err.println( "Num Pass : " + ( _tests.size() - ( errors.size() + fails.size() ) ) );
        System.err.println( "Num Erros : " + (  errors.size() ) );
        System.err.println( "Num Fails : " + (  fails.size() ) );

        System.err.println( "---------" );
        System.err.println( "ERRORS" );
        for ( Result r : errors )
            System.err.println( r );

        System.err.println( "---------" );
        System.err.println( "FAILS" );
        for ( Result r : fails )
            System.err.println( r );
        
    }

    public String toString(){
        return  "TestCase numCase:" + _tests.size();
    }
    
    final List<Test> _tests = new ArrayList<Test>();
    
    protected static void run( String args[] ){
        Args a = new Args( args );
        
        boolean foundMe = false;
        String theClass = null;
        for ( StackTraceElement ste : Thread.currentThread().getStackTrace() ){
            if ( foundMe ){
                theClass = ste.getClassName();
                break;
            }
            
            if ( ste.getClassName().equals( "ed.TestCase" ) && 
                 ste.getMethodName().equals( "run" ) )
                foundMe = true;
        }
        
        if ( theClass == null )
            throw new RuntimeException( "something is broken" );
        
        try {
            Class c = Class.forName( theClass );
            Object o = c.newInstance();
            
            System.out.println( o );
            if ( ! ( o instanceof TestCase ) )
                throw new RuntimeException( "you are stupid" );
            
            TestCase tc = (TestCase)o;
            if ( a.getOption( "m" ) != null )
                tc = new TestCase( tc , a.getOption( "m" ) );
            
            tc.runConsole();
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }

    public static void main( String args[] ){
        System.err.println( "Running all tests not implemented yet" );
    }
}
