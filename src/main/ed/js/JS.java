// JS.java

package ed.js;

import java.io.*;

import ed.js.engine.*;

public class JS {

    public static boolean JNI = false;
    public static final boolean DI = false;
    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );

    public static void _debugSI( String name , String place ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " \t " + place  );
    }

    public static void _debugSIStart( String name ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " Start" );
    }

    public static void _debugSIDone( String name ){
        if ( ! DI )
            return;
        System.err.println( "Static Init : " + name + " Done" );
    }

    public static final int fun(){
        return 17;
    }

    public static final Object eval( String js ){
        JNI = true;

        try {
            Scope s = Scope.GLOBAL.child();
            Object ret = s.eval( js );
            return ret;
        }
        catch ( Throwable t ){
            t.printStackTrace();
            return null;
        }
    }

    public static final String toString( Object o ){
        if ( o == null )
            return null;
        
        return o.toString();
    }

    public static void main( String args[] )
        throws Exception {
        
        for ( String s : args ){
            System.out.println( "-----" );
            System.out.println( s );
            
            Convert c = new Convert( new File( s ) );
            c.get().call( Scope.GLOBAL.child() );
        }
    }
}
    
