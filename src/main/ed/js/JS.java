// JS.java

package ed.js;

import java.io.*;

import ed.js.engine.*;

public class JS {
    
    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );

    public static final int fun(){
        return 17;
    }

    public static final Object eval( String js ){
        System.out.println( "going to eval : " + js );
        System.out.println( "yay" );
        try {
            Scope s = Scope.GLOBAL.child();
            Object ret = s.eval( js );
            System.out.println( "return value : " + ret );
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
    
