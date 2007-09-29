// JSObject.java

package ed.js;

import java.util.*;

public class JSObject {

    public void set( String n , Object v ){
        _map.put( n , v );
    }

    public Object get( String n ){
        return _map.get( n );
    }


    private Map<String,Object> _map = new TreeMap<String,Object>();
}
