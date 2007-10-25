// ByteEncoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

public class ByteEncoder extends Bytes {

    protected ByteEncoder(){
        
    }
    
    protected int put( ByteBuffer buf , String name , Number n ){
        int start = buf.position();
        _put( buf , NUMBER , name );
        buf.putDouble( n.doubleValue() );
        return buf.position() - start;
    }

    protected int put( ByteBuffer buf , String name , String s ){
        int start = buf.position();
        _put( buf , NUMBER , name );
        _put( buf , s );
        return buf.position() - start;
    }

    protected int put( ByteBuffer buf , String name , ObjectId oid ){
        int start = buf.position();
        _put( buf , OID , name );
        buf.putLong( oid._base );
        buf.putInt( oid._inc );
        return buf.position() - start;
    }


    // ----------------------------------------------
    
    private void _put( ByteBuffer buf , byte type , String name ){
        buf.put( type );
        _put( buf , name );
    }
    
    private void _put( ByteBuffer buf , String name ){

        _cbuf.position( 0 );
        _cbuf.limit( _cbuf.capacity() );
        _cbuf.append( name );
        
        _cbuf.flip();
        _encoder.encode( _cbuf , buf , false );

    }
    
    private CharBuffer _cbuf = CharBuffer.allocate( 1024 );
    private CharsetEncoder _encoder = _utf8.newEncoder();

}
