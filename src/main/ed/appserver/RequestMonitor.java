// RequestMonitor.java

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
import java.lang.ref.*;

import ed.util.*;
import ed.log.*;

class RequestMonitor extends Thread {

    public static final long MAX_MS = Config.get().getLong( "REQUEST.TIMEOUT.MAX" , 1000 * 60 * 5 );
    public static final long MIN_MS = Config.get().getLong( "REQUEST.TIMEOUT.MIN" , 1000 * 45 );
    
    public static final long SLEEP_TIME = 5000;

    public static final int MIN_CHECKS = (int)(MIN_MS / SLEEP_TIME);
    public static final int MAX_CHECKS = (int)(MAX_MS / SLEEP_TIME);

    static synchronized RequestMonitor getInstance(){
        if ( _instance == null )
            _instance = new RequestMonitor();
        return _instance;
    }

    private static RequestMonitor _instance;

    private RequestMonitor(){
        super( "RequestMonitor" );
        setDaemon( true );
        start();
    }


    // ----


    void watch( AppRequest request ){
        if ( request.canBeLong() )
            return;
        _watched.add( new Watched( request , Thread.currentThread() ) );
    }

    class Watched {
        Watched( AppRequest request , Thread thread ){
            _request = new WeakReference<AppRequest>( request );
            _thread = thread;
        }
        
        boolean done(){
            AppRequest request = _request.get();
            if ( request == null )
                return true;

            if ( request.isDone() )
                return true;
            
            return false;
        }

        boolean needToKill( long now ){

            final Thread.State state = _thread.getState();
            if ( state == Thread.State.BLOCKED || 
                 state == Thread.State.WAITING ){
                _bonuses++;
            }

            long elapsed = now - _start;

            if ( elapsed > MAX_MS )
                return true;
            
            if ( elapsed < MIN_MS )
                return false;
            
            elapsed = elapsed - ( _bonuses * SLEEP_TIME );
            if ( elapsed > MIN_MS )
                return true;

            return false;
        }

        void kill(){
            AppRequest request = _request.get();
            if ( request == null )
                return;            
            
            _logger.error( "killing : " + request );
            
            request.getScope().setToThrow( new AppServerError( "running too long " + ( System.currentTimeMillis() - _start ) + " ms" ) );
            _thread.interrupt();
        }

        private int _bonuses = 0;

        final WeakReference<AppRequest> _request;
        final Thread _thread;
        final long _start = System.currentTimeMillis();
    }

    public void run(){
        while ( true ){
            ThreadUtil.sleep( SLEEP_TIME );
            try {
                doPass();
            }
            catch ( Exception e ){
                _logger.error( "couldn't do a pass" , e );
            }
        }
    }
    
    private void doPass(){
        final long now = System.currentTimeMillis();
        
        for ( int i=0; i<_watched.size(); i++ ){

            final Watched w = _watched.get( i );
            
            if ( w.done() ){
                _watched.remove( i );
                i--;
                continue;
            }

            if ( ! w.needToKill( now ) )
                continue;

            // note: i am not removing it from the list on purpose.
            //       if it doesn't die for some reason i want to try again
            //       until it works
            w.kill();
        }
    }
    
    private final List<Watched> _watched = new Vector<Watched>();
    private final Logger _logger = Logger.getLogger( "requestmonitor" );
}
