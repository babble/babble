// JS.java

package ed.js;

import java.io.*;

import ed.js.engine.*;

public class JS {
    
    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );

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
    
