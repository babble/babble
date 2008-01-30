// ByteDecoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;
import ed.js.engine.*;
import ed.util.*;

public class ByteDecoder extends Bytes {

    static protected ByteDecoder get( DBBase base , String ns , JSFunction cons ){
        ByteDecoder bd = _pool.get();
        bd.reset();
        bd._base = base;
        bd._ns = ns;
        bd._constructor = cons;
        return bd;
    }

    protected void done(){
        _pool.done( this );
    }

    private final static SimplePool<ByteDecoder> _pool = new SimplePool<ByteDecoder>( "ByteDecoders" , 10 , -1 ){
        protected ByteDecoder createNew(){
            return new ByteDecoder();
        }
    };

    // ---
    
    protected ByteDecoder( ByteBuffer buf ){
        _buf = buf;
        if ( _buf.order() != ByteOrder.LITTLE_ENDIAN )
            throw new RuntimeException( "this is not correct" );
    }

    private ByteDecoder(){
        _buf = ByteBuffer.allocateDirect( BUF_SIZE );
        reset();
    }

    void reset(){
        _buf.position( 0 );
        _buf.limit( _buf.capacity() );
        _buf.order( ByteOrder.LITTLE_ENDIAN );        
    }

    protected JSObject readObject(){
        if ( _buf.position() >= _buf.limit() )
            return null;

        final int start = _buf.position();
        final int len = _buf.getInt();
        
        JSObjectBase created = null;

        if ( _ns != null ){
            if ( _ns.endsWith( "._files" ) ){
                created = new JSDBFile();
            }
            else if ( _ns.endsWith( "._chunks" ) ){
                created = new JSFileChunk();
            }
        }

        if ( created == null ){
            created = new JSObjectBase();
            if ( _constructor != null ){
                created.setConstructor( _constructor , true );
            }
        }
        
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
            o.set( name , new ObjectId( _buf.getLong() , _buf.getInt() ) );
            break;
            
        case REF:
            int stringSize = _buf.getInt();
            String ns = readCStr();
            ObjectId theOID = new ObjectId( _buf.getLong() , _buf.getInt() );
            o.set( name , new DBRef( o , name , _base , ns , theOID ) );
            break;
            
        case DATE:
            o.set( name , new JSDate( _buf.getLong() ) );
            break;
            
        case REGEX:
            o.set( name , new JSRegex( readCStr() , readCStr() ) );
            break;

        case BINARY:
            o.set( name , parseBinary() );
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

    Object parseBinary(){
        final int totalLen = _buf.getInt();
        final byte bType = _buf.get();

        switch ( bType ){
        case B_BINARY:
            final int len = _buf.getInt();
            final byte[] data = new byte[len];
            _buf.get( data );
            return new JSBinaryData.ByteArray( data );
        }
     
        throw new RuntimeException( "can't handle binary type : " + bType );
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
    private final byte _namebuf[] = new byte[ MAX_STRING ];

    final ByteBuffer _buf;

    String _ns;
    DBBase _base;
    JSFunction _constructor;
}

