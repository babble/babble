// SimplePool.java

package ed.util;

import java.util.*;

public abstract class SimplePool<T> {

    /**
     * @param maxToKeep max to hold to at any given time can be -1
     * @param maxTotal how many to have at any given time
     */
    public SimplePool( String name , int maxToKeep , int maxTotal ){
        this( name , maxToKeep , maxTotal , false );
    }

    /**
     * @param maxToKeep max to hold to at any given time can be -1
     * @param maxTotal how many to have at any given time
     */
    public SimplePool( String name , int maxToKeep , int maxTotal , boolean trackLeaks ){
        _name = name;
        _maxToKeep = maxToKeep;
        _maxTotal = maxTotal;
        _trackLeaks = trackLeaks;
    }

    protected abstract T createNew();

    public boolean ok( T t ){
        return true;
    }

    public void done( T t ){
        _where.remove( _hash( t ) );

        if ( ! ok( t ) )
            return;

        synchronized ( _avail ){
            if ( _maxToKeep > 0 && _avail.size() < _maxToKeep )
                _avail.add( t );
        }
    }
    
    public T get(){
        final T t = _get();
        if ( t != null && _trackLeaks ){
            Throwable stack = new Throwable();
            stack.fillInStackTrace();
            _where.put( _hash( t ) , stack );
        }
        return t;
    }

    private int _hash( T t ){
        return System.identityHashCode( t );
    }

    private T _get(){
        while ( true ){
            synchronized ( _avail ){
                if ( _avail.size() > 0 )
                    return _avail.remove( _avail.size() - 1 );
                
                if ( _maxTotal <= 0 || _all.size() < _maxTotal ){
                    T t = createNew();
                    _all.add( t );
                    return t;
                }
                
                if ( _trackLeaks && _trackPrintCount++ % 200 == 0 ){
                    _wherePrint();
                    _trackPrintCount = 1;
                }
            }
            ThreadUtil.sleep( 15 );
        }
    }

    private void _wherePrint(){
        StringBuilder buf = new StringBuilder( "Pool : " + _name + " waiting b/c of\n" );
        for ( Throwable t : _where.values() ){
            buf.append( "--\n" );
            final StackTraceElement[] st = t.getStackTrace();
            for ( int i=0; i<st.length; i++ )
                buf.append( "  " ).append( st[i] ).append( "\n" );
            buf.append( "----\n" );
        }
        
        System.out.println( buf );
    }

    protected void clear(){
        _avail.clear();
        _all.clear();
        _where.clear(); // is this correct
    }

    final String _name;
    final int _maxToKeep;
    final int _maxTotal;
    final boolean _trackLeaks;

    private final List<T> _avail = new ArrayList<T>();
    private final WeakBag<T> _all = new WeakBag<T>();
    private final Map<Integer,Throwable> _where = new HashMap<Integer,Throwable>();

    private int _trackPrintCount = 0;
}
