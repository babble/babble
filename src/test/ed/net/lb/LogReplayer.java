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

import ed.net.httpclient.*;
import ed.util.*;

public class LogReplayer {

    public LogReplayer( String server , InputStream log , double speedMultiplier )
        throws IOException {
        _server = server;
        _lines = LogLine.parse( log );
        _speedMultiplier = speedMultiplier;
        _client = new AsyncClient();
    }
    
    public void go()
        throws IOException {

        Date last = null;
        
        for ( LogLine ll : _lines ){
            
            final Date now = ll._when;
            final long sleep;
            
            if ( last != null ){
                long diff = now.getTime() - last.getTime();
                if ( diff < 0 )
                    diff = 0;

                sleep = (long)(diff / _speedMultiplier);
            }
            else
                sleep = 0;
            
            ThreadUtil.sleep( sleep );
            
            go( ll );
            last = now;

            clean();
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
        if ( ll._userAgent != null )
            buf.append( "User-Agent:" ).append( ll._userAgent ).append( "\n" );
        if ( ll._cookie != null )
            buf.append( "Cookie:" ).append( ll._cookie ).append( "\n" );
        
        _responses.add( _client.send( _server , "GET" , url , host , buf.toString() ) );
    }
    
    void clean(){
        
        for ( Iterator<AsyncClient.DelayedHttpResponse> i = _responses.iterator(); i.hasNext(); ){
            
            AsyncClient.DelayedHttpResponse res = i.next();

            if ( res.isDone() ){
                
                if ( res.getError() == null )
                    System.out.print( "." );
                else 
                    System.out.print( "x" );

                i.remove();
            }
        }
    }
    
    void waitTillDone()
        throws IOException {
        for ( AsyncClient.DelayedHttpResponse r : _responses ){
            r.finish();
            System.out.print( "." );
        }
    }
    
    final Iterable<LogLine> _lines;
    final String _server;
    final double  _speedMultiplier;

    List<AsyncClient.DelayedHttpResponse> _responses = new ArrayList<AsyncClient.DelayedHttpResponse>();
    
    final AsyncClient _client;
    
    public static void main( String args[] )
        throws IOException {
        
        if ( args.length < 2 ){
            System.out.println( "usage: ed.net.lb.LogReplayer <server> <path to log file> [multiplier]" );
            return;
        }

        String server = args[0];
        String logFile = args[1];

        double speedMultiplier = args.length < 3 ? 1 : Double.parseDouble( args[2] );
        
        LogReplayer lr = new LogReplayer( server , new FileInputStream( logFile ) , speedMultiplier );
        lr.go();

    }

}
