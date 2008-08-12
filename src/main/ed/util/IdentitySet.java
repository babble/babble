// IdentitySet.java

package ed.util;

import java.util.*;

public class IdentitySet<T> {

    public void add( T t ){
        _map.put( t , "a" );
    }

    public boolean contains( T t ){
        return _map.containsKey( t );
    }
    
    final IdentityHashMap<T,String> _map = new IdentityHashMap<T,String>();
}
