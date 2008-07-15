// ByteBufferPool.java

package ed.util;

import java.nio.*;

/** @expose */
public class ByteBufferPool extends SimplePool<ByteBuffer> {

    /** Initializes this pool with a given number of byte buffers to keep and size.
     * @param maxToKeep The number of byte buffers allowed
     * @param size The size for buffers this pool creates, in bytes
     */
    public ByteBufferPool( int maxToKeep , int size ){
        this( maxToKeep , size , null );
    }

    /** Initializes this pool with a given number of byte buffers, size, and ordering.
     * @param maxToKeep The number of byte buffers allowed
     * @param size The size for buffers this pool creates, in bytes
     * @param order The ordering of the buffers (big or little endian)
     */
    public ByteBufferPool( int maxToKeep , int size , ByteOrder order ){
        super( "ByteBufferPool" , maxToKeep , -1  );
        _size = size;
        _order = order;
    }

    /** Creates a new buffer with this pool's standard size and order.
     * @return The new buffer.
     */
    public ByteBuffer createNew(){
        ByteBuffer bb = ByteBuffer.allocateDirect( _size );
        if ( _order != null )
            bb.order( _order );
        return bb;
    }

    /** Sets the size of a given buffer to its capacity and resets the buffer's mark's position to 0.
     * @param buf Buffer to reset.
     * @return true
     */
    public boolean ok( ByteBuffer buf ){
        buf.position( 0 );
        buf.limit( buf.capacity() );
        return true;
    }

    /** @unexpose */
    final int _size;
    /** @unexpose */
    final ByteOrder _order;
}
