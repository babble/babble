// LRUCache.java

package ed.util;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * simple lru cache
 * @expose
 */
public class LRUCache<K,V> {

    final static boolean D = false;

    /**
     *  Creates a LRUCache of no size limit
     *
     * @param defaultCacheTime default time to cache elements (in milliseconds)
     */
    public LRUCache( long defaultCacheTime ){
        this( defaultCacheTime , -1 );
    }

    /**
     *  Creates a LRUCache of limited size.
     *
     * @param defaultCacheTime default time to cache elements (in milliseconds)
     * @param maxSize maximum number of entries in the cache
     */
    public LRUCache( long defaultCacheTime , int maxSize ){
        _defaultCacheTime = defaultCacheTime;
        _maxSize = maxSize;
    }

    /**
     * Adds an element to the map.  Element will be cached for the default cache
     * time specified in the CTOR
     *
     * @param k
     * @param v
     */
    public void put( K k , V v ){
        put( k, v , _defaultCacheTime );
    }

    /**
     *  Add an element to the map with the specified timeout.
     * @param k
     * @param v
     * @param cacheTime number of milliseconds to cache this element
     */
    public void put( K k , V v , long cacheTime ){
        _cache.put( k , new Entry( v , cacheTime ) );
    }

    /**
     *  Gets the value specified by the key as long as the value has been
     *  cached less than the specified cache time
     *
     * @param k
     * @param cacheTime maximum age of cached element to return (in milliseconds)
     * @return value for key if found, null if not found, if the value is expired, or
     *         if value has been in cache longer than specified cacheTime
     */
    public V get( K k , long cacheTime ){

        Entry e = _cache.get( k );
        if ( e == null ){
            if ( D ) System.out.println( "LRUCache.get : null" );
            return null;
        }

        if ( ! e.valid( cacheTime ) ){
            if ( D ) System.out.println( "LRUCache.get : invalid" );
            _cache.remove( k );
            return null;
        }

        return e._v;
    }

    /**
     *  Gets the value specified by the key
     *
     * @param k
     * @param cacheTime maximum age of cached element to return (in milliseconds)
     * @return value for key if found, null if not or if value expired
     */
    public V get( K k ){
        Entry e = _cache.get( k );
        if ( e == null ){
            if ( D ) System.out.println( "LRUCache.get : null" );
            return null;
        }

        if ( ! e.valid() ){
            if ( D ) System.out.println( "LRUCache.get : invalid" );
            _cache.remove( k );
            return null;
        }

        return e._v;
    }

    /** Returns the number of elements in the cache.
     * @return the number of elements in the cache (irrespective of validity?)
     */
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

    /** @unexpose */
    final long _defaultCacheTime;
    /** @unexpose */
    final int _maxSize;
    /** @unexpose */
    final Map<K,Entry> _cache = new LinkedHashMap<K,Entry>(){
        static final long serialVersionUID = -1;

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

