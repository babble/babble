// JSInternalFunctions.java

package ed.js;

public class JSInternalFunctions {

    public Object JS_add( Object a , Object b ){
        
        if ( a != null && ( a instanceof Number ) &&
             b != null && ( b instanceof Number ) ){
            
            double d1 = (Double)a;
            double d2 = (Double)b;
            
            return d1 + d2;
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
}
