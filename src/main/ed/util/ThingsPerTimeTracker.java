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
        _interval = interval;
        _counts = new int[intervalsBack];
        _lastBucket = System.currentTimeMillis();
    }

    /** Add one to a bucket */
    public void hit(){
        hit(1);
    }

    /** Adds a given number to a bucket
     * @param num the number to add
     */
    public void hit( int num ){
        long b = _bucket();
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

    private long _bucket(){
        final long t = System.currentTimeMillis();
        return t - ( t % _interval );
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
    public int get( int num ){
        int p = _pos - num;
        if ( p < 0 )
            p += _counts.length;
        return _counts[ p ];
    }

    /** Performs a 0-count hit */
    public void validate(){
        hit( 0 );
    }

    private long _lastBucket;
    private int _pos = 0;

    private final int _counts[];
    private final long _interval;
}
