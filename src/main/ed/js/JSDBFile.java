// JSDBFile.java

package ed.js;

import java.io.*;

public class JSDBFile extends JSFile {

    public JSDBFile(){
        
    }

    public JSFileChunk getChunk( int num ){
        throw new RuntimeException( "not done uet" );
    }

    public void debug(){
        
        System.out.println( "--- START" );
        System.out.println( toString() );
        System.out.println( "-" );
        for ( String n : keySet() ){
            System.out.println( "\t " + n + " : " + get( n ) );
        }
        System.out.println( "-----" );
        try {
            write( System.out );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( ioe );
        }
        System.out.println( "--- END" );

    }
}
