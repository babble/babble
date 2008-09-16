// JSHook.java

package ed.db;

import java.nio.*;
import java.util.*;

import com.twmacinta.util.*;

import ed.js.*;
import ed.util.*;
import ed.js.engine.*;

public class JSHook {

    public static String whereIsEd = null;
    static {
        if ( System.getenv( "ED_HOME" ) != null )
            whereIsEd = System.getenv( "ED_HOME" );
    }

    static final boolean DEBUG = false;

    public static final int NO_SCOPE = -1;
    public static final int NO_FUNCTION = -2;
    public static final int INVOKE_ERROR = -3;
    
    public static final int INVOKE_SUCCESS = 0;

    // is this a security hole
    private static Scope _scope = null;
    private static final Scope _getScope(){
        if ( _scope == null )
            _scope = Scope.newGlobal();
        return _scope;
    }

    // ---- init stuff
    
    public static void init( String ed ){
        whereIsEd = ed;
    }

    // -----    scope   -------    

    public static long scopeCreate(){
        JS.JNI = true;
        Scope s = _getScope().child();
        s.setGlobal( true );
        _scopes.put( s.getId() , s );
        return s.getId();
    }

    public static boolean scopeReset( long id ){
        Scope s = _scopes.get( id );
        if ( s == null )
            return false;
        s.reset();
        return true;
    }
    public static void scopeFree( long id ){
        _scopes.remove( id );
    }
    
    // -- setters

    public static boolean scopeSetNumber( long id , String field , double val ){
        _scopes.get( id ).set( field , val );
        return true;
    }

    public static boolean scopeSetBoolean( long id , String field , boolean val ){
        _scopes.get( id ).set( field , val );
        return true;
    }

    public static boolean scopeSetString( long id , String field , String val ){
        _scopes.get( id ).set( field , val );
        return true;
    }
    
    public static boolean scopeSetObject( long id , String field , JSObject o ){
	if ( field.equals( "args" ) )
	    _nextArgs.set( o );
        _scopes.get( id ).set( field , o );
        return true;
    }

    public static boolean scopeSetObject( long id , String field , ByteBuffer buf ){
        JSObject obj = null;
        if ( buf != null ){
            buf.order( Bytes.ORDER );
            obj = new DBJSObject( buf );
        }
	return scopeSetObject( id , field , obj );
    }

    public static boolean scopeSetThis( long id , ByteBuffer buf ){
        if ( buf == null )
	    return false;
	
	buf.order( Bytes.ORDER );
	
	Scope s = _scopes.get( id );

	JSObject obj = new DBJSObject( buf );
	s.setThis( obj );

	return true;
    }

    public static boolean scopeInit( long id , ByteBuffer buf ){
        if ( buf == null )
	    return false;
	
	buf.order( Bytes.ORDER );
		    
	Scope s = _scopes.get( id );
	
	JSObject obj = new DBJSObject( buf );
	for ( Object key : obj.keySet() )
	    s.set( key , obj.get(key ) );
	return true;
    }
    
    static SimplePool<ByteDecoder> _setObjectPool = new SimplePool<ByteDecoder>( "JSHook.scopeSetObjectPool" , 10 , 10 ){
        protected ByteDecoder createNew(){
            ByteBuffer temp = ByteBuffer.wrap( new byte[1] );
            temp.order( Bytes.ORDER );
            return new ByteDecoder( temp );
        }
    };

    // -- getters

    public static double scopeGetNumber( long id , String field ){
        Object foo = _scopeGet( id , field );
        if ( foo == null )
            return 0;
        
        if ( foo instanceof Number )
            return ((Number)foo).doubleValue();
        
        if ( foo instanceof Boolean ){
            boolean b = (Boolean)foo;
            return b ? 1 : 0;
        }
        
        String msg = "can't get a number from a : " + foo.getClass();
        throw new RuntimeException( msg );
    }

    public static String scopeGetString( long id , String field ){
        Object o = _scopeGet( id , field );
        if ( o == null )
            return null;
        return o.toString();
    }
    
    public static boolean scopeGetBoolean( long id , String field ){
        return JSInternalFunctions.JS_evalToBool( _scopeGet( id , field ) );
    }

    public static byte scopeGetType( long id , String field ){
        return Bytes.getType( _scopeGet( id , field ) );
    }

    public static long scopeGuessObjectSize( long id , String field ){
        Object o = _scopeGet( id , field );
        if ( o == null )
            return 0;

        if ( ! ( o instanceof JSObject ) )
            return 0;

        return _guessSize( (JSObject)o );
    }

    public static final long _guessSize( JSObject o ){
        if ( o == null )
            return 2;
        
        long s = 20;
        
        for ( String name : o.keySet( false ) ){
            s += name.length() + 12;
            Object foo = o.get( name );
            
            if ( foo == null )
                continue;

            if ( foo instanceof Number )
                s += 12;
            else if ( foo instanceof JSString || foo instanceof String )
                s += foo.toString().length() * 3;
            else if ( foo instanceof JSDate )
                s += 12;
            else if ( foo instanceof JSObject )
                s += _guessSize( (JSObject)foo );
            else if ( foo instanceof Boolean )
                s += 2;
            else if ( foo instanceof ObjectId )
                s += 12;
            else {
                System.err.println( "JSHook : guessing size for : " + foo.getClass() );
                s += foo.toString().length() * 10;
            }
            
        }

        return s;
    }

    public static int scopeGetObject( long id , String field , ByteBuffer bb ){
        Object o = _scopeGet( id , field );
        if ( o == null ) {
            return 0;
	}

        if ( ! ( o instanceof JSObject ) ) {
            return 0;
	}

        JSObject obj = (JSObject)o;

        ByteEncoder encoder = new ByteEncoder( bb );
        encoder.putObject( obj );
        return bb.position();
    }
    
    static Object _scopeGet( long id , String field ){
        return _scopes.get( id ).get( field );
    }
    
    private static Map<Long,Scope> _scopes = Collections.synchronizedMap( new HashMap<Long,Scope>() );


    // -----    functions   -------

    public static JSFunction convertToFunction( String code ){
        code = code.trim();
        int idx = 0;
        while ( idx < code.length() && Character.isLetter( code.charAt( idx ) ) )
            idx++;
        
        if ( idx >= code.length() )
            throw new RuntimeException( "don't know how to deal with code [" + code + "]" );
        
        final String sym = code.substring( 0 , idx );
        
        if ( sym.equals( "function" ) )
            return Convert.makeAnon( code , true );
        
        if ( sym.equals( "def" ) )
            return ed.lang.python.Python.extractLambda( code );
        
        // default to JS
        return Convert.makeAnon( code , true );
    }
    
    public static long functionCreate( String code ){
        if ( DEBUG ) System.err.println( "functionCreate start " );

        JS.JNI = true;

        String md5 = null;
        synchronized( _myMd5 ){
            _myMd5.Init();
            _myMd5.Update( code.toString() );
            md5 = _myMd5.asHex();
        }
        
        if ( DEBUG ) System.err.println( "functionCreate hash : " + md5 );
        
        Pair<JSFunction,Long> p = _functions.get( md5 );
        if ( p != null )
            return p.second;
        
        JSFunction f = null;

        if ( DEBUG ) System.err.println( "\t compiling : " + code );

        try {
            f = convertToFunction( code );
        }
        catch ( Throwable t ){
            t.printStackTrace();
            return 0;
        }
        
        if ( DEBUG ) System.err.println( "\t " + f );

        long id = _funcID++;
        p = new Pair<JSFunction,Long>( f , id );
        _functions.put( md5 , p );
        _functionIDS.put( id , f );
        return id;
    }
    private final static MD5 _myMd5 = new MD5();
    private final static Map<String,Pair<JSFunction,Long>> _functions = Collections.synchronizedMap( new HashMap<String,Pair<JSFunction,Long>>() );
    private final static Map<Long,JSFunction> _functionIDS = Collections.synchronizedMap( new HashMap<Long,JSFunction>() );
    private static long _funcID = 1;

    // ------ invoke -----

    public static int invoke( long scopeID , long functionID  ){
        Scope s = _scopes.get( scopeID );
        if ( DEBUG ) System.err.println( "invoke -- scopeID : " + scopeID + " functionID : " + functionID );
        if ( s == null )
            return NO_SCOPE;
        
        JSFunction f = _functionIDS.get( functionID );
        if ( f == null )
            return NO_FUNCTION;
        
        if ( DEBUG ){
            System.err.println( "\t" + f.getName() );
            System.err.println( "\t" + f );
        }

        Object client = s.get( "$client" );
        if ( client != null ){
            String clientString = client.toString();
            DBJni db = _clients.get( clientString );
            if ( db == null ){
                db = new DBJni( clientString );
                _clients.put( clientString , db );
            }
            s.set( "db" , db );
        }
        
        try {
	    Object[] args = null;
	    {
		JSObject argsObject = _nextArgs.get();
		_nextArgs.set( null );
		if ( argsObject != null ){
		    int num=0;
		    while ( argsObject.containsKey( String.valueOf( num ) ) )
			num++;
		    
		    args = new Object[ num ];
		    for ( int i=0; i<num; i++ ){
			Object a = argsObject.get( String.valueOf( i ) ); 
			args[i] = a;
		    }
		}
	    }
	    
            Object ret = f.call( s , args );
            
            if ( ret instanceof JSFunction ){
                if ( DEBUG ) System.err.println( "\t return was function? " + ret );
                ret = ((JSFunction)ret).call( s , null );
            }
            if( DEBUG ) System.err.println( "\t setting return to :" + ret );
            s.set( "return" , ret );
            return INVOKE_SUCCESS;
        }
        catch ( Throwable t ){
            t.printStackTrace();
            scopeSetString( scopeID , "error" , t.toString() );
            return INVOKE_ERROR;
        }
    }

    static final Map<String,DBJni> _clients = Collections.synchronizedMap( new HashMap<String,DBJni>() );
    static final ThreadLocal<JSObject> _nextArgs = new ThreadLocal<JSObject>();
}
