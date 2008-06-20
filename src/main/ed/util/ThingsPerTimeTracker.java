//ThingsPerTimeTracker.java

package ed.util;

public class ThingsPerTimeTracker {

    /**
     * @param interval bucket size.  so for per second, use 1000
     */
    public ThingsPerTimeTracker( long interval , int intervalsBack ){
        _interval = interval;
        _counts = new int[intervalsBack];
        _lastBucket = System.currentTimeMillis();
    }
    
    public void hit(){
        hit(1);
    }

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
    
    public int size(){
        return _counts.length;
    }

    /**
     * @param num
     *   0 = now
     *   MAX = farthest back
     */
    public int get( int num ){
        int p = _pos - num;
        if ( p < 0 )
            p += _counts.length;
        return _counts[ p ];
    }

    public int validate(){
        hit( 0 );
    }

    private long _lastBucket;
    private int _pos = 0;
    
    private final int _counts[];
    private final long _interval;
}

