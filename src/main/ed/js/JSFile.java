// JSFile.java

package ed.js;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public abstract class JSFile {

    public static final int CHUNK_SIZE = 1024; // * 512;  making 1k for testing, will make 1 meg
    
    protected JSFile( String filename , String contentType , long length ){
        _filename = filename;
        _contentType = contentType;
        _length = length;
    }
    
    /**
     * will throw an exception if there is not enough room
     */
    public abstract void fillIn( int chunkNumber , ByteBuffer buf )
        throws IOException ;

    public String getFileName(){
        return _filename;
    }
    
    public String getContentType(){
        return _contentType;
    }

    public long getLength(){
        return _length;
    }

    public int numChunks(){
        return (int)Math.ceil( _length / CHUNK_SIZE );
    }

    public String toString(){
        return "{ JSFile.  filename:" + _filename + " contentType:" + _contentType + " length:" + _length + " }";
    }
    
    protected final String _filename;
    protected final String _contentType;
    protected final long _length;
    
    public static class Local extends JSFile {

        Local( String s ){
            this( new File( s ) );
        }
        
        Local( File f ){
            super( f.getName() , ed.appserver.MimeTypes.get( f ) , f.length() );
            _file = f;
        }
        
        public void fillIn( int chunkNumber , ByteBuffer buf )
            throws IOException {
            if ( _fc == null )
                _fc = (new FileInputStream( _file )).getChannel();
            

            final int oldLimit = buf.limit();
            buf.limit( buf.position() + CHUNK_SIZE );
            
            _fc.read( buf , chunkNumber * CHUNK_SIZE );
            buf.limit( oldLimit );
        }
        
        final File _file;
        int _curChunk = 0;
        FileChannel _fc;
    }
}
