// JSFile.java

package ed.js;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import ed.db.*;
import ed.io.*;

public abstract class JSFile extends JSObjectBase {

    private static final int DEF_CHUNK_SIZE = 1024 * 256;

    protected JSFile(){
        set( "_ns" , "_files" );
    }
    
    protected JSFile( String filename , String contentType , long length ){
        this( null , filename , contentType , length );
    }

    protected JSFile( ObjectId id , String filename , String contentType , long length ){
        this();
        if ( id != null )
            set( "_id" , id );
        
        set( "filename" , filename );
        set( "contentType" , contentType );
        set( "length" , length );
    }
    
    public JSFileChunk getFirstChunk(){
        ((JSObject)get( "next" )).keySet();
        JSFileChunk chunk = (JSFileChunk)get( "next" );        
        if ( chunk == null )
            throw new NullPointerException( "first chunk is null :(" );
        return chunk;
    }

    public void write( OutputStream out )
        throws IOException {
        
        JSFileChunk chunk = getFirstChunk();
        while ( chunk != null ){
            chunk.getData().write( out );
            chunk = chunk.getNext();
        }
    }

    public String getFileName(){
        return getJavaString( "filename" );
    }
    
    public String getContentType(){
        return getJavaString( "contentType" );
    }

    public long getLength(){
        return ((Number)get( "length" )).longValue();
    }
    
    public int numChunks(){
        return (int)Math.ceil( (double)getLength() / getChunkSize() );
    }

    public int getChunkSize(){
        Object foo = get( "chunkSize" );
        if ( foo == null ){
            set( "chunkSize" , DEF_CHUNK_SIZE );
            return DEF_CHUNK_SIZE;
        }
        return ((Number)foo).intValue();
    }
       

    public String asString(){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            sender().write( new WritableByteChannelConnector( bout ) );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
        return new String( bout.toByteArray() );
    }
    
    public String toString(){
        return getFileName();
    }

    public Sender sender(){
        return new Sender( getFirstChunk() );
    }        
    
    public static class Sender {
        
        Sender( JSFileChunk chunk ){
            if ( chunk == null )
                throw new NullPointerException("chunk can't be null" );
            if ( chunk.getData() == null )
                throw new NullPointerException("chunk data can't be null" );
            _chunk = chunk;
            _buf = _chunk.getData().asByteBuffer();
        }

        /**
         * @return true if we're all done
         */
        public boolean write( WritableByteChannel out )
            throws IOException {
            
            if ( _chunk == null )
                return true;
            
            if ( _buf.remaining() == 0 ){
                _buf = null;
                _chunk = _chunk.getNext();
                
                if ( _chunk == null )
                    return true;
                
                _buf = _chunk.getData().asByteBuffer();
            }
            

            if ( _maxPostion > 0 ){
                long bytesLeft = _maxPostion - _bytesWritten;
                if ( ( _buf.limit() - _buf.position() ) > bytesLeft )
                    _buf.limit( _buf.position() + (int)bytesLeft );
            }

            _bytesWritten += out.write( _buf );
            return false;
        }

        public void skip( final long num )
            throws IOException {
            write( new WritableByteChannel(){
                    
                    public int write ( ByteBuffer src )
                        throws IOException {
                        for ( long i=0; i<num; i++ )
                            src.get();
                        return (int)num;
                    }
                    
                    public void close(){}
                    public boolean isOpen(){ return true; }

                } );
        }

        public void maxPosition( long max ){
            _maxPostion = max;
        }
        
        JSFileChunk _chunk;
        ByteBuffer _buf;
        long _bytesWritten = 0;
        long _maxPostion = -1;
    }
}




