// ByteEncoder.java

package ed.db;

import java.util.*;
import java.nio.*;
import java.nio.charset.*;

import ed.js.*;
import ed.js.engine.*;
import ed.util.*;

public class ByteEncoder extends Bytes {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.BE" );

    // things that won't get sent in the scope
    static final Set<String> BAD_GLOBALS = new HashSet<String>(); 
    static {
	BAD_GLOBALS.add( "db" );
	BAD_GLOBALS.add( "local" );
	BAD_GLOBALS.add( "core" );
        BAD_GLOBALS.add( "args" ); // TODO: should we get rid of this
        BAD_GLOBALS.add( "obj" ); // TODO: get rid of this
    }
    
    
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
        _buf.order( Bytes.ORDER );        
    }

    // ----
    
    private ByteEncoder(){
        _buf = ByteBuffer.allocateDirect( BUF_SIZE );
        _buf.order( Bytes.ORDER );
    }

    protected void reset(){
        _buf.position( 0 );
        _buf.limit( _buf.capacity() );
        _flipped = false;
	_dontRef.clear();
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
        if ( o == null )
            throw new NullPointerException( "can't save a null object" );

        if ( DEBUG ) System.out.println( "putObject : " + name + " [" + o.getClass() + "]" + " # keys " + o.keySet( false ).size() );
        
        if ( _flipped )
            throw new RuntimeException( "already flipped" );
        final int start = _buf.position();
        
        byte myType = OBJECT;
        if ( o instanceof JSArray || o instanceof List )
            myType = ARRAY;

        if ( _handleSpecialObjects( name , o ) )
            return _buf.position() - start;
        
        if ( name != null ){
            _put( myType , name );
        }

        final int sizePos = _buf.position();
        _buf.putInt( 0 ); // leaving space for this.  set it at the end

	boolean skipId = true;
        Object possibleId = o.get( "_id" );
        if ( possibleId != null ){
            if ( possibleId instanceof ObjectId )
		putObjectId( "_id" , (ObjectId)possibleId );
            else if ( possibleId instanceof String || possibleId instanceof JSString )
                putObjectId( "_id" , new ObjectId( possibleId.toString() ) );
            else
                skipId = false;
        }
            
        JSArray transientFields = null;
        {
            Object temp = o.get( "_transientFields" );
            if ( temp instanceof JSArray )
                transientFields = (JSArray)temp;
        }
        

        for ( String s : o.keySet( false ) ){

            if ( skipId && s.equals( "_id" ) )
                continue;
            
            if ( transientFields != null && transientFields.contains( s ) )
                continue;

            _putObjectField( s , o.get( s ) );

        }
        _buf.put( EOO );
        
        _buf.putInt( sizePos , _buf.position() - sizePos );
        return _buf.position() - start;
    }

    private void _putObjectField( String name , Object val ){

        if ( dbOnlyField( name ) || name.equals( "_transientFields" ) )
            return;
        
        if ( DEBUG ) System.out.println( "\t put thing : " + name );
        
        if ( name.equals( "$where") && ( val instanceof String || val instanceof JSString ) ){
            _put( CODE , name );
            _putValueString( val.toString() );
            return;
        }
        
        if ( val instanceof JSFunction ){
            JSFunction func = (JSFunction)val;
            if ( func.isCallable() ){
                if ( name.startsWith( "$" ) && func.getSourceCode() != null )
                    putFunction( name , func );
                return;
            }
        }
        
        
        if ( val == null )
            putNull(name);
        else if ( val instanceof Number )
            putNumber(name, (Number)val );
        else if ( val instanceof JSString.Symbol )
            putSymbol(name, val.toString() );
        else if ( val instanceof String || val instanceof JSString )
            putString(name, val.toString() );
        else if ( val instanceof ObjectId )
            putObjectId(name, (ObjectId)val );
        else if ( val instanceof JSObject )
            putObject(name, (JSObject)val );
        else if ( val instanceof Boolean )
            putBoolean(name, (Boolean)val );
        else if ( val instanceof JSBinaryData )
            putBinary(name, (JSBinaryData)val );
        else if ( val instanceof Map )
            putMap( name , (Map)val );
        else 
            throw new RuntimeException( "can't serialize " + val.getClass() );
        
    }
    
    private void putMap( String name , Map m ){
        _put( OBJECT , name );
        final int sizePos = _buf.position();
        _buf.putInt( 0 );
        
        for ( Object key : m.keySet() )
            _putObjectField( key.toString() , m.get( key ) );

        _buf.put( EOO );
        _buf.putInt( sizePos , _buf.position() - sizePos );
    }
    

    private boolean _handleSpecialObjects( String name , JSObject o ){
        
        if ( o == null )
            return false;

        if ( o instanceof JSDate ){
            _put( DATE , name );
            _buf.putLong( ((JSDate)o).getTime() );
            return true;
        }

        if ( o instanceof DBCollection ){
            DBCollection c = (DBCollection)o;
            putDBRef( name , c.getName() , Bytes.COLLECTION_REF_ID );
            return true;
        }

        if ( o instanceof JSRegex ){
            JSRegex r = (JSRegex)o;
            _put( REGEX , name );
            _put( r.getPattern() );
            _put( r.getFlags() );
            return true;
        }
        
        if ( ! _dontRefContains( o ) && name != null && o instanceof DBRef ){
            DBRef r = (DBRef)o;
            putDBRef( name , r._ns , r._id );
            return true;
        }
        
        if ( o.get( Bytes.NO_REF_HACK ) != null ){
            o.removeField( Bytes.NO_REF_HACK );
            return false;
        }

        if ( ! _dontRefContains( o ) && 
	     name != null && 
             cameFromDB( o ) ){
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
	if ( n instanceof Integer ){
	    _put( NUMBER_INT , name );
	    _buf.putInt( n.intValue() );
	}
	else {
	    _put( NUMBER , name );
	    _buf.putDouble( n.doubleValue() );
	}
        return _buf.position() - start;
    }

    protected int putFunction( String name , JSFunction func ){
        if ( D ) System.out.println( "putFunction [" + name + "]" );
        final int start = _buf.position();
        
        Set<String> globalsToSend = new HashSet<String>();
        {
            JSArray globals = func.getGlobals();
            if ( globals != null ){
                for ( Object var : globals ){
                    if ( BAD_GLOBALS.contains( var.toString() ) )
                        continue;
                    globalsToSend.add( var.toString() );
                }
            }
        }

        if ( D ) System.out.println( "globalsToSend : " + globalsToSend );

	if ( name.startsWith( "$" ) && globalsToSend.size() > 0 && func.getScope() != null ){
	    _put( CODE_W_SCOPE , name );
	    final int save = _buf.position();
	    _buf.putInt( 0 );
	    _putValueString( func.getSourceCode() );
	    
	    JSObjectBase scopeToPass = new JSObjectBase();
	    Scope s = func.getScope();

            IdentitySet dontRef = new IdentitySet();
            _dontRef.push( dontRef );

	    for ( String var : globalsToSend ){
		Object val = s.get( var );
		dontRef.add( val );
		scopeToPass.set( var , val );
	    }
	    
	    putObject( null , scopeToPass );
	    
            _dontRef.pop();
	    
	    _buf.putInt( save , _buf.position() - save );
	}
	else {
	    _put( CODE , name );
	    _putValueString( func.getSourceCode() );
	}
	
	
        return _buf.position() - start;
    }
    
    protected int putSymbol( String name , String s ){
        int start = _buf.position();
        _put( SYMBOL , name );
        _putValueString( s );
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

    boolean _dontRefContains( Object o ){
        if ( _dontRef.size() == 0 )
            return false;
        return _dontRef.peek().contains( o );
    }
    
    private final CharBuffer _cbuf = CharBuffer.allocate( MAX_STRING );
    private final CharsetEncoder _encoder = _utf8.newEncoder();
    private Stack<IdentitySet> _dontRef = new Stack<IdentitySet>();
    
    private boolean _flipped = false;
    final ByteBuffer _buf;
}
