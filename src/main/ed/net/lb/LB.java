// LB.java

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
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;

import org.apache.commons.cli.*;

import ed.io.*;
import ed.js.*;
import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.net.nioclient.*;
import ed.cloud.*;
import ed.net.httpserver.*;
import static ed.net.lb.Mapping.*;

public class LB extends NIOClient {

    static final String LBIDENT = DNSUtil.getLocalHost().getHostName() + " : v0" ;

    enum State { WAITING , IN_HEADER , READY_TO_STREAM , STREAMING , ERROR , DONE };

    public LB( int port , MappingFactory mappingFactory , int verbose )
        throws IOException {
        super( "LB" , 2000 , verbose );
        
        _port = port;
        _handler = new LBHandler();
        
        _logger = Logger.getLogger( "LB" );
	_logger.setLevel( Level.forDebugId( verbose ) );

        _server = new HttpServer( port );
        _server.addGlobalHandler( _handler ) ;
        
        _router = new Router( mappingFactory );
	_loadMonitor = new LoadMonitor( _router );

        _addMonitors();
        
        _httpLog = new AsyncAppender();
        _httpLog.addAppender( new DailyFileAppender( new File( "logs/httplog/" ) , "lb" , new HttpLogEventFormatter() ) );

        setDaemon( true );
    }
    
    public void start(){
        _server.start();
        super.start();
    }
    
    public void run(){
        _logger.debug( "Started" );
        try {
            super.run();
        }
        catch ( Throwable t ){
            try {
                System.err.println( "LB run got error" );
                t.printStackTrace();
            }
            catch ( Throwable t2 ){}
            System.exit(-1);
        }
    }


    protected void shutdown(){
        _logger.error( "SHUTDOWN starting" );
        _server.stopServer();
        super.shutdown();
        Thread t = new Thread(){
                public void run(){
                    try {
                        Thread.sleep( 1000 * 60 );
                    }
                    catch ( Exception e ){}
                    System.exit(0);
                }
            };
        t.start();
    }
    
    protected void serverError( InetSocketAddress addr , ServerErrorType type , Exception why ){
        // TODO: uh oh
        _logger.debug( 1 , "serverError" , addr + ":" + type );
    }
    

    class LBHandler implements HttpHandler {
        public boolean handles( HttpRequest request , Info info ){
            info.admin = false;
            info.fork = false;
            info.doneAfterHandles = false;
            return true;
        }
        
        public boolean handle( HttpRequest request , HttpResponse response ){
	    
	    if ( request.getHeader( "X-fromlb" ) != null ){
		_handleError( request , response , 500 , "load balancer loop?  (lb 34)\n" + request.getFullURL() + "\n" + 
                              "physical address:" + request.getPhysicalRemoteAddr() + "\n" +
                              "remote address:" + request.getRemoteIP() );
		return true;
	    }
	    
            if ( _router.reject( request ) ){
                _handleError( request , response , 403 , "request rejected" );
                return true;
            }

            if ( add( new LBCall( LB.this , request , response ) ) )
                return false;
	    
	    _handleError( request , response , 500 , "new request queue full  (lb 1)" );
            return true;
        }

	private void _handleError( HttpRequest request , HttpResponse response , int code , String msg ){
	    response.setResponseCode( code );
            JxpWriter out = response.getJxpWriter();
	    out.print( msg );
	    out.print( "\n" );
            try {
                response.done();
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
            }	    
	}
        
        public double priority(){
            return Double.MAX_VALUE;
        }
    }
    
    void log( LBCall rr ){
        _httpLog.append( new HttpEvent( rr ) );
    }

    void _addMonitors(){
        HttpServer.addGlobalHandler( new WebViews.LBOverview( this ) );
        HttpServer.addGlobalHandler( new WebViews.LBLast( this ) );
	
        HttpServer.addGlobalHandler( new WebViews.LoadMonitorWebView( this._loadMonitor ) );
        
        HttpServer.addGlobalHandler( new WebViews.RouterPools( this._router ) );
        HttpServer.addGlobalHandler( new WebViews.RouterServers( this._router ) );
        HttpServer.addGlobalHandler( new WebViews.MappingView( this._router ) );


        HttpServer.addGlobalHandler( new HttpHandler(){
                public boolean handles( HttpRequest request , Info info ){
                    if ( ! "/~kill".equals( request.getURI() ) )
                        return false;
                    
                    if ( ! "127.0.0.1".equals( request.getPhysicalRemoteAddr() ) )
                        return false;
                
                    info.fork = false;
                    info.admin = true;
    
                    return true;
                }
                
                public boolean handle( HttpRequest request , HttpResponse response ){
                    JxpWriter out = response.getJxpWriter();
                    if ( isShutDown() ){
                        out.print( "alredy shutdown" );
                        return true;
                    }
                    LB.this.shutdown();
                    out.print( "done" );
                    return true;
                }
                
                public double priority(){
                    return Double.MIN_VALUE;
                }
            } 
            );
    }
    
    class HttpLogEventFormatter implements EventFormatter {
        
        public synchronized String format( Event old ){
            HttpEvent e = (HttpEvent)old;
            LBCall rr = e._rr;
            
            _buf.append( "[" ).append( e.getDate().format( _format ) ).append( "] " );
            _buf.append( rr._request.getRemoteIP() );
            _buf.append( " retry:" ).append( rr._numFails );
            _buf.append( " went:" ).append( rr.lastWent() );
            _buf.append( " " ).append( rr._response.getResponseCode() );
            _buf.append( " " ).append( rr._request.getMethod() );
            _buf.append( " \"" ).append( rr._request.getFullURL() ).append( "\"" );
            _buf.append( " " ).append( rr._response.handleTime() );
            _buf.append( " \"" ).append( rr._request.getHeader( "User-Agent" ) ).append( "\"" );
            _buf.append( " \"" ).append( rr._request.getHeader( "Referer" ) ).append( "\"" );
            _buf.append( " \"" ).append( rr._request.getHeader( "Cookie" ) ).append( "\"" );
            _buf.append( "\n" );

            String s = _buf.toString();
            if ( _buf.length() > _bufSize )
                _buf = new StringBuilder( _bufSize );
            else
                _buf.setLength( 0 );
            return s;
        }
        
        final int _bufSize = 1024;
        StringBuilder _buf = new StringBuilder( _bufSize );
        final SimpleDateFormat _format = new SimpleDateFormat( "MM/dd/yyyy hh:mm:ss.SSS z" );
    }
    
    class HttpEvent extends Event {
        HttpEvent( LBCall rr ){
            super( null , rr._request.getStart() , null , null , null , null );
            _rr = rr;
        }
        
        final LBCall _rr;
    }

    final int _port;
    final LBHandler _handler;
    final HttpServer _server;
    final Router _router;
    final LoadMonitor _loadMonitor;
    
    final Logger _logger;
    final AsyncAppender _httpLog;
    
    final CircularList<LBCall> _lastCalls = new CircularList<LBCall>( 1000 , true );

    public static void main( String args[] )
        throws Exception {

        int verbose = 0;

        for ( int i=0; i<args.length; i++ ){
            if ( args[i].matches( "\\-v+" ) ){
                verbose = args[i].length() - 1;
                args[i] = "";
            }
        }

        Options o = new Options();
        o.addOption( "p" , "port" , true , "Port to Listen On" );
        o.addOption( "v" , "verbose" , false , "Verbose" );
        o.addOption( "mapfile" , "m" , true , "file from which to load mappings" );

        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        final int port = Integer.parseInt( cl.getOptionValue( "p" , "8080" ) );
        
        MappingFactory mf = null;
        if ( cl.hasOption( "m" ) )
            mf = new TextMapping.Factory( cl.getOptionValue( "m" , null ) );
        else 
            mf = new GridMapping.Factory();

        System.out.println( "10gen load balancer" );
        System.out.println( "\t port \t " + port  );
        System.out.println( "\t verbose \t " + verbose  );
        
        int retriesLeft = 2;
        
        LB lb = null;
        
        while ( retriesLeft-- > 0 ){
            
            try {
                lb = new LB( port , mf , verbose );
                break;
            }
            catch ( BindException be ){
                be.printStackTrace();
                System.out.println( "can't bind to port.  going to try to kill old one" );
                HttpDownload.downloadToJS( new URL( "http://127.0.0.1:" + port + "/~kill" ) );
                Thread.sleep( 100 );
            }
        }
        
        if ( lb == null ){
            System.err.println( "couldn't ever bind" );
            System.exit(-1);
            return;
        }
        
        lb.start();
        lb.join();
        System.exit(0);
    }
}
