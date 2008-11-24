// ByteDecoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.lang.*;
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
	_constructor = null;
        _pool.done( this );
    }

    private final static int _poolSize = 6 * BUFS_PER_50M;
    private final static SimplePool<ByteDecoder> _pool = new SimplePool<ByteDecoder>( "ByteDecoders" , _poolSize , -1 ){
        protected ByteDecoder createNew(){
	    if ( D ) System.out.println( "creating new ByteDecoder" );
            return new ByteDecoder();
        }
    };

    // ---
    
    public ByteDecoder( ByteBuffer buf ){
        reset( buf );
        _private = false;
    }

    private ByteDecoder(){
        _buf = ByteBuffer.allocateDirect( BUF_SIZE );
        _private = true;
        reset();
    }

    public void reset( ByteBuffer buf ){
        if ( _private )
            throw new RuntimeException( "can't reset private ByteDecoder" );

        _buf = buf;
        if ( _buf.order() != Bytes.ORDER )
            throw new RuntimeException( "this is not correct" );
    }

    void reset(){
        _buf.position( 0 );
        _buf.limit( _buf.capacity() );
        _buf.order( Bytes.ORDER );        
    }

    public JSObject readObject(){
        if ( _buf.position() >= _buf.limit() )
            return null;

        final int start = _buf.position();
        final int len = _buf.getInt();
        
        JSObject created = null;

        if ( _ns != null ){
            if ( _ns.endsWith( "._files" ) ){
                created = new JSDBFile( _base );
            }
            else if ( _ns.endsWith( "._chunks" ) ){
                created = new JSFileChunk();
            }
        }

        if ( created == null )
            created = _create( _constructor );
        
        while ( decodeNext( created , null , _constructor ) > 1 );
        
        if ( _buf.position() - start != len )
            throw new RuntimeException( "lengths don't match " + (_buf.position() - start) + " != " + len );

        return created;
    }

    private JSObject _create( JSFunction cons ){
        if ( cons == null )
            return new JSDict();
        
        JSObject o = cons.newOne();
        if ( o instanceof JSObjectBase )
            ((JSObjectBase)o).setConstructor( cons , true );
        return o;
    }

    protected int decodeNext( JSObject o ){
        return decodeNext( o , null , null );
    }

    
    protected int decodeNext( JSObject o , JSFunction embedCons , JSFunction myCons ){
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
	    
	case NUMBER_INT:
	    o.set( name , _buf.getInt() );
	    break;
	    
        case SYMBOL:
        case STRING:
            int size = _buf.getInt() - 1;
            _buf.get( _namebuf , 0 , size );
            try {
                String raw = new String( _namebuf , 0 , size , "UTF-8" );
                if ( type == SYMBOL )
                    o.set( name , new JSString.Symbol( raw ) );
                else
                    o.set( name , new JSString( raw ) );
            }
            catch ( java.io.UnsupportedEncodingException uee ){
                throw new RuntimeException( "impossible" , uee );
            }
            _buf.get();
            break;

        case OID:
            o.set( name , new ObjectId( _buf.getLong() , _buf.getInt() ) );
            break;
            
        case REF:
            int stringSize = _buf.getInt();
            String ns = readCStr();
            ObjectId theOID = new ObjectId( _buf.getLong() , _buf.getInt() );
            if ( theOID.equals( Bytes.COLLECTION_REF_ID ) )
                o.set( name , _base.getCollectionFromFull( ns ) );
            else 
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
            
        case CODE:
            // TODO: clean this up
            try {
                _buf.getInt(); // this is value string, not a cstr
                final String theCode = readCStr();
                final JSFunction theFunc = Convert.makeAnon( theCode );
                o.set( name , theFunc );
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
            break;

        case ARRAY:
            if ( created == null )
                created = new JSArray();
        case OBJECT:
            int embeddedSize = _buf.getInt();

            if ( created == null )
                created = _create( embedCons );
            
            JSFunction nextEmebedCons = null;
            
            if ( o.get( name ) != null ){
                created = (JSObject)o.get( name );
                Object maybe = created.get( "_dbCons" );

                if ( maybe instanceof JSFunction )
                    nextEmebedCons = (JSFunction)maybe;
                
                if ( embedCons == null )
                    embedCons = created.getConstructor();
                
            }
            
            if ( myCons != null && nextEmebedCons == null ){
                Object holder = myCons.get( "_dbCons" );
                if ( holder instanceof JSObject ){
                    Object maybe = ((JSObject)holder).get( name );
                    if ( maybe instanceof JSFunction )
                        nextEmebedCons = (JSFunction)maybe;
                }
            }
            
            while ( decodeNext( created , nextEmebedCons , embedCons ) > 1 );
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
	    if ( D ) System.out.println( "got binary of size : " + len );
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

    long remaining(){
        return _buf.remaining();
    }

    void doneReading( int len ){
        _buf.position( len );
        _buf.flip();
    }

    private final CharsetDecoder _decoder = _utf8.newDecoder();
    private final byte _namebuf[] = new byte[ MAX_STRING ];

    ByteBuffer _buf;
    private final boolean _private;

    String _ns;
    DBBase _base;
    private JSFunction _constructor;
}

