// ByteEncoder.java

package ed.db;

import java.nio.*;
import java.nio.charset.*;

import ed.js.*;
import ed.util.*;

public class ByteEncoder extends Bytes {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.BE" );
    
    public static boolean dbOnlyField( Object o ){
        if ( o == null )
            return false;
        
        if ( o instanceof String || o instanceof JSString )
            return dbOnlyField( o.toString() );
        
        return false;
    }

    public static boolean dbOnlyField( String s ){
        return 
            s.equals( "_ns" )  
            || s.equals( "_save" )
            || s.equals( "_update" );
    }

    static ByteEncoder get(){
        return _pool.get();
    }

    protected void done(){
        reset();
        _pool.done( this );
    }
    
    private final static int _poolSize = Math.min( Bytes.CONNECTIONS_PER_HOST , 2 * BUFS_PER_50M );
    private final static SimplePool<ByteEncoder> _pool = new SimplePool( "ByteEncoders" , -1 , _poolSize  ){
            protected ByteEncoder createNew(){
		if ( D ) System.out.println( "creating new ByteEncoder" );
                return new ByteEncoder();
            }
        };

    public ByteEncoder( ByteBuffer buf ){
        _buf = buf;
        _buf.order( ByteOrder.LITTLE_ENDIAN );        
    }

    // ----
    
    private ByteEncoder(){
        _buf = ByteBuffer.allocateDirect( BUF_SIZE );
        _buf.order( ByteOrder.LITTLE_ENDIAN );
    }

    protected void reset(){
        _buf.position( 0 );
        _buf.limit( _buf.capacity() );
        _flipped = false;
    }

    protected void flip(){
        _buf.flip();
        _flipped = true;
    }
    
    /**
     * this is for the higher level api calls
     */
    public int putObject( JSObject o ){
        try {
            return putObject( null , o );
        }
        catch ( BufferOverflowException bof ){
            reset();
            throw new RuntimeException( "tried to save too large of an object.  max size : " + ( _buf.capacity() / 2  ) );
        }
    }

    /**
     * this is really for embedded objects
     */
    private int putObject( String name , JSObject o ){
        if ( DEBUG ) System.out.println( "putObject : " + name + " [" + o.getClass() + "]" + " # keys " + o.keySet().size() );
            
        if ( _flipped )
            throw new RuntimeException( "already flipped" );
        final int start = _buf.position();
        
        byte myType = OBJECT;
        if ( o instanceof JSArray )
            myType = ARRAY;

        if ( _handleSpecialObjects( name , o ) )
            return _buf.position() - start;
        
        if ( name != null ){
            _put( myType , name );
        }

        final int sizePos = _buf.position();
        _buf.putInt( 0 ); // leaving space for this.  set it at the end

        Object possibleId = o.get( "_id" );
        if ( possibleId != null ){
            if ( ! ( possibleId instanceof ObjectId ) )
                throw new RuntimeException( "_id is not an ObjectId is a " + possibleId.getClass() );
            putObjectId( "_id" , (ObjectId)possibleId );
        }
            
        JSArray transientFields = null;
        {
            Object temp = o.get( "_transientFields" );
            if ( temp instanceof JSArray )
                transientFields = (JSArray)temp;
        }
        

        for ( String s : o.keySet() ){
            
            if ( dbOnlyField( s ) 
                 || s.equals( "_transientFields" ) 
                 || s.equals( "_id" ) )
                continue;
            
            if ( transientFields != null && transientFields.contains( s ) )
                continue;

            if ( DEBUG ) System.out.println( "\t put thing : " + s );

            Object val = o.get( s );
            
            if ( val instanceof JSFunction ){

                if ( s.startsWith( "$" ) && 
                     ! val.toString().startsWith( JSFunction.TO_STRING_PREFIX ) ){
                    putFunction( s , (JSFunction)val );
                    continue;
                }

                continue;
            }


            if ( val == null )
                putNull( s );
            else if ( val instanceof Number )
                putNumber( s , (Number)val );
            else if ( val instanceof String || val instanceof JSString )
                putString( s , val.toString() );
            else if ( val instanceof ObjectId )
                putObjectId( s , (ObjectId)val );
            else if ( val instanceof JSObject )
                putObject( s , (JSObject)val );
            else if ( val instanceof Boolean )
                putBoolean( s , (Boolean)val );
            else if ( val instanceof JSBinaryData )
                putBinary( s , (JSBinaryData)val );
            else 
                throw new RuntimeException( "can't serialize " + val.getClass() );

        }
        _buf.put( EOO );
        
        _buf.putInt( sizePos , _buf.position() - sizePos );
        return _buf.position() - start;
    }

    private boolean _handleSpecialObjects( String name , JSObject o ){

        if ( o instanceof JSDate ){
            _put( DATE , name );
            _buf.putLong( ((JSDate)o).getTime() );
            return true;
        }

        if ( o instanceof JSRegex ){
            JSRegex r = (JSRegex)o;
            _put( REGEX , name );
            _put( r.getPattern() );
            _put( r.getFlags() );
            return true;
        }

        if ( name != null && o instanceof DBRef ){
            DBRef r = (DBRef)o;
            putDBRef( name , r._ns , r._id );
            return true;
        }
        
        if ( name != null && 
             ( o.get( "_id" ) != null && 
               o.get( "_ns" ) != null ) ){ 
            putDBRef( name , o.get( "_ns" ).toString() , (ObjectId)(o.get( "_id" ) ) );
            return true;
        }
        
        return false;
    }

    protected int putNull( String name ){
        int start = _buf.position();
        _put( NULL , name );
        return _buf.position() - start;
    }


    protected void putBinary( String name , JSBinaryData bin ){
        
        if ( bin.length() < 0 )
            throw new RuntimeException( "wtf?" );
        
        _put( BINARY , name );
        _buf.putInt( 4 + bin.length() );

        _buf.put( B_BINARY );
        _buf.putInt( bin.length() );
        int before = _buf.position();
        bin.put( _buf );
        int after = _buf.position();
        
        ed.MyAsserts.assertEquals( after - before , bin.length() );
    }

    protected int putBoolean( String name , Boolean b ){
        int start = _buf.position();
        _put( BOOLEAN , name );
        _buf.put( b ? (byte)0x1 : (byte)0x0 );
        return _buf.position() - start;
    }

    protected int putNumber( String name , Number n ){
        int start = _buf.position();
        _put( NUMBER , name );
        _buf.putDouble( n.doubleValue() );
        return _buf.position() - start;
    }

    protected int putFunction( String name , JSFunction func ){
        int start = _buf.position();
        _put( CODE , name );
        _putValueString( func.toString() );
        return _buf.position() - start;
    }


    protected int putString( String name , String s ){
        int start = _buf.position();
        _put( STRING , name );
        _putValueString( s );
        return _buf.position() - start;
    }

    protected int putObjectId( String name , ObjectId oid ){
        int start = _buf.position();
        _put( OID , name );
        _buf.putLong( oid._base );
        _buf.putInt( oid._inc );
        return _buf.position() - start;
    }
    
    protected int putDBRef( String name , String ns , ObjectId oid ){
        int start = _buf.position();
        _put( REF , name );
        
        _putValueString( ns );
        _buf.putLong( oid._base );
        _buf.putInt( oid._inc );

        return _buf.position() - start;
    }

    // ----------------------------------------------
    
    private void _put( byte type , String name ){
        _buf.put( type );
        _put( name );
    }

    void _putValueString( String s ){
        int lenPos = _buf.position();
        _buf.putInt( 0 ); // making space for size
        int strLen = _put( s );
        _buf.putInt( lenPos , strLen );
    }
    
    int _put( String name ){

        _cbuf.position( 0 );
        _cbuf.limit( _cbuf.capacity() );
        _cbuf.append( name );
        
        _cbuf.flip();
        final int start = _buf.position();
        _encoder.encode( _cbuf , _buf , false );

        _buf.put( (byte)0 );

        return _buf.position() - start;
    }
    
    private final CharBuffer _cbuf = CharBuffer.allocate( MAX_STRING );
    private final CharsetEncoder _encoder = _utf8.newEncoder();

    private boolean _flipped = false;
    final ByteBuffer _buf;
}
