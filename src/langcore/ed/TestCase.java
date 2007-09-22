// TestCase.java

package ed;

import java.io.*;
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
            String foo = _o.getClass().getName() + "." + _m.getName();
            if ( _name == null )
                return foo;
            return _name + "(" + foo + ")";
        }

        protected String _name = null;

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

    public TestCase(){ 
        this( null );
    }

    public TestCase( String name ){
        for ( Method m : getClass().getDeclaredMethods() ){
            if ( m.getName().startsWith( "test" ) ){
                Test t = new Test( this , m );
                t._name = name;
                _tests.add( t );
            }
        }
    }

    public TestCase( Object o , String m )
        throws NoSuchMethodException {
        this( o , o.getClass().getDeclaredMethod( m ) );
    }

    public TestCase( Object o , Method m ){
        _tests.add( new Test( o , m ) );
    }

    public void add( TestCase tc ){
        _tests.addAll( tc._tests );
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

        System.out.println( "% Pass : " + ( ((double)pass*100) / _tests.size() ) );
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

    public static void main( String args[] )
        throws Exception {
        
        Process p = Runtime.getRuntime().exec( "find src/test/" );
        BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        TestCase theTestCase = new TestCase();
        
        String line;
        while ( ( line = in.readLine() ) != null ){
            if ( ! line.endsWith( "Test.java" ) )
                continue;
            line = line.substring( 9 );
            line = line.substring( 0 , line.length() - 5 );
            line = line.replace( '/' , '.' );
            
            System.out.println( line );
            try {
                Class c = Class.forName( line );
                Object o = c.newInstance();
                TestCase tc = (TestCase)o;
                theTestCase._tests.addAll( tc._tests );
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }
        
        theTestCase.runConsole();
    }
}
