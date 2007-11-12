// UploadFile.java

package ed.net.httpserver;

import java.nio.*;

import ed.js.*;

public class UploadFile /* extends JSNewFile */ {
    int CHUNK_SIZE = -1;
    UploadFile( String filename , String contentType , PostData pd , int start , int end ){
        //super( filename , contentType , end - start );
        _data = pd;
        _start = start;
        _end = end;
    }

    public String string(){
        return _data.string( _start , _end - _start );
    }

    /*
    public void fillIn( int chunkNumber , ByteBuffer buf ){
        final int start = _start + ( chunkNumber * CHUNK_SIZE );
        final int end = Math.min( _end , start + CHUNK_SIZE );
        _data.fillIn( buf , start , end );
    }
    */

    final PostData _data;
    final int _start;
    final int _end;
        
}
