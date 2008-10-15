// WebViews.java

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
        }
        
        final LB _lb;
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

    static void displayLast( HttpMonitor.MonitorRequest mr , LB lb ){
	displayLast( mr , lb , lb._lastCalls.length );
    }

    static void displayLast( HttpMonitor.MonitorRequest mr , LB lb , int num ){
	JxpWriter out = mr.getWriter();
	
	out.print( "<table border='1' >" );
        
	out.print( "<tr>" );
	out.print( "<th>Host</th>" );
	out.print( "<th>URL</th>" );
	out.print( "<th>Server</th>" );
	out.print( "<th>Started</th>" );
        
	out.print( "<th>Code</th>" );
	out.print( "<th>Lenghth</th>" );
	out.print( "<th>time</th>" );
        
	out.print( "</tr>\n" );
        
	for ( int i=0; i<num; i++ ){
	    int pos = ( lb._lastCallsPos - i ) - 1;
	    if ( pos < 0 )
		pos += 1000;
	    
	    LB.RR rr = lb._lastCalls[pos];
            
	    if ( rr == null )
		break;
	    
	    out.print( "<tr>" );
	    mr.addTableCell( rr._request.getHost() );
	    mr.addTableCell( rr._request.getURL() );
	    mr.addTableCell( rr.lastWent() );
	    mr.addTableCell( SHORT_TIME.format( new Date( rr.getStartedTime() ) ) );
	    if ( rr.isDone() ){
		int rc = rr._response.getResponseCode();
		mr.addTableCell( rc , rc >= 500 ? "error" : null );
		mr.addTableCell( rr._response.getContentLength() );
                
		long tt = rr.getTotalTime();
		mr.addTableCell( tt , tt > 2000 ? "error" : ( tt > 300 ? "warn" : null ) );
	    }
	    out.print( "</tr>\n" );
	}
	out.print( "</table>" );
    }

    public static final SimpleDateFormat SHORT_TIME = new SimpleDateFormat( "MM/dd HH:mm:ss.S" );
}
