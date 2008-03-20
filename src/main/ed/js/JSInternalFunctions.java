// JSInternalFunctions.java

package ed.js;

import ed.js.engine.*;
import ed.js.func.*;

public class JSInternalFunctions extends JSNumericFunctions {

    static { JS._debugSIStart( "JSInternalFunctions" ); }

    static { JS._debugSI( "JSInternalFunctions" , "0" ); }

    public final static JSString TYPE_STRING = new JSString( "string" );
    static { JS._debugSI( "JSInternalFunctions" , "0.1" ); }
    public final static JSString TYPE_NATIVE_STRING = new JSString( "native_string" );
    public final static JSString TYPE_NUMBER = new JSString( "number" );
    public final static JSString TYPE_BOOLEAN = new JSString( "boolean" );
    public final static JSString TYPE_UNDEFINED = new JSString( "undefined" );
    public final static JSString TYPE_OBJECT = new JSString( "object" );
    public final static JSString TYPE_NATIVE = new JSString( "native" );
    public final static JSString TYPE_FUNCTION = new JSString( "function" );

    static {
        JS._debugSI( "JSInternalFunctions" , "1" );
    }


    public static JSFunction FunctionCons = new JSFunctionCalls0(){
            
            public Object call( Scope s , Object extra[] ){
                Object t = s.getThis();
                if ( t != null )
                    return t;

                return new JSFunctionCalls0(){
                    public Object call( Scope s , Object extra[] ){
                        return null;
                    }
                };

            }

            protected void init(){
                JS._debugSI( "JSInternalFunctions" , "FunctionCons init" );
            }
                        
        };

    static {
        JS._debugSI( "JSInternalFunctions" , "2" );
    }
    
    public JSInternalFunctions(){
        super( FunctionCons );
    }

    public static final Object self( Object o ){
        return o;
    }

    public boolean JS_instanceof( Object thing , Object type ){
        if ( thing == null )
            return false;
        
        if ( type == null )
            throw new NullPointerException( "type can't be null" );
        
        if ( thing instanceof Number ){
            //return type == JSNumber.CONS;
            return false;
        }

        if ( type instanceof String || type instanceof JSString ){
            try {
                Class clazz = JSBuiltInFunctions._getClass( type.toString() );
                return clazz.isAssignableFrom( thing.getClass() );
            }
            catch ( Exception e ){
                throw new JSException( "can't find : " + type , e );
            }
        }
        
        if ( ! ( thing instanceof JSObjectBase ) )
            return false;
        
        if ( type instanceof JSBuiltInFunctions.NewObject )
            return true;

        JSObjectBase o = (JSObjectBase)thing;
        
        if ( o.getConstructor() == null )
            return false;

        return o.getConstructor() == type;
    }

    public JSString JS_typeof( Object obj ){

        if ( obj == null )
            return TYPE_UNDEFINED;
        
        if ( obj instanceof JSString )
            return TYPE_STRING;

        if ( obj instanceof String )
            return TYPE_NATIVE_STRING;
        
        if ( obj instanceof Number )
            return TYPE_NUMBER;

        if ( obj instanceof Boolean )
            return TYPE_BOOLEAN;
        
        if ( obj instanceof JSFunction )
            return TYPE_FUNCTION;

        if ( obj instanceof JSObject )
            return TYPE_OBJECT;
        

        return TYPE_NATIVE;
    }

    public JSObject JS_buildLiteralObject( String names[] , Object ... fields ){
        JSObject o = new JSObjectBase();
        int max = names.length;
        if ( fields != null )
            max = Math.min( fields.length , max );
        for ( int i=0; i<max; i++ )
            o.set( names[i] , fields == null ? null : fields[i] );
        return o;
    }

    public Object JS_setDefferedPlus( JSObject obj , Object place , Object other ){
        return obj.set( place , JS_add( obj.get( place ) , other ) );
    }
    
    public Object JS_inc( JSRef ref , boolean post , int num ){
        Object obj = ref.get();
        if ( obj == null || ! ( obj instanceof Number ) )
            throw new RuntimeException( "got a non number : [" + obj + "] from : [" + ref  + "] is a [" +  ( obj == null ? "null" : obj.getClass().getName() ) + "]" );
        Object n = JS_add( num , obj );
        ref.set( n );
        return post ? obj : n;
    }

    public static Object JS_or( Object a , Object b ){
        if ( JS_evalToBool( a ) )
            return a;
        return b;
    }

    public static Boolean JS_not( Object o ){
        return ! JS_evalToBool( o );
    }

    public static boolean JS_evalToBool( Object foo ){
        if ( foo == null )
            return false;
        
        if ( foo instanceof Boolean )
            return (Boolean)foo;
        
        if ( foo instanceof Number ){
            Number n = (Number)foo;
            if ( Double.isNaN( n.doubleValue() ) )
                return false;
            return n.doubleValue() != 0;
        }
        
        if ( foo instanceof String || 
             foo instanceof JSString )
            return foo.toString().length() > 0 ;

        return true;
    }

    public static Boolean JS_shne( Object a , Object b ){
        return ! JS_sheq( a , b );
    }

    public static Boolean JS_sheq( Object a , Object b ){
        if ( a == b )
            return true;
        
        if ( a == null && b == null )
            return true;
        
        if ( a == null || b == null )
            return false;
        
        if ( a instanceof Number || b instanceof Number ){
            a = _parseNumber( a );
            b = _parseNumber( b );
        }

        if ( a instanceof Number && b instanceof Number ){
            return ((Number)a).doubleValue() == ((Number)b).doubleValue();
        }
        
        if ( a instanceof JSString )
            a = a.toString();
        
        if ( b instanceof JSString )
            b = b.toString();
        
        if ( a instanceof String && b instanceof String )
            return a.equals( b );
        
        if ( _charEQ( a , b ) || _charEQ( b , a ) )
            return true;

        return false;
    }
    
    private static boolean _charEQ( Object a , Object b ){
        if ( ! ( a instanceof String ) )
            return false;
        
        if ( ! ( b instanceof Character ) )
            return false;
        
        String s = (String)a;
        Character c = (Character)b;
        
        if ( s.length() != 1 )
            return false;
        
        return s.charAt(0) == c;
    }

    public static Boolean JS_eq( Object a , Object b ){
        if ( JS_sheq( a , b ) )
            return true;
	
	if ( a == null || b == null )
	    return false;

	return a.equals( b );
    }

    public Boolean JS_ge( Object a , Object b ){
        return _compare( a , b ) >= 0;
    }

    public Boolean JS_le( Object a , Object b ){
        return _compare( a , b ) <= 0;
    }

    public Boolean JS_lt( Object a , Object b ){
        return _compare( a , b ) < 0;
    }

    public Boolean JS_gt( Object a , Object b ){
        return _compare( a , b ) > 0;
    }
    
    int _compare( Object a , Object b ){
        if ( a == null && b == null )
            return 0;

        if ( a == null ){
            if ( b instanceof Number )
                a = 0;
            else
                return 1;
        }
        
        if (  b == null ){
            if ( a instanceof Number )
                b = 0;
            else
                return -1;
        }
        
        if ( a.equals( b ) )
            return 0;

        if ( a instanceof Number || b instanceof Number ){
            a = _parseNumber( a );
            b = _parseNumber( b );
        }

        if ( a instanceof Number && 
             b instanceof Number ){
            double aVal = ((Number)a).doubleValue();
            double bVal = ((Number)b).doubleValue();
            if ( aVal == bVal )
                return 0;
            return aVal < bVal ? -1 : 1;
        }

        if ( a instanceof Comparable )
            return ((Comparable)a).compareTo( b );

        return a.toString().compareTo( b.toString() );
    }
    
    public static final Object parseNumber( final Object o , final Object def ){
	Object r = _parseNumber( o );
	if ( r instanceof Number )
	    return r;
	return _parseNumber( def );
    }

    public static final Object JS_comma( Object ... o ){
        if ( o == null || o.length == 0 )
            return null;
        return o[o.length-1];
    }



    static String _debug( Object o ){
        if ( o == null )
            return "null";
        
        return "[" + o + "](" + o.getClass() + ")" ;
    }

    static {
        JS._debugSIDone( "JSInternalFunctions" );
    }
}
