// AsyncAppender.java

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

package ed.log;

import ed.js.*;

import java.util.*;
import java.util.concurrent.*;

public class AsyncAppender implements Appender {

    public AsyncAppender(){
        _runner = new Runner();
        _runner.start();
    }

    public void addAppender( Appender a ){
        _appenders.add( a );
    }

    public void append( Event e ){
        if ( ! _queue.offer( e ) )
            System.err.println( "aysync appender queue to full for logger : " + e._loggerName );
    }

    public class Runner extends Thread {
        Runner(){
            super( "AsyncAppender" );
            setDaemon( true );
        }

        public void run(){
            while ( true ){
                Event e = null;
                try {
                    e = _queue.take();
                }
                catch ( InterruptedException ie ){
                }
                   
                if ( e == null )
                    continue;

                for ( int i=0; i<_appenders.size(); i++ ){
                    Appender a = _appenders.get(i);
                    try {
                        a.append( e );
                    }
                    catch ( Exception err ){
                        err.printStackTrace();
                    }
                }
            }
        }
    };


    final BlockingQueue<Event> _queue = new ArrayBlockingQueue<Event>( 2000 );
    final List<Appender> _appenders = new ArrayList<Appender>();
    final Runner _runner;
}
