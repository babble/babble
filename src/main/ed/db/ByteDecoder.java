// ByteDecoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

public class ByteDecoder extends Bytes {

    protected ByteDecoder(){
        
    }

    protected int decodeNext( ByteBuffer buf , JSObject o ){
        final int start = buf.position();
        final byte type = buf.get();


        String name = readName( buf );
        
        switch ( type ){
        case NUMBER:
            double val = buf.getDouble();
            o.set( name , val );
            break;

        case STRING:
            int size = buf.getInt() - 1;
            System.out.println( "need to read:" + size );
            buf.get( _namebuf , 0 , size );
            o.set( name , new JSString( new String( _namebuf , 0 , size ) ) );
            buf.get();
            break;

        default:
            throw new RuntimeException( "can't handle : " + type );
        }
        
        return buf.position() - start;
    }

    private String readName( ByteBuffer buf ){
        int pos = 0;
        while ( true ){
            byte b = buf.get();
            if ( b == 0 )
                break;
            _namebuf[pos++] = b;
        }
        return new String( _namebuf , 0 , pos );
    }

    private CharBuffer _cbuf = CharBuffer.allocate( 1024 );
    private CharsetDecoder _decoder = _utf8.newDecoder();
    private byte _namebuf[] = new byte[1024];
}
