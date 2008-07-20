// SimplePool.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.util;

import java.util.*;

/** @expose */
public abstract class SimplePool<T> {

    /** 
     * See full constructor docs
     */
    public SimplePool( String name , int maxToKeep , int maxTotal ){
        this( name , maxToKeep , maxTotal , false );
    }

    /** Initializes a new pool of objects.
     * @param name name for the pool
     * @param maxToKeep max to hold to at any given time. if < 0 then no limit
     * @param maxTotal max to have allocated at any point.  if there are no more, get() will block
     * @param trackLeaks if leaks should be tracked
     */
    public SimplePool( String name , int maxToKeep , int maxTotal , boolean trackLeaks ){
        _name = name;
        _maxToKeep = maxToKeep;
        _maxTotal = maxTotal;
        _trackLeaks = trackLeaks;
    }

    /** Creates a new object of this pool's type.
     * @return the new object.
     */
    protected abstract T createNew();

    /** 
     * callback to determin if an object is ok to be added back to the pool
     * by default just returns true.  override to return false in some cases if you want 
     * an object to be tossed under certain conditions
     * @return true iff the object is ok to be added back to pool
     */
    public boolean ok( T t ){
        return true;
    }

    /** 
     * call done when you are done with an object form the pool
     * if there is room and the object is ok will get added
     * @param t Object to add
     */
    public void done( T t ){
        _where.remove( _hash( t ) );

        if ( ! ok( t ) )
            return;

        synchronized ( _avail ){
            if ( _maxToKeep < 0 || _avail.size() < _maxToKeep )
                _avail.add( t );
        }
    }

    /** Gets an object from the pool.
     * will block if none are available
     * @return An object from the pool
     */
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

    /** Clears the pool of all objects. */
    protected void clear(){
        _avail.clear();
        _all.clear();
        _where.clear(); // is this correct
    }

    /** @unexpose */
    final String _name;
    /** @unexpose */
    final int _maxToKeep;
    /** @unexpose */
    final int _maxTotal;
    /** @unexpose */
    final boolean _trackLeaks;

    private final List<T> _avail = new ArrayList<T>();
    private final WeakBag<T> _all = new WeakBag<T>();
    private final Map<Integer,Throwable> _where = new HashMap<Integer,Throwable>();

    private int _trackPrintCount = 0;
}
