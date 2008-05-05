// JSNumericFunctions.java

package ed.js;

public class JSNumericFunctions extends JSObjectBase {

    static {
        JS._debugSIStart( "JSNumericFunctions" );
    }

    JSNumericFunctions( JSFunction cons ){
        super( cons );
    }

    public static final Object fixType( final Object o ){

        if ( o == null )
            return o;
        
        if ( o instanceof String )
            return new JSString( o.toString() );

        if ( o instanceof Number ){
            if ( o instanceof Float || 
                 o instanceof Double ){
                
                final Number n = (Number)o;
                final double d = n.doubleValue();
                
                if ( couldBeInt( d ) )
                    return n.intValue();

                if ( couldBeLong( d ) )
                    return (long)d;
            }
            
            return o;
        }
        
        
        return o;
    }

    public static final boolean couldBeInt( final double d ){
        if ( d > Integer.MAX_VALUE / 2 )
            return false;
        
        if ( d < Integer.MIN_VALUE / 2 )
            return false;
        
        if ( Math.floor( d ) != d )
            return false;

        return true;
    }

    public static final boolean couldBeLong( final double d ){
        if ( d > Long.MAX_VALUE / 2 )
            return false;
        
        if ( d < Long.MIN_VALUE / 2 )
            return false;
        
        long l = (long)d;
        return l == d;
    }

    public Object JS_mul( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer ){
                
                int ai = an.intValue();
                int bi = bn.intValue();
                
                int mul = an.intValue() * bn.intValue();

                if ( ai > 0 && bi > 0 && mul < 0 );
                else return mul;
            }
            
            return fixType( an.doubleValue() * bn.doubleValue() );
        }
        
        return Double.NaN;
    }

    public Object JS_div( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b ) ;       

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            return fixType( an.doubleValue() / bn.doubleValue() );
        }
        
        return Double.NaN;
    }

    public Object JS_sub( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );        

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer )
                return an.intValue() - bn.intValue();
            
            return an.doubleValue() - bn.doubleValue();
        }
        
        return Double.NaN;
    }

    public Object JS_add( Object a , Object b ){
        
        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Integer &&
                 bn instanceof Integer )
                return an.intValue() + bn.intValue();
            
            return fixType( an.doubleValue() + bn.doubleValue() );
        }
        
        if ( ( a != null && ( a instanceof Number ) && b == null ) ||
             ( b != null && ( b instanceof Number ) && a == null ) ){
            return Double.NaN;
        }

        String s1 = a == null ? "null" : a.toString();
        String s2 = b == null ? "null" : b.toString();
        
        return new JSString( s1 + s2 );
    }

    public static Number JS_bitor( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a instanceof Boolean )
            if ( ((Boolean)a) )
                a = 1;
        
        if ( b instanceof Boolean )
            if ( ((Boolean)b) )
                b = 1;

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

    static final Object _parseNumber( final Object orig ){
        Object o = orig;
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
            return orig;

        if ( s.length() > 9 )
            return s;

        boolean allDigits = true;
        for ( int i=0; i<s.length(); i++ ){
            final char c = s.charAt( i );
            if ( ! Character.isDigit( c ) ){
                allDigits = false;
                if ( c != '.' )
                    return orig;
            }
        }
        
        if ( allDigits )
            return Integer.parseInt( s );
        
        if ( s.matches( "\\d+\\.\\d+" ) )
            return Double.parseDouble( s );

        return orig;
    }
}
