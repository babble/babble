// JSNewFile.java

package ed.js;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

import ed.db.*;

public abstract class JSNewFile extends JSFile {

    JSNewFile( String filename , String contentType , long length ){
        super( filename , contentType , length );
        _me = this;

        final int nc = numChunks();
        System.out.println( "nc: " + nc );

        for ( int i=0; i<nc; i++ ){
            JSFileChunk c = newChunk( i );
            c.set( "_id" , ObjectId.get() );
            if ( i == 0 )
                set( "next" , c );
            
            _chunks.add( c );
            if ( i > 0 )
                _chunks.get( i - 1 ).set( "next" , c );

        }
    }

    
    protected abstract JSFileChunk newChunk( int num );

    public ObjectId getChunkID( int num ){
        return (ObjectId)(_chunks.get( num ).get( "_id" ));
    }

    public JSFileChunk getChunk( int num ){
        return _chunks.get( num );
    }

    final List<JSFileChunk> _chunks = new ArrayList<JSFileChunk>();
    final JSFile _me;

    // ----

    public static class Local extends JSNewFile {
        
        Local( String s ){
            this( new File( s ) );
        }
        
        Local( File f ){
            super( f.getName() , ed.appserver.MimeTypes.get( f ) , f.length() );
            _file = f;
        }
        
        protected JSFileChunk newChunk( int i ){
            return new MyChunk( i );
        }
        
        class MyChunk extends JSFileChunk {
            MyChunk( int num ){
                super( _me , num );
                _num = num;
            }
            
            protected JSBinaryData getData(){
                final long start = _num * CHUNK_SIZE;
                final long end = Math.min( _file.length() , ( _num + 1 ) * CHUNK_SIZE );
                
                return new JSBinaryData(){

                    public int length(){
                        return (int)( end - start );
                    }
                    
                    public void put( ByteBuffer buf ){
                        
                        try {
                            if ( _fc == null )
                                _fc = (new FileInputStream( _file )).getChannel();
                            
                            final int oldLimit = buf.limit();
                            buf.limit( buf.position() + CHUNK_SIZE );
                            
                            _fc.read( buf , _num * CHUNK_SIZE );
                            buf.limit( oldLimit );
                        }
                        catch ( IOException ioe ){
                            throw new RuntimeException( "can't read file " + _file , ioe );
                        }
                    }
                    
                    public void write( OutputStream out ) 
                        throws IOException {
                        throw new RuntimeException( "not implemented" );
                    }
                    
                    public ByteBuffer asByteBuffer(){
                        throw new RuntimeException( "not implemented" );
                    }

                };
            }
            
            final int _num;
        }
        
        final File _file;
        int _curChunk = 0;
        FileChannel _fc;
    }
}
