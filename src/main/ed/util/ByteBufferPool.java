// ByteBufferPool.java

package ed.util;

import java.nio.*;

public class ByteBufferPool extends SimplePool<ByteBuffer> {

    public ByteBufferPool( int maxToKeep , int size ){
        super( "ByteBufferPool" , maxToKeep , -1  );
        _size = size;
    }

    public ByteBuffer createNew(){
        System.out.println( "creating new buffer" );
        return ByteBuffer.allocateDirect( _size );
    }

    public boolean ok( ByteBuffer buf ){
        buf.position( 0 );
        return true;
    }    

    final int _size;
}
