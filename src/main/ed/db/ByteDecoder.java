// ByteDecoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;

public class ByteDecoder extends Bytes {

    protected ByteDecoder( ByteBuffer buf ){
        _buf = buf;
    }

    protected ByteDecoder(){
        _buf = ByteBuffer.allocateDirect( 1024 * 1024 );
        _buf.order( ByteOrder.LITTLE_ENDIAN );
    }

    protected JSObject readObject(){
        final int start = _buf.position();
        final int len = _buf.getInt();
        
        JSObjectBase created = new JSObjectBase();
        while ( decodeNext( created ) > 1 );
        
        if ( _buf.position() - start != len )
            throw new RuntimeException( "lengths don't match " + (_buf.position() - start) + " != " + len );

        return created;
    }

    protected int decodeNext( JSObject o ){
        final int start = _buf.position();
        final byte type = _buf.get();

        if ( type == EOO )
            return 1;
        
        String name = readCStr();

        JSObject created = null;

        switch ( type ){
        case NULL:
            o.set( name , null );
            break;

        case BOOLEAN:
            o.set( name , _buf.get() > 0 );
            break;

        case NUMBER:
            double val = _buf.getDouble();
            o.set( name , val );
            break;

        case STRING:
            int size = _buf.getInt() - 1;
            _buf.get( _namebuf , 0 , size );
            o.set( name , new JSString( new String( _namebuf , 0 , size ) ) );
            _buf.get();
            break;

        case OID:
            ObjectId theOID = new ObjectId( _buf.getLong() , _buf.getInt() );
            if ( name.equals( "_id" ) )
                o.set( name , theOID );
            else
                o.set( name , new DBRef( theOID ) );
            break;
            
        case DATE:
            o.set( name , new JSDate( _buf.getLong() ) );
            break;
            
        case REGEX:
            o.set( name , new JSRegex( readCStr() , readCStr() ) );
            break;

        case ARRAY:
            if ( created == null )
                created = new JSArray();
        case OBJECT:
            int embeddedSize = _buf.getInt();
            if ( created == null )
                created = new JSObjectBase();
            while ( decodeNext( created ) > 1 );
            o.set( name , created );
            break;

        default:
            throw new RuntimeException( "can't handle : " + type );
        }
        
        return _buf.position() - start;
    }

    private String readCStr(){
        int pos = 0;
        while ( true ){
            byte b = _buf.get();
            if ( b == 0 )
                break;
            _namebuf[pos++] = b;
        }
        return new String( _namebuf , 0 , pos );
    }

    int getInt(){
        return _buf.getInt();
    }

    long getLong(){
        return _buf.getLong();
    }

    boolean more(){
        return _buf.position() < _buf.limit();
    }

    void doneReading( int len ){
        _buf.position( len );
        _buf.flip();
    }

    private final CharsetDecoder _decoder = _utf8.newDecoder();
    private final byte _namebuf[] = new byte[1024];

    final ByteBuffer _buf;
}
