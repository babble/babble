// ByteEncoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

public class ByteEncoder extends Bytes {

    protected ByteEncoder(){
        
    }
    
    protected int putNumber( ByteBuffer buf , String name , Number n ){
        int start = buf.position();
        _put( buf , NUMBER , name );
        buf.putDouble( n.doubleValue() );
        return buf.position() - start;
    }

    protected int putString( ByteBuffer buf , String name , String s ){
        int start = buf.position();
        _put( buf , STRING , name );
        
        int lenPos = buf.position();
        buf.putInt( 0 ); // making space for size
        int strLen = _put( buf , s );
        buf.putInt( lenPos , strLen );
        
        return buf.position() - start;
    }

    protected int putObjectId( ByteBuffer buf , String name , ObjectId oid ){
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
    
    private int _put( ByteBuffer buf , String name ){

        _cbuf.position( 0 );
        _cbuf.limit( _cbuf.capacity() );
        _cbuf.append( name );
        
        _cbuf.flip();
        final int start = buf.position();
        _encoder.encode( _cbuf , buf , false );

        buf.put( (byte)0 );

        return buf.position() - start;
    }
    
    private CharBuffer _cbuf = CharBuffer.allocate( 1024 );
    private CharsetEncoder _encoder = _utf8.newEncoder();

}
