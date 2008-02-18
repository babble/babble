// SimplePool.java

package ed.util;

import java.util.*;

public abstract class SimplePool<T> {

    /**
     * @param maxToKeep max to hold to at any given time can be -1
     * @param maxTotal how many to have at any given time
     */
    public SimplePool( String name , int maxToKeep , int maxTotal ){
        _name = name;
        _maxToKeep = maxToKeep;
        _maxTotal = maxTotal;
    }

    protected abstract T createNew();

    public boolean ok( T t ){
        return true;
    }

    public void done( T t ){
        if ( ! ok( t ) )
            return;

        synchronized ( _avail ){
            if ( _maxToKeep > 0 && _avail.size() < _maxToKeep )
                _avail.add( t );
        }
    }
    
    public T get(){
        while ( true ){
            synchronized ( _avail ){
                if ( _avail.size() > 0 )
                    return _avail.remove( _avail.size() - 1 );
                
                if ( _maxTotal <= 0 || _all.size() < _maxTotal ){
                    T t = createNew();
                    _all.put( t , "as" );
                    return t;
                }
            }
            ThreadUtil.sleep( 15 );
        }
    }

    final String _name;
    final int _maxToKeep;
    final int _maxTotal;

    private final List<T> _avail = new ArrayList<T>();
    private final WeakHashMap<T,String> _all = new WeakHashMap<T,String>();

}
