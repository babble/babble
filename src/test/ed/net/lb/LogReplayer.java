// LogReplayer.java

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

package ed.net.lb;

import java.io.*;
import java.util.*;

import ed.util.*;

public class LogReplayer {

    public LogReplayer( String server , InputStream log , int speedMultiplier )
        throws IOException {
        _server = server;
        _lines = LogLine.parse( log );
        _speedMultiplier = speedMultiplier;
    }
    
    public void go(){

        Date last = null;
        
        for ( LogLine ll : _lines ){
            
            final Date now = ll._when;
            final long sleep;
            
            if ( last != null ){
                long diff = now.getTime() - last.getTime();
                if ( diff < 0 )
                    diff = 0;

                sleep = diff / _speedMultiplier;
            }
            else
                sleep = 0;
            
            ThreadUtil.sleep( sleep );
            
            go( ll );
            last = now;
        }

        waitTillDone();
    }
    
    void go( LogLine ll ){

        String url = ll._url.replaceAll( "^https?://" , "" );
        int idx = url.indexOf( "/" );
        
        final String host;

        if ( idx < 0 ){
            host = url;
            url = "/";
        }
        else {
            host = url.substring( 0 , idx );
            url = url.substring( idx );
        }

        StringBuilder buf = new StringBuilder();
        buf.append( "GET " ).append( url ).append( " HTTP/1.0\n" );
        buf.append( "Host: " ).append( host ).append( "\n" );

        if ( ll._userAgent != null )
            buf.append( "User-Agent:" ).append( ll._userAgent ).append( "\n" );
        if ( ll._cookie != null )
            buf.append( "Cookie:" ).append( ll._cookie ).append( "\n" );

        System.out.println( ll._url + "\n" + buf );
    }
    
    void waitTillDone(){

    }

    final Iterable<LogLine> _lines;
    final String _server;
    final int _speedMultiplier;
    
    public static void main( String args[] )
        throws IOException {
        
        String server = args[0];
        String logFile = args[1];

        int speedMultiplier = args.length < 3 ? 1 : Integer.parseInt( args[2] );
        
        LogReplayer lr = new LogReplayer( server , new FileInputStream( logFile ) , speedMultiplier );
        lr.go();

    }

}