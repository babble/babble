// UploadFile.java

package ed.net.httpserver;

import java.io.*;
import java.nio.*;

import ed.js.*;

public class UploadFile extends JSNewFile  {

    UploadFile( String filename , String contentType , PostData pd , int start , int end ){
        super( filename , contentType , end - start );
        _data = pd;
        _start = start;
        _end = end;
    }

    public String string(){
        return _data.string( _start , _end - _start );
    }

    protected JSFileChunk newChunk( int i ){
        return new MyChunk( i );
    }

    class MyChunk extends JSFileChunk {
        MyChunk( int num ){
            super( UploadFile.this , num );
            _num = num;
        }

        protected JSBinaryData getData(){

            final int start = _start + ( _num * CHUNK_SIZE );
            final int end = Math.min( _end , start + CHUNK_SIZE );
            
            return new JSBinaryData(){

                public int length(){
                    return end - start;
                }
                
                public void put( ByteBuffer buf ){
                    _data.fillIn( buf , start , end );
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


    final PostData _data;
    final int _start;
    final int _end;
        
}
