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
            mr.getWriter().print( "overview" );
        }
        
        final LB _lb;
    }


    static class LBLast extends HttpMonitor {
        LBLast( LB lb ){
            super( "lb-last" );
            _lb = lb;
        }
        
        public void handle( MonitorRequest mr ){
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
            
            for ( int i=0; i<_lb._lastCalls.length; i++ ){
                int pos = ( _lb._lastCallsPos - i ) - 1;
                if ( pos < 0 )
                    pos += 1000;
                
                LB.RR rr = _lb._lastCalls[pos];
                
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
            
            out.print( "<ul>" );
            
            for ( String s : _router.getPoolNames() ){
                out.print( "<li>" );
                out.print( s );
                
                out.print( "<ul>" );
                for ( Server server : _router.getPool( s )._servers ){
                    out.print( "<li>" );
                    out.print( server.toString() );
                    out.print( "</li>" );
                }
                out.print( "</ul>" );
                
                out.print( "</li>" );
            }
            
            out.print( "</ul>" );
        }

        final Router _router;
    }

    public static final SimpleDateFormat SHORT_TIME = new SimpleDateFormat( "MM/dd HH:mm:ss.S" );
}
