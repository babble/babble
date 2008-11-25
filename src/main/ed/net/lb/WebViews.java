// WebViews.java

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

import java.util.*;
import java.text.*;

import ed.net.*;
import ed.net.httpserver.*;
import ed.appserver.*;

public class WebViews {
    
    static class LBOverview extends HttpMonitor {
        LBOverview( LB lb ){
            super( "lb" );
            _lb = lb;
        }
        
        public void handle( MonitorRequest mr ){
	    JxpWriter out = mr.getWriter();
	    mr.addHeader( "overview" );

	    out.print( "<div>" );
            _lb._loadMonitor._all.getShort().displayGraph( out );
	    out.print( "</div>" );

	    displayLast( mr , _lb , 15 );
            
            out.print( "<hr>" );
            
            out.print( "<table><tr>" );
            
            { // servers
                out.print( "<td valign='top' >" );
                
                out.print( "Servers" );
                out.print( "<ul>" );
                for ( Server s : sortedServers() ){
                    
                    String css = "";
                    
                    if ( s.inErrorState() )
                        css = "error";
                    else if ( s.timeSinceLastError() < 1000 * 60 * 5 )
                        css ="warn";
                    
                    out.print( "<li class='" + css + "'>" + s + "</li>" );
                }
                out.print( "<ul>" );
                
                out.print( "</td>" );
            }

            { // logs
                out.print( "<td>" );
                
                out.print( "logs<br>" );
                HttpMonitor.printLastLogMessages( mr , 10 );

                out.print( "</td>" );
            }

            out.print( "</tr></table>" );
        }

        List<Server> sortedServers(){
            List<Server> l = new ArrayList<Server>( _lb._router.getServers() );
            Collections.sort( l , _serverComparator );
            return l;
        }

        final LB _lb;
        final Comparator<Server> _serverComparator = new Comparator<Server>(){

            public int compare( Server a , Server b ){
                boolean aError = a.inErrorState();
                boolean bError = b.inErrorState();
                
                if ( aError ){
                    if ( bError )
                        return 0;
                    return -1;
                }

                if ( bError )
                    return 1;
                
                return (int)(a.timeSinceLastError() - b.timeSinceLastError());
            }
        };
    }

    static class MappingView extends HttpMonitor {

        MappingView( Router r ){
            super( "lb-mapping" , true );
            _router = r;
        }
        
        public void handle( MonitorRequest mr ){
	    JxpWriter out = mr.getWriter();
	    mr.addHeader( "mapping" );
            
            if ( mr.getRequest().getBoolean( "update" , false ) ){
                try {
                    _router.updateMapping();
                    out.print( "<b>updated mapping</b>" );
                }
                catch ( Exception e ){
                    out.print( "couldn't update : " + e );
                }
                out.print( "<hr>" );
            }

            out.print( "<a href='/~lb-mapping?update=t'>update</a>" );

	    out.print( "<pre>" );
            out.print( _router._mapping.toFileConfig() );
            out.print( "</pre>" );

        }
        
        final Router _router;
    }


    static class LBLast extends HttpMonitor {
        LBLast( LB lb ){
            super( "lb-last" );
            _lb = lb;
        }
        
        public void handle( MonitorRequest mr ){
	    displayLast( mr , _lb );
        }

        final LB _lb;
    }

    static class LoadMonitorWebView extends HttpMonitor {
        LoadMonitorWebView( LoadMonitor lm ){
            super( "lb-load" );
            _lm = lm;
        }
        
        public void handle( MonitorRequest request ){
            _lm._all.displayGraph( request.getWriter() );
        }
        
        final LoadMonitor _lm;
    }
    
    static class RouterPools extends HttpMonitor {
        RouterPools( Router r ){
            super( "lb-pools" );
            _router = r;
        }

        
        public void handle( MonitorRequest request ){
            JxpWriter out = request.getWriter();
            
            out.print( "<table>" );
            
            for ( String poolName : _router.getPoolNames() ){
                final Router.Pool p = _router.getPool( poolName );
                out.print( "<tr><th colspan='2'>" + poolName + "</th></tr>" );
                
                out.print( "<tr>" );
                
                out.print( "<td><ul>" );
                for ( Server server : p._servers ){
                    out.print( "<li>" );
                    out.print( server.toString() );
                    out.print( "</li>" );
                }
                out.print( "</ul></td>" );
                
                out.print( "<td>" );
                p._tracker.displayGraph( out );
                out.print( "</td>" );
                
                out.print( "</tr>" );
            }
            
            out.print( "</table>" );
        }

        final Router _router;
    }

    static class RouterServers extends HttpMonitor {
        RouterServers( Router r ){
            super( "lb-servers" );
            _router = r;
        }

        
        public void handle( MonitorRequest request ){
	    List<Server> servers = _router.getServers();
	    Collections.sort( servers );
	    
            JxpWriter out = request.getWriter();
	    
            out.print( "<table>" );
            
            for ( Server s : servers ){

                out.print( "<tr>" );
                out.print( "<td>" );
                s._tracker.displayGraph( out );
                out.print( "</td>" );
                out.print( "</tr>" );

                out.print( "<tr>" );
                request.addTableCell( "in error state:" + s._inErrorState );
                out.print( "</tr>" );

                out.print( "<tr><td colspan='2'><hr></td></tr>" );
            }
            
            out.print( "</table>" );
        }

        final Router _router;
    }

    static void displayLast( MonitorRequest mr , LB lb ){
	displayLast( mr , lb , lb._lastCalls.size() );
    }

    static void displayLast( MonitorRequest mr , LB lb , int num ){
	JxpWriter out = mr.getWriter();
	
	out.print( "<table border='1' >" );
        
	out.print( "<tr>" );
	out.print( "<th>Host</th>" );
	out.print( "<th>URL</th>" );
	out.print( "<th>Server</th>" );
	out.print( "<th>Started</th>" );

	out.print( "<th>state</th>" );
        
	out.print( "<th>Code</th>" );
	out.print( "<th>Lenghth</th>" );
	out.print( "<th>time</th>" );
        
	out.print( "</tr>\n" );
        
	for ( int i=0; i<num; i++ ){
	    LBCall call = lb._lastCalls.get(i);
            
	    if ( call == null )
		break;
	    
	    out.print( "<tr>" );
	    mr.addTableCell( call._request.getHost() );
            String url = call._request.getURL();
            if ( url.length() > 100 )
                url = url.substring( 0 , 100 ) + "...";
	    mr.addTableCell( url );
	    mr.addTableCell( call.lastWent() );
	    mr.addTableCell( SHORT_TIME.format( new Date( call.getStartedTime() ) ) );
            
            mr.addTableCell( call.getStateString() );
            
	    if ( call.isDone() ){
		int rc = call._response.getResponseCode();
		mr.addTableCell( rc , rc >= 500 ? "error" : null );
		mr.addTableCell( call._response.getContentLength() );
                
		long tt = call.getTotalTime();
		mr.addTableCell( tt , tt > 2000 ? "error" : ( tt > 300 ? "warn" : null ) );
	    }

	    out.print( "</tr>\n" );
	}
	out.print( "</table>" );
    }

    public static final SimpleDateFormat SHORT_TIME = new SimpleDateFormat( "MM/dd HH:mm:ss.S" );
}
