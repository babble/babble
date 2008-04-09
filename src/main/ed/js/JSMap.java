// JSMap.java

package ed.js;

import ed.util.*;
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

        if ( n instanceof String || 
             n instanceof JSString ||
             n instanceof Number )
            return super.get( n );

        return null;
    }
    
    public Object removeField( Object n ){
        Object val = _map.remove( n );
        Object val2 = super.removeField( n );

        return val == null ? val2 : val;
    }
    
    public Collection keys(){
        return _map.keySet();
    }

    public Collection values(){
        return _map.values();
    }
    
    private final Map _map = new CustomHashMap(){
            public boolean doEquals( Object a , Object b ){
                
                if ( a.getClass() == JSObjectBase.class &&
                     b.getClass() == JSObjectBase.class ){
                    
                    JSObjectBase ao = (JSObjectBase)a;
                    JSObjectBase bo = (JSObjectBase)b;

                    Collection<String> ak = ao.keySet();
                    Collection<String> bk = bo.keySet();

                    if ( ak.size() != bk.size() )
                        return false;
                    
                    for ( String k : ak )
                        if ( ! JSInternalFunctions.JS_eq( ao.get( k ) , bo.get( k ) ) )
                            return false;
                    
                    return true;

                }

                return JSInternalFunctions.JS_eq( a , b );
            }
            
            public int doHash( Object key ){
            
                if ( key == null )
                    return 0;
    
                if ( key instanceof JSObject ){
                    int hash = 0;
                    for ( String s : ((JSObject)key).keySet() )
                        hash += s.hashCode();
                    return hash;
                }
                
                return key.hashCode();
                
            }
            
        };
}
