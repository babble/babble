// UsageTracker.java

package ed.appserver;

import java.util.*;

import ed.js.*;
import ed.db.*;

public class UsageTracker {

    public UsageTracker( String base ){
        _base = base;
        _trackers.put( this , 0L );
    }

    public void hit( String name , long amount ){
        synchronized ( _lock ){
            Long value = _counts.get( name );
            if ( value == null )
                value = amount;
            else
                value += amount;
            _counts.put( name , value );
        }
        
    }

    protected void finalize(){
        flush();
    }
    
    public void flush(){
        synchronized ( _flushLock ){
            Map<String,Long> counts = _counts;
            
            synchronized ( _lock ){
                _counts = new TreeMap<String,Long>();
            }
            
            try {
                JSDate now = (new JSDate()).roundMinutes( 5 );
                DBCollection db = DBProvider.get( _base ).getCollection( "_system" );
                db = db.getCollection( "usage" );
                for ( String s : counts.keySet() ){
                    long val = counts.get( s );
                    if ( val <= 0 )
                        continue;
                    
                    DBCollection t = db.getCollection( s );
                    
                    JSObjectBase query = new JSObjectBase();
                    query.set( "ts" , now );
                    
                    t.update( query , inc( val ) , true , false );

		    if ( ! _ensuredIndexes ){
			JSObject keys = new JSObjectBase();
			keys.set( "ts" , 1 );
			t.ensureIndex( keys );
		    }
                }

		_ensuredIndexes = true;
            }
            catch ( Throwable t ){
                ed.log.Logger.getLogger( "UsageTracker" ).error( "couldn't flush [" + _base + "]" , t );
                for ( String s : counts.keySet() )
                    hit( s , counts.get( s ) );
            }

        }
    }
    
    final String _base;
    final String _lock = "LOCK" + Math.random();
    final String _flushLock = "FLOCK" + Math.random();
    private Map<String,Long> _counts = new TreeMap<String,Long>();
    private boolean _ensuredIndexes = false;

    static JSObject inc( long n ){

        JSObject num = new JSObjectBase();
        num.set( "num" , n );
        
        JSObject inc = new JSObjectBase();
        inc.set( "$inc" , num );
        
        return inc;
    }

    static Map<UsageTracker,Long> _trackers = Collections.synchronizedMap( new WeakHashMap<UsageTracker,Long>() );
    
    static final Thread _dumper;
    static {
        _dumper = new Thread( "UsageTracker-Dumper"){
                public void run(){
                    while ( true ){
                        
                        try {
                            List<UsageTracker> lst = new ArrayList<UsageTracker>( _trackers.keySet() );
                            for ( UsageTracker ut : lst ){
                                ut.flush();
                                _trackers.put( ut , System.currentTimeMillis() );
                            }
                        }
                        catch ( Throwable t ){
                            ed.log.Logger.getLogger( "UsageTracker" ).error( "while loop died" , t );
                        }

                        try {
                            Thread.sleep( 1000 * 20 );
                        }
                        catch ( Throwable t ){}
                    }
                }
            };
        _dumper.setDaemon( true );
        _dumper.start();
    }

}
