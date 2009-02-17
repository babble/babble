// WatchableRequestMonitor.java

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

package ed.lang;

import java.util.*;
import java.lang.ref.*;

import ed.util.*;
import ed.log.*;
import ed.appserver.*;

public class WatchableRequestMonitor extends Thread {

    public static final long SLEEP_TIME = 200;

    public static synchronized WatchableRequestMonitor getInstance(){
        if ( _instance == null )
            _instance = new WatchableRequestMonitor();
        return _instance;
    }

    private static WatchableRequestMonitor _instance;
    
    public WatchableRequestMonitor(){
        this( "REQUEST" );
    }
    
    public WatchableRequestMonitor( String configPrefix ){
        this( Config.get().getLong( configPrefix + ".TIMEOUT.MIN" , 1000 * 45 ) , 
              Config.get().getLong( configPrefix + ".TIMEOUT.MAX" , 1000 * 60 * 5 ) , 
              Config.get().getLong( configPrefix + ".MEMORY.WARN" , 1024 * 1024 * 10 )
              );
    }

    public WatchableRequestMonitor( int normalSeconds , int megabytes ){
        this( 1000 * normalSeconds , 1000 * 6 * normalSeconds , 1024 * 1024 * megabytes );
    }
    
    public WatchableRequestMonitor( long maxAllowedTime , long normalAllowedTime , long memoryWarn ){
        super( "WatchableRequestMonitor" );
        
        _maxAllowedTime = maxAllowedTime;
        _normalAllowedTime = normalAllowedTime;
        _memoryWarn = memoryWarn;
        
        setDaemon( true );
        start();
    }


    // ----


    public void watch( WatchableRequest request ){
        if ( request.canBeLong() )
            return;
        _watched.add( new Watched( request , Thread.currentThread() ) );
    }

    class Watched {
        Watched( WatchableRequest request , Thread thread ){
            _request = new WeakReference<WatchableRequest>( request );
            _thread = thread;
        }
        
        boolean done(){
            WatchableRequest request = _request.get();
            if ( request == null )
                return true;

            if ( request.isDone() )
                return true;
            
            return false;
        }

        boolean needToKill( long now ){
            WatchableRequest request = _request.get();
            if ( request == null )
                return false;
            
            return 
                runningTooLong( request , now ) || 
                usingTooMuchMemory( request , now );
        }
        
        boolean usingTooMuchMemory( WatchableRequest request , long now ){
            long elapsed = now - _start;
            if ( elapsed < 600 )
                return false;
            
            long size = request.approxSize();
            if ( size > _memoryWarn )
                _logger.warn( request.debugName() + " using " + size + " memory" );

            return false;
        }
        
        boolean runningTooLong( WatchableRequest request , long now ){
            
            final Thread.State state = _thread.getState();
            if ( state == Thread.State.BLOCKED || 
                 state == Thread.State.WAITING ){
                _bonuses++;
            }

            long elapsed = now - _start;

            if ( elapsed > _maxAllowedTime )
                return true;
            
            if ( elapsed < _normalAllowedTime )
                return false;
            
            elapsed = elapsed - ( _bonuses * SLEEP_TIME );
            if ( elapsed > _normalAllowedTime )
                return true;

            return false;
        }

        void kill(){
            WatchableRequest request = _request.get();
            if ( request == null )
                return;            
            
            if ( ! _killAttempted )
                _logger.error( "killing : " + request );

            _killAttempted = true;
            
            request.getScope().setToThrow( new AppServerError( "running too long " + ( System.currentTimeMillis() - _start ) + " ms" ) );
            _thread.interrupt();
        }

        private int _bonuses = 0;
        private boolean _killAttempted = false;

        final WeakReference<WatchableRequest> _request;
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

    private final long _maxAllowedTime;
    private final long _normalAllowedTime;
    private final long _memoryWarn;
}
