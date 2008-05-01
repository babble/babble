// WeakValueMap.java

package ed.util;

import java.lang.ref.*;
import java.util.*;

public class WeakValueMap<K,V> /* implements Map<K,V> */ {

    public WeakValueMap(){
        _map = new HashMap<K,WeakReference<V>>();
    }

    public V get( Object key ){
        System.out.println( "get: " + key );
        WeakReference<V> r = _map.get( key );
        if ( r == null )
            return null;
        
        V v = r.get();
        if ( v == null )
            _map.remove( key );

        return v;
    }

    public V put( K key , V v ){
        System.out.println( "put: " + key );
        final WeakReference<V> nr = new WeakReference<V>( v );
        final WeakReference<V> or = _map.put( key , nr );
        if ( or == null )
            return null;
        return or.get();
    }

    final Map<K,WeakReference<V>> _map;
}
