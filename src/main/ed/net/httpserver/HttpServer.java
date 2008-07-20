// HttpServer

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

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.io.*;
import ed.net.*;
import ed.log.*;
import ed.util.*;
import ed.appserver.*;

public class HttpServer extends NIOServer {

    static final int WORKER_THREADS = 30;
    static final int ADMIN_WORKER_THREADS = 10;

    static final int WORKER_THREAD_QUEUE_MAX = 200;
    static final int ADMIN_THREAD_QUEUE_MAX = 10;

    static final boolean D = Boolean.getBoolean( "DEBUG.HTTP" );
    static final Logger LOGGER = Logger.getLogger( "httpserver" );
    
    public HttpServer( int port )
        throws IOException {
        super( port );
    }
    
    protected HttpSocketHandler accept( SocketChannel sc ){
        return new HttpSocketHandler( this , sc );
    }
    
    protected boolean handle( HttpRequest request , HttpResponse response )
        throws IOException {
        
        _numRequests++;
        _reqPerSecTracker.hit();
        _reqPerSmallTracker.hit();
        _reqPerMinTracker.hit();

        HttpHandler.Info info = new HttpHandler.Info();
        for ( int i=0; i<_handlers.size(); i++ ){
            info.reset();
            if ( _handlers.get( i ).handles( request , info ) ){
                request._handler.pause();
                if ( info.fork ){
                    
                    WorkerThreadPool tp = info.admin ? _forkThreadsAdmin : _forkThreads;
                    
                    if ( info.fork ) _numRequestsForked++;
                    if ( info.admin ) _numRequestsAdmin++;

                    if ( tp.offer( new Task( request , response , _handlers.get( i ) ) ) ){
                        if ( D ) System.out.println( "successfully gave thing to a forked thing" );
                        return false;
                    }
                    
                    response.setResponseCode( 500 );
                    response.getWriter().print( "no more threads (appsrv h612)\n" );
                    response.done();
                    return false;
                }
                _handlers.get( i ).handle( request , response );
                response.done();
                return false;
            }
        }
        response.setResponseCode( 404 );
        response.getWriter().print( "no HTTP handlers available for : " + request.getURL() );
        response.done();
        
        return false;
    }
    
    class HttpSocketHandler extends SocketHandler {
        HttpSocketHandler( HttpServer server , SocketChannel sc ){
            super( sc );
            _server = server;
        }
        
        protected boolean shouldClose()
            throws IOException {
            writeMoreIfWant();
            return _done;
        }
        
        protected void writeMoreIfWant()
            throws IOException {
            if ( _lastResponse == null )
                return;
            
            if ( ! _lastResponse.done() )
                return;
            
            _lastRequest = null;
            _lastResponse = null;

            if ( _in.position() > 0 )
                gotData( null );

        }

        boolean hasData(){
            return _in.position() > 0;
        }
        
        protected boolean gotData( ByteBuffer inBuf )
            throws IOException {
            try {
                return _gotData( inBuf );
            }
            catch ( IOException ioe ){
                throw ioe;
            }
            catch ( RuntimeException re ){
                LOGGER.error( "_gotData error" , re );
                
                if ( _lastRequest == null )
                    throw re;
                
                if ( _lastResponse == null )
                    _lastResponse = new HttpResponse( _lastRequest );

                _lastResponse.setResponseCode( 500 );
                _lastResponse.getWriter().print( "error : " + re + "\n\n" );
                _lastResponse.done();
            }
            
            return false;
        }
        
        protected boolean _gotData( ByteBuffer inBuf )
            throws IOException {
            
            if ( inBuf != null ){

		if ( inBuf.capacity() > 1024 * 1024 * 500 )
		    throw new RuntimeException( "way too big" );

                if ( inBuf.remaining() > _in.remaining() )
                    throw new RuntimeException( "problem" );
                
                _in.put( inBuf );
            }
            if ( D ) System.out.println( _in );
            
            // this gets called when the header is parsed, but we may have content
            if ( _lastRequest != null && _lastResponse == null ){
                int end = _endOfHeader;
                end++;
                                
                final int cl = _lastRequest.getIntHeader( "Content-Length" , 0 );
                PostData pd = null;

                if ( cl > 0 ){
                    if ( _lastRequest._postData == null )
                        _lastRequest._postData = PostData.create( _lastRequest );
                    
                    pd = _lastRequest._postData;
                    
                    while ( end < _in.position() && ! pd.done() )
                        pd.put( _in.get( end++ ) );
                    
                    if ( ! pd.done() ){
                        _in.position( _endOfHeader + 1 );
                        return false;
                    }
                }
                
                // move anything extraneous to the front for pipelining, etc...
                int np = 0;
                while ( end < _in.position() ){
                    _in.put( np++ , _in.get( end ) );
                    end++;
                }
                
                _in.position( np );
                _lastResponse = new HttpResponse( _lastRequest );
                return handle( _lastRequest , _lastResponse );
            }

            int end = endOfHeader( _in , 0 );
            boolean finishedHeader = end >= 0;
            
            if ( ! finishedHeader ){
                if ( _in.position() > 16000 ){
                    throw new RuntimeException( "don't have a header and buffer is already " + _in.position() + " bytes" );
                }
                registerForReads();
                return false;
            }
            
            byte bb[] = new byte[end];
            for ( int i=0; i<bb.length; i++ )
                bb[i] = _in.get( i );
            
            _endOfHeader = end;
            String header = new String( bb );
            
            _lastRequest = new HttpRequest( this , header );
            _lastResponse = null;
            if ( D ) System.out.println( _lastRequest );
            
            return gotData( null );
        }

        int endOfHeader( ByteBufferHolder buf , final int start ){

            boolean lastWasNewLine = false;

            final int dataEnd = buf.position();
            int end = start;
            for ( ; end < dataEnd ; end++ ){
                byte b = _in.get(end);
                if ( b == '\r' )
                    continue;
                
                if ( b == '\n' ){
                    if ( lastWasNewLine ){
                        if ( D ) System.err.println( "got end of header! " );
                        return end;
                    }
                    lastWasNewLine = true;
                }
                else
                    lastWasNewLine = false;
            }       
            return -1;
        }
        
        protected SocketChannel getChannel(){
            return _channel;
        }

        protected Selector getSelector(){
            return _selector;
        }

        final HttpServer _server;
        ByteBufferHolder _in = new ByteBufferHolder( 1024 * 1024 * 200 ); // 200 mb
        int _endOfHeader = 0;
        boolean _done = false;
        HttpRequest _lastRequest;
        HttpResponse _lastResponse;
    }

    class Task {
        Task( HttpRequest req , HttpResponse res , HttpHandler han ){
            _request = req;
            _response = res;
            _handler = han;
        }
        
        final HttpRequest _request;
        final HttpResponse _response;
        final HttpHandler _handler;
    }

    private final WorkerThreadPool _forkThreads = new WorkerThreadPool( "main" , WORKER_THREADS , WORKER_THREAD_QUEUE_MAX );
    private final WorkerThreadPool _forkThreadsAdmin = new WorkerThreadPool( "admin" , ADMIN_WORKER_THREADS , ADMIN_THREAD_QUEUE_MAX );
    
    static class WorkerThreadPool extends ThreadPool<Task> {

        WorkerThreadPool( String name , int num , int maxQueue ){
            super( "HttpServer-" + name , num , maxQueue );
        }
        
        public void handle( Task t ) throws IOException {
            t._handler.handle( t._request , t._response );
	    try {
		t._response.done();
	    }
	    catch ( IOException ioe ){
		if ( D ) ioe.printStackTrace();
		t._response.cleanup();
	    }
        }
        
        public void handleError( Task t , Exception e ){
            Logger l = LOGGER;
            if ( t._request._attachment instanceof AppRequest )
                l = ((AppRequest)t._request._attachment).getLogger();
            
            l.error( "error handling a task" , e );
            try {
                t._response.done();
            }
            catch ( IOException ioe ){
                if ( D ) ioe.printStackTrace();
            }
        }

    };

    public static void addGlobalHandler( HttpHandler h ){
        _handlers.add( h );
        Collections.sort( _handlers , new Comparator<HttpHandler>(){
                public int compare( HttpHandler a , HttpHandler b ){
                    return a.priority() > b.priority() ? 1 : -1;
                }
            } 
            );
        
    }

    static final HttpHandler _stats = new HttpMonitor( "stats" ){

            public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
                out.print( "stats\n" );
                out.print( "---\n" );
                
                final int forkedQueueSize = request._handler._server._forkThreads.queueSize();

                out.print( "forked queue length : " + forkedQueueSize  + "\n" );
                out.print( "admin queue length : " + request._handler._server._forkThreadsAdmin.queueSize()  + "\n" );

                if ( forkedQueueSize >= WORKER_THREAD_QUEUE_MAX )
                    response.setResponseCode( 510 );

                out.print( "\n" );
                
                out.print( "numRequests : " + _numRequests + "\n" );
                out.print( "numRequestsForked : " + _numRequestsForked + "\n" );
                out.print( "numRequestsAdmin : " + _numRequestsAdmin + "\n" );
                
                out.print( "\n" );
                
                out.print( "uptime : " + ed.js.JSMath.sigFig( ( System.currentTimeMillis() - _startTime ) / ( 1000 * 60.0 ) ) + " min\n" );
                
                out.print( "\n" );
                
                _printTracker( "Request Per Second" , _reqPerSecTracker , out );
                _printTracker( "Request Per " + _trackerSmall + " Seconds" , _reqPerSmallTracker , out );
                _printTracker( "Request Per Minute" , _reqPerMinTracker , out );

            }

            
            void _printTracker( String name , ThingsPerTimeTracker tracker , JxpWriter out ){

                tracker.validate();

                out.print( name + "\n" );
                for ( int i=0; i<tracker.size(); i++ ){
                    out.print( tracker.get( i ) );
                    out.print( " " );
                }
                
                out.print( "\n" );
            }
            
            final long _startTime = System.currentTimeMillis();
        };

    static List<HttpHandler> _handlers = new ArrayList<HttpHandler>();
    
    static {
        DummyHttpHandler.setup();
        addGlobalHandler( _stats );
        addGlobalHandler( new HttpMonitor.MemMonitor() );
        addGlobalHandler( new HttpMonitor.ThreadMonitor() );
        addGlobalHandler( new HttpMonitor.FavIconHack() );
    }
    
    private static int _numRequests = 0;
    private static int _numRequestsForked = 0;
    private static int _numRequestsAdmin = 0;

    private static final int _trackerSmall = 10;

    private static ThingsPerTimeTracker _reqPerSecTracker = new ThingsPerTimeTracker( 1000  , 30 );
    private static ThingsPerTimeTracker _reqPerSmallTracker = new ThingsPerTimeTracker( 1000 * 10 , 30 );
    private static ThingsPerTimeTracker _reqPerMinTracker = new ThingsPerTimeTracker( 1000 * 60 , 30 );
    

    // ---

    public static void main( String args[] )
        throws Exception {
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }
    
}
