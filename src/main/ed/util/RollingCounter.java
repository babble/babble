// RollingCounter.java

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

import bak.pcj.map.*;

public class RollingCounter {

    public RollingCounter( String name , long interval , int intervalsBack ){
	_name = name;
        _interval = interval;
        _slots = new Slot[intervalsBack];
        _lastBucket = System.currentTimeMillis();
    }

    public String getName(){
	return _name;
    }

    /** Add one to a bucket */
    public void hit( String thing ){
        hit( thing , 1);
    }

    /** Adds a given number to a bucket
     * @param num the number to add
     */
    public void hit( String thing , long num ){
        long b = bucket();
        while ( _lastBucket < b ){
            _pos++;
            if ( _pos == _slots.length )
                _pos = 0;
            _slots[_pos] = new Slot();

            _lastBucket += _interval;
        }

        if( thing != null )
            _slots[_pos].hit( thing , num );
        return;
    }

    public long bucket(){
        final long t = System.currentTimeMillis();
        return t - ( t % _interval );
    }

    public long beginning(){
	return bucket() - ( _slots.length * _interval );
    }

    /** Returns the number of intervals to save.
     * @return the number of intervals to save
     */
    public int size(){
        return _slots.length;
    }

    /** Gets a given interval
     * @param num
     *   0 = now
     *   MAX = farthest back
     * @return the number of hits on that interval
     */
    public long get( String thing , int num ){
        int p = _pos - num;
        if ( p < 0 )
            p += _slots.length;
        return _slots[ p ].get(thing);
    }

    /** Performs a 0-count hit */
    public void validate(){
        hit( null , 0 );
    }
    
    public long max(){
	long max = 0;
	for ( int i=0; i<_slots.length; i++ )
	    max = Math.max( max , _slots[i].max() );
	return max;
    }
    
    class Slot {
        
        void hit( String thing , long count ){
            _slots.put( thing , _slots.get( thing ) + count );
        }
        
        long max(){
            long max = 0;
            for ( Object o : _slots.keySet() )
                max = Math.max( max , _slots.get( o ) );
            return max;
        }

        long get( String name ){
            return _slots.get( name );
        }

        final ObjectKeyLongMap _slots = new ObjectKeyLongOpenHashMap();
    }

    final String _name;

    private long _lastBucket;
    private int _pos = 0;

    private final Slot _slots[];
    private final long _interval;
}
