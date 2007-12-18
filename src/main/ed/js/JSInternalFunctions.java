// JSInternalFunctions.java

package ed.js;

import ed.js.engine.*;

public class JSInternalFunctions extends JSObjectBase {

    public final static JSString TYPE_STRING = new JSString( "string" );
    public final static JSString TYPE_NATIVE_STRING = new JSString( "native_string" );
    public final static JSString TYPE_NUMBER = new JSString( "number" );
    public final static JSString TYPE_BOOLEAN = new JSString( "boolean" );
    public final static JSString TYPE_UNDEFINED = new JSString( "undefined" );
    public final static JSString TYPE_OBJECT = new JSString( "object" );
    public final static JSString TYPE_NATIVE = new JSString( "native" );
    public final static JSString TYPE_FUNCTION = new JSString( "function" );

    public boolean JS_instanceof( Object thing , Object type ){
        throw new RuntimeException( "the spec for instanceof is weird and broken - deferring" );
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

    public static boolean JS_evalToBool( Object foo ){
        if ( foo == null )
            return false;
        
        if ( foo instanceof Boolean )
            return (Boolean)foo;
        
        if ( foo instanceof Number )
            return ((Number)foo).doubleValue() != 0;
        
        if ( foo instanceof String || 
             foo instanceof JSString )
            return foo.toString().length() > 0 ;

        return true;
    }

    public Object JS_mul( Object a , Object b ){
        

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Double ||
                 bn instanceof Double )
                return an.doubleValue() * bn.doubleValue();
            
            return an.intValue() * bn.intValue();
        }
        
        return Double.NaN;
    }

    public Object JS_div( Object a , Object b ){
        

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            return an.doubleValue() / bn.doubleValue();
        }
        
        return Double.NaN;
    }

    public Object JS_sub( Object a , Object b ){
        

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Double ||
                 bn instanceof Double )
                return an.doubleValue() - bn.doubleValue();
            
            return an.intValue() - bn.intValue();
        }
        
        return Double.NaN;
    }

    public Object JS_add( Object a , Object b ){
        
        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Double ||
                 bn instanceof Double )
                return an.doubleValue() + bn.doubleValue();
            
            return an.intValue() + bn.intValue();
        }
        
        if ( ( a != null && ( a instanceof Number ) && b == null ) ||
             ( b != null && ( b instanceof Number ) && a == null ) ){
            return Double.NaN;
        }

        String s1 = a == null ? "null" : a.toString();
        String s2 = b == null ? "null" : b.toString();
        
        return new JSString( s1 + s2 );
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
        
        return false;
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

        if ( a == null )
            return 1;
        
        if (  b == null )
            return -1;
        
        if ( a.equals( b ) )
            return 0;

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
    
    public Number JS_bitor( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() | ((Number)b).intValue();
        
        if ( a != null && a instanceof Number )
            return (Number)a;

        if ( b != null && b instanceof Number )
            return (Number)b;

        return 0;
    }

    public Number JS_bitand( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() & ((Number)b).intValue();
        
        /*
        if ( a != null && a instanceof Number )
            return (Number)a;

        if ( b != null && b instanceof Number )
            return (Number)b;
        */
        return 0;
    }

    public Number JS_bitxor( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() ^ ((Number)b).intValue();
        
        if ( a != null && a instanceof Number )
            return (Number)a;

        if ( b != null && b instanceof Number )
            return (Number)b;

        return 0;
    }

    public Number JS_mod( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() % ((Number)b).intValue();
        
        return Double.NaN;
    }

    public Number JS_lsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() << ((Number)b).intValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_rsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() >> ((Number)b).intValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_ursh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).intValue() >>> ((Number)b).intValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_bitnot( Object a ){
        a = _parseNumber( a );
        if ( a instanceof Number )
            return ~((Number)a).intValue();
        return -1;
    }

    static final Object _parseNumber( final Object o ){
        if ( o == null )
            return null;
        
        if ( o instanceof Number )
            return o;
        
        String s = null;
        if ( o instanceof JSString )
            s = o.toString();
        else if ( o instanceof String )
            s = o.toString();
        
        if ( s == null )
            return o;

        if ( s.length() > 9 )
            return s;

        boolean allDigits = true;
        for ( int i=0; i<s.length(); i++ ){
            final char c = s.charAt( i );
            if ( ! Character.isDigit( c ) ){
                allDigits = false;
                if ( c != '.' )
                    return o;
            }
        }
        
        if ( allDigits )
            return Integer.parseInt( s );
        
        if ( s.matches( "\\d+\\.\\d+" ) )
            return Double.parseDouble( s );

        return o;
    }

    static String _debug( Object o ){
        if ( o == null )
            return "null";
        
        return "[" + o + "](" + o.getClass() + ")" ;
    }
}
