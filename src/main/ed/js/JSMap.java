// JSMap.java

package ed.js;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

import java.util.*;

public class JSMap extends JSObjectBase {

    public final static JSFunction _cons = new JSFunctionCalls0(){
        
            public JSObject newOne(){
                return new JSMap();
            }
            
            public Object call( Scope scope , Object[] extra ){
                return new JSMap();
            }
        };
    
    public Object set( Object n , Object v ){
        _map.put( n , v );
        return v;
    }
    public Object get( Object n ){
        Object foo = _map.get( n );
        if ( foo != null )
            return foo;
        return super.get( n );
    }

    public void removeField( Object n ){
        _map.remove( n );
        super.removeField( n );
    }

    private final Map _map = new HashMap();
}
