// LRUCache.java

package ed.util;

import java.util.*;

/**
 * simple lru cache
 */
public class LRUCache<K,V> {

    public LRUCache( long defaultCacheTime ){
        this( defaultCacheTime , -1 );
    }

    public LRUCache( long defaultCacheTime , int maxSize ){
        _defaultCacheTime = defaultCacheTime;
        _maxSize = maxSize;
    }
    
    public void put( K k , V v ){
        put( k, v , _defaultCacheTime );
    }

    public void put( K k , V v , long cacheTime ){
        _cache.put( k , new Entry( v , cacheTime ) );
    }
    
    public V get( K k , long cacheTime ){

        Entry e = _cache.get( k );
        if ( e == null )
            return null;
        
        if ( ! e.valid( cacheTime ) ){
            _cache.remove( k );
            return null;
        }        

        return e._v;
    }

    public V get( K k ){
        Entry e = _cache.get( k );
        if ( e == null )
            return null;
        
        if ( ! e.valid() ){
            _cache.remove( k );
            return null;
        }

        return e._v;
    }

    public int size(){
        return _cache.size();
    }

    class Entry {
        Entry( V v , long cacheTime ){
            _v = v;
            _cacheTime = cacheTime;
            _expireTime = _createTime + _cacheTime;
        }
        
        boolean valid( long cacheTime ){
            return valid() && 
                ( _createTime + cacheTime ) > System.currentTimeMillis();
        }

        boolean valid(){
            return _expireTime > System.currentTimeMillis();
        }

        final V _v;
        final long _cacheTime;
        final long _expireTime;
        
        final long _createTime = System.currentTimeMillis();
    }

    final long _defaultCacheTime;
    final int _maxSize;
    final Map<K,Entry> _cache = new LinkedHashMap<K,Entry>(){
        protected boolean removeEldestEntry( Map.Entry<K,Entry> eldest){

            // if its too big, kill the oldest
            if ( _maxSize > 0 && size() > _maxSize )
                return true;
            
            // if the oldest is invalid, get rid of no matter what
            if ( ! eldest.getValue().valid() )
                return true;

            return false;
        }
    };

}
