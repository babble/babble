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
        
        return a.equals( b );
    }

    public Boolean JS_ge( Object a , Object b ){
        if ( a == null && b == null )
            return true;

        if ( a == null || b == null )
            return false;
        
        if ( a instanceof Number && 
             b instanceof Number ){
            return ((Number)a).doubleValue() >= ((Number)b).doubleValue();
        }

        return a.toString().compareTo( b.toString() ) >= 1;
    }

}
