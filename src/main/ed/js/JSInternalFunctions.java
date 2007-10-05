// JSInternalFunctions.java

package ed.js;

public class JSInternalFunctions {

    public boolean JS_evalToBool( Object foo ){
        if ( foo == null )
            return false;
        
        if ( foo instanceof Boolean )
            return (Boolean)foo;
        
        if ( foo instanceof Number )
            return ((Number)foo).doubleValue() != 0;

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
        
        return s1 + s2;
    }

    public Boolean JS_eq( Object a , Object b ){
        if ( a == null && b == null )
            return true;
        
        if ( a == null || b == null )
            return false;
        
        if ( a instanceof JSString )
            a = a.toString();

        if ( b instanceof JSString )
            b = b.toString();

        return a.equals( b );
    }

    public Boolean JS_ge( Object a , Object b ){
        return _compare( a , b ) >= 0;
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

    static Object _parseNumber( Object o ){
        if ( o == null )
            return null;
        
        if ( o instanceof Number )
            return o;
        
        if ( ! ( o instanceof String ) )
            return o;

        String s = (String)o;
        if ( s.length() > 9 )
            return s;

        for ( int i=0; i<s.length(); i++ )
            if ( ! Character.isDigit( s.charAt( i ) ) )
                return o;
        
        return Integer.parseInt( s );
    }
}
