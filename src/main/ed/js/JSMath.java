// JSMath.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class JSMath extends JSObjectBase {

    private static JSMath _instance = new JSMath();
    public static JSMath getInstance(){
        return _instance;
    }
    
    private JSMath(){
        set( "max" , 
             new JSFunctionCalls2(){
                 public Object call( Scope s , Object a , Object b , Object foo[] ){

                     if ( a != null && ! ( a instanceof Number ) )
                         return Double.NaN;

                     if ( b != null && ! ( b instanceof Number ) )
                         return Double.NaN;
                     
                     if ( a == null && b == null )
                         return 0;
                     
                     if ( a == null )
                         return b;

                     if ( b == null )
                         return a;

                     return ((Number)a).doubleValue() > ((Number)b).doubleValue() ? a : b;
                 }
             } );
    }
    

    
}
