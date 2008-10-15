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
import ed.net.*;
import ed.cloud.*;
import ed.net.httpserver.*;

public class LB extends NIOClient {

    static final long WAIT_FOR_POOL_TIMEOUT = 10000;
    static final String LBIDENT = DNSUtil.getLocalHost().getHostName() + " : v0" ;

    enum State { WAITING , IN_HEADER , READY_TO_STREAM , STREAMING , ERROR , DONE };

    public LB( int port , MappingFactory mappingFactory , int verbose )
        throws IOException {
        super( "LB" , 15 , verbose );
        
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
        super.run();
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
    
    class RR extends Call {
        
        RR( HttpRequest req , HttpResponse res ){
            _request = req;
            _response = res;
            reset();

            _lastCalls[_lastCallsPos] = this;

            _lastCallsPos++;
            if ( _lastCallsPos >= _lastCalls.length )
                _lastCallsPos = 0;
        }
        
        void reset(){
            _response.clearHeaders();
            _response.setHeader( "X-lb" , LBIDENT );
            _state = State.WAITING;
            _line = null;
        }

        void success(){
            done();
            _router.success( _request , _response , _lastWent );
	    _loadMonitor.hit( _request , _response );
        }

        public void done(){
            super.done();
            log( this );
        }
        
        protected InetSocketAddress where(){
            _lastWent = _router.chooseAddress( _request , _request.elapsed() > WAIT_FOR_POOL_TIMEOUT );
            return _lastWent;
        }
        
        protected InetSocketAddress lastWent(){
            return _lastWent;
        }
        
        protected void error( ServerErrorType type , Exception e ){
            _logger.debug( 1 , "backend error" , e );
	    _loadMonitor._all.networkEvent();
            _router.error( _request , _response , _lastWent , type , e );
	    
            if ( ! _response.isCommitted() && 
		 type != ServerErrorType.WEIRD && 
                 ( _state == State.WAITING || _state == State.IN_HEADER ) && 
                 ++_numFails <= 3 ){
                reset();
                _logger.debug( 1 , "retrying" );
                add( this );
                return;
            }
            
            try {
		if ( ! _response.isCommitted() ){
		    _response.setResponseCode( 500 );
		    _response.getJxpWriter().print( "backend error : " + e + "\n\n" );
		}
                _response.done();
                _state = State.ERROR;
                done();
		_loadMonitor.hit( _request , _response );
            }
            catch ( IOException ioe2 ){
                ioe2.printStackTrace();
            }
        }
	
        
        void backendError( ServerErrorType type , String msg ){
            backendError( type , new IOException( msg ) );
        }
        
        void backendError( ServerErrorType type , IOException ioe ){
            _logger.debug( 1 , "backend error" , ioe );
            error( type , ioe );
        }

        protected WhatToDo handleRead( ByteBuffer buf , Connection conn ){

            _logger.debug( 3 , "handleRead  _state:" + _state );
            
            if ( _state == State.WAITING || _state == State.IN_HEADER ){
                // TODO: should i read this all in at once                
                if ( _line == null )
                    _line = new StringBuilder();
                while ( buf.hasRemaining() ){
                    char c = (char)buf.get();
                    if ( c == '\r' )
                        continue;
                    if ( c == '\n' ){
                        if ( _line.length() == 0 ){
                            _logger.debug( 3 , "end of header" );
                            _state = State.READY_TO_STREAM;
                            break;
                        }

                        String line = _line.toString();                        

                        if ( _state == State.WAITING ){
                            int idx = line.indexOf( " " );
                            if ( idx < 0 ){
                                backendError( ServerErrorType.INVALID , "invalid first line [" + line + "]" );
                                return WhatToDo.ERROR;
                            }
                            line = line.substring( idx + 1 ).trim();
                            idx = line.indexOf( " " );
                            if ( idx < 0 )
                                _response.setResponseCode( Integer.parseInt( line ) );
                            else
                                _response.setStatus( Integer.parseInt( line.substring( 0 , idx ) ) , line.substring( idx + 1 ).trim() );
                            
                            _logger.debug( 3 , "got first line " , line );
                            _state = State.IN_HEADER;
                        }
                        else {
                            int idx = line.indexOf( ":" );
                            if ( idx < 0 ){
                                backendError( ServerErrorType.INVALID , "invalid line [ " + line + "]" );
                                return WhatToDo.ERROR;
                            }
                            String name = line.substring( 0 , idx );
                            String value = line.substring( idx + 1 ).trim();
                            _logger.debug( 3 , "got header line " , line );
                            
                            if ( name.equalsIgnoreCase( "Connection" ) ){
                                _keepalive = ! value.toLowerCase().contains( "close" );
                            }
                            else {
                                _response.addHeader( name , value );
                            }

                        }
                        _line.setLength( 0 );
                        continue;
                    }
                    _line.append( c );
                }
                
                if ( _state != State.READY_TO_STREAM )
                    return WhatToDo.CONTINUE;

                _logger.debug( 3 , "starting to stream data" );
            }
            
            if ( _state == State.READY_TO_STREAM ){
                MyChunk chunk = new MyChunk( this , conn , _response.getContentLength() , buf );
                _response.sendFile( new MySender( chunk ) );
                _state = State.STREAMING;
            }
            
            try {
                _response.done();
            }
            catch ( IOException ioe ){
                _logger.debug( 2 , "client error" , ioe );
                return WhatToDo.ERROR;
            }
            return WhatToDo.PAUSE;
        }
        
        protected ByteStream fillInRequest( ByteBuffer buf ){
            buf.put( generateRequestHeader().getBytes() );
	    if ( _request.getPostData() != null )
		return _request.getPostData().getByteStream();
	    return null;
        }

        String generateRequestHeader(){
            StringBuilder buf = new StringBuilder( _request.getRawHeader().length() + 200 );
            buf.append( _request.getMethod().toUpperCase() ).append( " " ).append( _request.getURL() ).append( " HTTP/1.0\r\n" );
            buf.append( "Connection: keep-alive\r\n" );
	    buf.append( HttpRequest.REAL_IP_HEADER ).append( ": " ).append( _request.getRemoteIP() ).append( "\r\n" );
            buf.append( "X-fromlb: " ).append( LBIDENT ).append( "\r\n" );

            for ( String n : _request.getHeaderNameKeySet() ){

		if ( n.equalsIgnoreCase( "Connection" ) || 
		     n.equalsIgnoreCase( HttpRequest.REAL_IP_HEADER ) )
		    continue;
		
                String v = _request.getHeader( n );
                buf.append( n ).append( ": " ).append( v ).append( "\r\n" );
            }
            buf.append( "\r\n" );

            _logger.debug( 3 , "request\n" , buf );
            
            return buf.toString();
        }
        
	public String toString(){
	    return _request.getFullURL();
	}

        final HttpRequest _request;
        final HttpResponse _response;
        
        int _numFails = 0;

        private InetSocketAddress _lastWent;

        private boolean _keepalive;
        private State _state;
        private StringBuilder _line;
    }

    class MySender extends JSFile.Sender {
        MySender( MyChunk chunk ){
            super( chunk , chunk._length );
        }
    }

    class MyChunk extends JSFileChunk {
        MyChunk( RR rr , Connection conn , long length , ByteBuffer buf ){
            _rr = rr;
            _conn = conn;
            _length = length;
            _buf = buf;
            
            _data = new MyBinaryData( _buf );
            _sent += _data.length();
        }
        
        public JSBinaryData getData(){
            return _data;
        }
        
        public MyChunk getNext(){
            _logger.debug( 4, "want next chunk of data" );
            if ( _sent == _length ){
                _logger.debug( 4 , "no more data" );
                _conn.done( ! _rr._keepalive );
                _rr._state = State.DONE;
                _rr.success();
                return null;
            }
            
            _conn.doRead( _length > 0 );
            _sent += _data.length();            
            _last = _data.length();
            _logger.debug( _last == 0 ? 4 : 3 , "sent " + _sent + "/" + _length );
            return this;
        }
        
        long _sent = 0;
        long _last = -1;
        
        final RR _rr;
        final Connection _conn;
        final long _length;
        final ByteBuffer _buf;
        final MyBinaryData _data;
    }

    class MyBinaryData extends JSBinaryData {
        MyBinaryData( ByteBuffer buf ){
            _buf = buf;
        }
        
        public int length(){
            return _buf.remaining();
        }

        public void put( ByteBuffer buf ){
            throw new RuntimeException( "not allowed" );
        }
        
        public void write( OutputStream out ){
            throw new RuntimeException( "not allowed" );
        }

        public ByteBuffer asByteBuffer(){
            return _buf;
        }

        final ByteBuffer _buf;
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
	    
            if ( add( new RR( request , response ) ) )
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
    
    void log( RR rr ){
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
            RR rr = e._rr;
            
            _buf.append( "[" ).append( e.getDate().format( _format ) ).append( "] " );
            _buf.append( rr._request.getRemoteIP() );
            _buf.append( " retry:" ).append( rr._numFails );
            _buf.append( " went:" ).append( rr._lastWent );
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
        HttpEvent( RR rr ){
            super( null , rr._request.getStart() , null , null , null , null );
            _rr = rr;
        }
        
        final RR _rr;
    }

    final int _port;
    final LBHandler _handler;
    final HttpServer _server;
    final Router _router;
    final LoadMonitor _loadMonitor;
    
    final Logger _logger;
    final AsyncAppender _httpLog;
    
    final RR[] _lastCalls = new RR[1000];
    int _lastCallsPos = 0;

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
