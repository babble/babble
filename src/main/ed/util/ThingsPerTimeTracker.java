//ThingsPerTimeTracker.java

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

/** @expose */
public class ThingsPerTimeTracker {

    /** Initializes a new time tracker
     * @param interval bucket size.  so for per second, use 1000
     * @param intervalsBack the number of intervals to save
     */
    public ThingsPerTimeTracker( long interval , int intervalsBack ){
	this( "unnamed" , interval , intervalsBack );
    }

    /** Initializes a new time tracker
     * @param interval bucket size.  so for per second, use 1000
     * @param intervalsBack the number of intervals to save
     */
    public ThingsPerTimeTracker( String name , long interval , int intervalsBack ){
	_name = name;
        _interval = interval;
        _counts = new long[intervalsBack];
        _lastBucket = bucket();
    }

    ThingsPerTimeTracker( RollingCounter counter , String thing ){
        _name = thing;
        _interval = counter._interval;

        _lastBucket = counter._lastBucket;
        _pos = counter._pos;

        _counts = new long[counter._slots.length];
        for ( int i=0; i<_counts.length; i++ )
            if ( counter._slots[i] != null )
                _counts[i] = counter._slots[i].get( thing );
        
    }

    public String getName(){
	return _name;
    }

    /** Add one to a bucket */
    public void hit(){
        hit(1);
    }

    /** Adds a given number to a bucket
     * @param num the number to add
     */
    public void hit( long num ){
        long b = bucket();
        while ( _lastBucket < b ){
            _pos++;
            if ( _pos == _counts.length )
                _pos = 0;
            _counts[_pos] = 0;

            _lastBucket += _interval;
        }

        _counts[_pos] += num;
        return;
    }

    public long bucket(){
        final long t = System.currentTimeMillis();
        return t - ( t % _interval );
    }

    public long beginning(){
	return bucket() - ( _counts.length * _interval );
    }

    /** Returns the number of intervals to save.
     * @return the number of intervals to save
     */
    public int size(){
        return _counts.length;
    }

    /** Gets a given interval
     * @param num
     *   0 = now
     *   MAX = farthest back
     * @return the number of hits on that interval
     */
    public long get( int num ){
        int p = _pos - num;
        if ( p < 0 )
            p += _counts.length;
        return _counts[ p ];
    }

    /** Performs a 0-count hit */
    public void validate(){
        hit( 0 );
    }
    
    public long max(){
	long max = 0;
	for ( int i=0; i<_counts.length; i++ )
	    max = Math.max( max , _counts[i] );
	return max;
    }

    final String _name;

    private long _lastBucket;
    private int _pos = 0;

    private final long _counts[];
    private final long _interval;
}
