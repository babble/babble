// JSNumericFunctions.java

package ed.js;

public class JSNumericFunctions extends JSObjectBase {

    JSNumericFunctions( JSFunction cons ){
        super( cons );
    }

    public Object JS_mul( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            Number an = (Number)a;
            Number bn = (Number)b;

            if ( an instanceof Double ||
                 bn instanceof Double )
                return an.doubleValue() * bn.doubleValue();
            
            return an.longValue() * bn.longValue();
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

            return an.doubleValue() / bn.doubleValue();
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

            if ( an instanceof Double ||
                 bn instanceof Double )
                return an.doubleValue() - bn.doubleValue();
            
            return an.longValue() - bn.longValue();
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
            
            return an.longValue() + bn.longValue();
        }
        
        if ( ( a != null && ( a instanceof Number ) && b == null ) ||
             ( b != null && ( b instanceof Number ) && a == null ) ){
            return Double.NaN;
        }

        String s1 = a == null ? "null" : a.toString();
        String s2 = b == null ? "null" : b.toString();
        
        return new JSString( s1 + s2 );
    }

    public Number JS_bitor( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).longValue() | ((Number)b).longValue();
        
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
            return ((Number)a).longValue() & ((Number)b).longValue();
        
        return 0;
    }

    public Number JS_bitxor( Object a , Object b ){
        
        a = _parseNumber( a );
        b = _parseNumber( b );
        
        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).longValue() ^ ((Number)b).longValue();
        
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
            return ((Number)a).longValue() % ((Number)b).longValue();
        
        return Double.NaN;
    }

    public Number JS_lsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).longValue() << ((Number)b).longValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_rsh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).longValue() >> ((Number)b).longValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_ursh( Object a , Object b ){
        a = _parseNumber( a );
        b = _parseNumber( b );

        if ( a != null && a instanceof Number && 
             b != null && b instanceof Number )
            return ((Number)a).longValue() >>> ((Number)b).longValue();
        
        if ( a == null || ! ( a instanceof Number ) )
            return 0;
        
        return (Number)a;
    }

    public Number JS_bitnot( Object a ){
        a = _parseNumber( a );
        if ( a instanceof Number )
            return ~((Number)a).longValue();
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
