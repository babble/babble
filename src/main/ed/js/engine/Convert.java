// Convert.java

package ed.js.engine;

import java.io.*;

import org.mozilla.javascript.*;

import ed.io.*;

public class Convert {
    
    String convertFunction( String s ){
        return s;
    }
    
    static String convertStatement( String s ){
        s = s.replaceAll( "\\bvar\\b" , "Object" );
        s = s.replaceAll( "\\breturn\\s*;" , "return null;" );
        return s;
    }

    public static void main( String args[] )
        throws Exception {
        
        CompilerEnvirons ce = new CompilerEnvirons();
        Parser p = new Parser( ce , ce.getErrorReporter() );
        
        String raw = StreamUtil.readFully( new java.io.FileInputStream( args[0] ) );
        ScriptOrFnNode ss = p.parse( raw , args[0] , 0 );
        
        System.out.println( ss.getFunctionCount() );
        for ( int i=0; i<ss.getFunctionCount(); i++ ){
            System.out.println( "-------- ");
            FunctionNode fn = ss.getFunctionNode( i );
            System.out.println( fn.getFunctionName() );
            
            Node next = fn.getFirstChild();
            while ( next != null ){
                System.out.println( next.toStringTree( fn) );
                next = next.getNext();
            }
        }

        System.out.println( "---------------------------" );
        Node next = ss.getFirstChild();
        while ( next != null ){
            System.out.println( next.toStringTree( ss ) );
            next = next.getNext();
        }
        
        
    }
    
}
