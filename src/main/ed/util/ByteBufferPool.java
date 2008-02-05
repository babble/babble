// ByteBufferPool.java

package ed.util;

import java.nio.*;

public class ByteBufferPool extends SimplePool<ByteBuffer> {

    public ByteBufferPool( int maxToKeep , int size ){
        this( maxToKeep , size , null );
    }

    public ByteBufferPool( int maxToKeep , int size , ByteOrder order ){
        super( "ByteBufferPool" , maxToKeep , -1  );
        _size = size;
        _order = order;
    }
    
    public ByteBuffer createNew(){
        ByteBuffer bb = ByteBuffer.allocateDirect( _size );
        if ( _order != null )
            bb.order( _order );
        return bb;
    }
    
    public boolean ok( ByteBuffer buf ){
        buf.position( 0 );
        buf.limit( buf.capacity() );
        return true;
    }    

    final int _size;
    final ByteOrder _order;
}
