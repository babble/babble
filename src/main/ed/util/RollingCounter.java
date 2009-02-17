// RollingCounter.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.util;

import java.util.*;

import bak.pcj.map.*;

public class RollingCounter {

    public RollingCounter( long interval , int intervalsBack ){
        this( "not-named" , interval , intervalsBack );
    }

    public RollingCounter( String name , long interval , int intervalsBack ){
	_name = name;
        _interval = interval;
        _slots = new Slot[intervalsBack];
        _lastBucket = bucket();
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

        if( thing != null ){
	    if ( _slots[_pos] == null )
		_slots[_pos] = new Slot();
            _slots[_pos].hit( thing , num );
	}
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
        return _slots[ _pos(num) ].get(thing);
    }

    private int _pos( int num ){
        int p = _pos - num;
        if ( p < 0 )
            p += _slots.length;
        return p;
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
    
    /**
       sorts keys by total hits
       each entry is multipled by scaler^pos.
       so pos 0 is just itself
       pos 1 is scaler * hit
       pos 2 is scaler^2 * hut
       so if you only care about the first
       just use 0
     */
    public List<String> sorted( double scaler ){
        final ObjectKeyDoubleMap map = new ObjectKeyDoubleOpenHashMap();

        for ( int i=0; i<size(); i++ ){
            Slot s = _slots[_pos(i)];
            if ( s == null )
                continue;
            s.sum( map , Math.pow( scaler , i ) );
        }

        List<String> all = new ArrayList<String>();
        for ( Object o : map.keySet() )
            all.add( o.toString() );
        
        Collections.sort( all , new Comparator<String>(){
                public int compare( String a , String b ){
                    double av = map.get( a );
                    double bv = map.get( b );
                    
                    if ( av == bv )
                        return 0;

                    return av < bv ? 1 : -1;
                }
            }
            );

        return all;
    }

    public ThingsPerTimeTracker getTimeTrackerForOne( String thing ){
        return new ThingsPerTimeTracker( this , thing );
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
        
        void sum( ObjectKeyDoubleMap map , double mult ){
            for ( Object o : _slots.keySet() ){
                double val = map.get( o );
                val += _slots.get( o ) * mult;
                map.put( o , val );
            }
        }

        final ObjectKeyLongMap _slots = new ObjectKeyLongOpenHashMap();
    }

    final String _name;
    final long _interval;
    final Slot _slots[];
    
    long _lastBucket;
    int _pos = 0;
}
