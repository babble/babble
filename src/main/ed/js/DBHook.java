// DBHook.java

package ed.js;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.regex.*;

import com.twmacinta.util.*;

import ed.db.*;
import ed.util.*;
import ed.js.engine.*;

public class DBHook {

    static final boolean DEBUG = true;

    public static final int NO_SCOPE = -1;
    public static final int NO_FUNCTION = -2;
    public static final int INVOKE_ERROR = -3;
    
    public static final int INVOKE_SUCCESS = 0;

    // -----    scope   -------    

    public static long scopeCreate(){
        JS.JNI = true;
        Scope s = Scope.GLOBAL.child();
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
    
    public static boolean scopeSetObject( long id , String field , ByteBuffer buf ){
        JSObject obj = null;
        if ( buf != null ){
            buf.order( ByteOrder.LITTLE_ENDIAN );
            ByteDecoder bd = new ByteDecoder( buf );
            obj = bd.readObject();
        }
        _scopes.get( id ).set( field , obj );
        return true;
    }

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
        System.err.println( msg );
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

    static final long _guessSize( JSObject o ){
        if ( o == null )
            return 2;
        
        long s = 20;
        
        for ( String name : o.keySet() ){
            s += name.length() + 12;
            Object foo = o.get( s );
            
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
            else {
                System.err.println( "guessing on : " + foo.getClass() );
                s += foo.toString().length() * 10;
            }
            
        }

        return s;
    }

    public static int scopeGetObject( long id , String field , ByteBuffer bb ){
        Object o = _scopeGet( id , field );
        if ( o == null )
            return 0;

        if ( ! ( o instanceof JSObject ) )
            return 0;

        JSObject obj = (JSObject)o;

        ByteEncoder encoder = new ByteEncoder( bb );
        encoder.putObject( null , obj );
        return bb.position();
    }
    
    static Object _scopeGet( long id , String field ){
        return _scopes.get( id ).get( field );
    }
    
    private static Map<Long,Scope> _scopes = Collections.synchronizedMap( new HashMap<Long,Scope>() );


    // -----    functions   -------
    
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

        code = code.trim();
        
        if ( code.startsWith( "function" ) ){
            String repl = "^function \\( \\) \\{(.*)\\} $";
            repl = repl.replaceAll( " " , "\\\\s*" );
            System.out.println( repl );
            Pattern patt = Pattern.compile( repl, Pattern.DOTALL );
            
            code = patt.matcher( code ).replaceAll( "$1" );
        }

        if ( DEBUG ) System.err.println( "\t compiling : " + code );

        try {
            Convert c = new Convert( "trigger" + Math.random() , code );
            f = c.get();
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
        if ( DEBUG ) System.err.println( "scopeID : " + scopeID + " functionID : " + functionID );
        if ( s == null )
            return NO_SCOPE;
        
        JSFunction f = _functionIDS.get( functionID );
        if ( f == null )
            return NO_FUNCTION;
        
        try {
            System.err.println( f );
            Object ret = f.call( s , null );
            
            if ( ret instanceof JSFunction ){
                if ( DEBUG ) System.err.println( "\t return was function? " + ret );
                ret = ((JSFunction)ret).call( s , null );
            }
            if( DEBUG ) System.err.println( "\t setting return to :" + ret );
            s.set( "return" , ret );
            return INVOKE_SUCCESS;
        }
        catch ( Throwable t ){
            scopeSetString( scopeID , "error" , t.toString() );
            return INVOKE_ERROR;
        }
    }

}
