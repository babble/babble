// ByteDecoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

public class ByteDecoder extends Bytes {

    protected ByteDecoder(){
        
    }

    protected JSObject readObject( ByteBuffer buf ){
        buf.order( ByteOrder.LITTLE_ENDIAN );

        final int start = buf.position();
        final int len = buf.getInt();
        
        JSObjectBase created = new JSObjectBase();
        while ( decodeNext( buf , created ) > 1 );
        
        if ( buf.position() - start != len )
            throw new RuntimeException( "lengths don't match" );
        
        return created;
    }

    protected int decodeNext( ByteBuffer buf , JSObject o ){
        buf.order( ByteOrder.LITTLE_ENDIAN );
        final int start = buf.position();
        final byte type = buf.get();
        
        if ( type == EOO )
            return 1;
        
        String name = readName( buf );

        JSObject created = null;

        switch ( type ){
        case NULL:
            o.set( name , null );
            break;

        case BOOLEAN:
            o.set( name , buf.get() > 0 );
            break;

        case NUMBER:
            double val = buf.getDouble();
            o.set( name , val );
            break;

        case STRING:
            int size = buf.getInt() - 1;
            buf.get( _namebuf , 0 , size );
            o.set( name , new JSString( new String( _namebuf , 0 , size ) ) );
            buf.get();
            break;

        case OID:
            o.set( name , new ObjectId( buf.getLong() , buf.getInt() ) );
            break;

        case DATE:
            o.set( name , new JSDate( buf.getLong() ) );
            break;
            
        case ARRAY:
            if ( created == null )
                created = new JSArray();
        case OBJECT:
            int embeddedSize = buf.getInt();
            if ( created == null )
                created = new JSObjectBase();
            while ( decodeNext( buf , created ) > 1 );
            o.set( name , created );
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
