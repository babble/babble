// UsageTracker.java

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

package ed.appserver;

import java.util.*;

import ed.js.*;
import ed.db.*;

/** Tracks site traffic.
 * @expose
 */
public class UsageTracker {

    /** Creates a newly allocated UsageTracker for a site.
     * @param base Site name for which to create this usage tracker
     */
    public UsageTracker( AppContext context ){
        _context = context;
        _trackers.put( this , 0L );
    }

    /** Records adding a value to one of the _system collections.
     * _system collections include cpu_millis, bytes_in, bytes_out, and requests
     * @param name Name of the relevant collection
     * @param amount The number to add
     */
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

    /** @unexpose */
    protected void finalize(){
        flush();
    }

    /** Forces any usage information that has not been written to a collection yet to be saved.
     * @return If all usage information was successfully saved.
     */
    public boolean flush(){
        synchronized ( _flushLock ){
            DBBase theDB = _context.getDB();
            if ( theDB == null )
                return false;

            Map<String,Long> counts = _counts;

            synchronized ( _lock ){
                _counts = new TreeMap<String,Long>();
            }
            
            try {
                JSDate now = (new JSDate()).roundMinutes( 5 );
                DBCollection db = theDB.getCollection( "_system" );
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
                return true;
            }
            catch ( Throwable t ){
                ed.log.Logger.getLogger( "UsageTracker" ).error( "couldn't flush [" + _context.getName() + "] : " + t );
                for ( String s : counts.keySet() )
                    hit( s , counts.get( s ) );
                return false;
            }

        }
    }

    final AppContext _context;
    final String _lock = "LOCK" + Math.random();
    final String _flushLock = "FLOCK" + Math.random();
    private Map<String,Long> _counts = new TreeMap<String,Long>();
    private boolean _ensuredIndexes = false;

    /** Given a value <tt>n</tt>, sets up an object of the form { $inc : { num : n } }.  The outer
     * $inc will be stripped off when the object is saved to the database and only { num : n } will be saved
     * as a key/value pair of a usage stat.
     * @param n Usage value
     * @return The object.
     */
    static JSObject inc( long n ){

        JSObject num = new JSObjectBase();
        num.set( "num" , n );

        JSObject inc = new JSObjectBase();
        inc.set( "$inc" , num );

        return inc;
    }

    /** @unexpose */
    static Map<UsageTracker,Long> _trackers = Collections.synchronizedMap( new WeakHashMap<UsageTracker,Long>() );

    /** @unexpose */
    static final Thread _dumper;
    static {
        _dumper = new Thread( "UsageTracker-Dumper"){
                public void run(){
                    while ( true ){

                        boolean allFails = true;

                        try {
                            List<UsageTracker> lst = new ArrayList<UsageTracker>( _trackers.keySet() );
                            for ( UsageTracker ut : lst ){
                                if ( ut.flush() )
                                    allFails = false;
                                _trackers.put( ut , System.currentTimeMillis() );
                            }
                        }
                        catch ( Throwable t ){
                            ed.log.Logger.getLogger( "UsageTracker" ).error( "while loop died" , t );
                        }

                        try {
                            Thread.sleep( 1000 * 20 * ( allFails ? 30 : 1 ) );
                        }
                        catch ( Throwable t ){}
                    }
                }
            };
        _dumper.setDaemon( true );
        _dumper.start();
    }

}
