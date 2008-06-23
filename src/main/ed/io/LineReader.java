// LineReader.java

package ed.io;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class LineReader implements Iterable<String> , Iterator<String> {
    
    public LineReader( String filename )
        throws IOException {
        this( new File( filename ) );
    }

    public LineReader( File f )
        throws IOException {
        this( _open( f ) );
    }

    public LineReader( InputStream in ){
        this( new InputStreamReader( in ) );
    }
    
    public LineReader( Reader r ){
        this( new BufferedReader( r ) );
    }

    public LineReader( BufferedReader in ){
        _in = in;
    }

    public String next(){
        if ( _next != null ){
            String s = _next;
            _next = null;
            return s;
        }
        try {
            return _in.readLine();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't read" , ioe );
        }
    }

    public boolean hasNext(){
        if ( _next != null )
            return true;
            
        try {
            _next = _in.readLine();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't read" , ioe );
        }            
            
        return _next != null;
    }

    public void remove(){
        throw new RuntimeException( "you are stupid" );
    }

    public Iterator<String> iterator(){
        return this;
    }

    private static InputStream _open( File f )
        throws IOException {
        InputStream in = new FileInputStream( f );
        
        if ( f.getName().endsWith( ".gz" ) )
            in = new GZIPInputStream( in );

        return in;
    }

    final BufferedReader _in;
        
    private String _next = null;
}
