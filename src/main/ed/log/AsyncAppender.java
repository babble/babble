// AsyncAppender.java

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
