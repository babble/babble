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
import ed.net.nioserver.*;
import ed.log.*;
import ed.util.*;
import ed.appserver.*;

public class HttpServer extends NIOServer {

    public static final int WORKER_THREADS = 30;
    public static final int ADMIN_WORKER_THREADS = 10;
    
    public static final int WORKER_THREAD_QUEUE_MAX = 200;
    public static final int ADMIN_THREAD_QUEUE_MAX = 10;

    public final static long CLIENT_TIMEOUT = 1000 * 40;
    
    public static final boolean D = Boolean.getBoolean( "DEBUG.HTTP" );
    public static final Logger LOGGER = Logger.getLogger( "httpserver" );
    
    static final RollingNamedPipe _requestPipe = new RollingNamedPipe( "http-request" );
    static { _requestPipe.setMessageDivider( "\n" ); } 
    
    public HttpServer( int port )
        throws IOException {
        super( port );

        _listentingPorts.add( port );

        addHandler( new Stats() );
        addHandler( getSelectorMonitor() );
        addHandler( new MySelectorMonitor() );

    }
    
    protected HttpSocketHandler accept( SocketChannel sc ){
        _numTotalConnections++;
        return new HttpSocketHandler( this , sc );
    }
    
    protected boolean handle( HttpSocketHandler handler , HttpRequest request , HttpResponse response )
        throws IOException {
        
        _numRequests++;
        _tracker.hit( null , null );
        _requestPipe.write( request.getFullURL() );

        HttpHandler.Info info = new HttpHandler.Info();
        List<HttpHandler> handlers = getHandlers();
        for ( int i=0; i<handlers.size(); i++ ){
            HttpHandler h = handlers.get(i);
            info.reset();
            if ( h.handles( request , info ) ){
                if ( info.admin ) _numRequestsAdmin++;

                if ( info.fork ){
                    _numRequestsForked++;
                    request._handler.pause( "pause-fork" );

                    WorkerThreadPool tp = info.admin ? _forkThreadsAdmin : _forkThreads;

                    if ( tp.offer( new Task( request , response , h ) ) ){
                        if ( D ) System.out.println( "successfully gave thing to a forked thing" );
                        handler._inFork = true;
                        return false;
                    }
                    
                    response.setResponseCode( 503 );
                    response.getJxpWriter().print( "no more threads (appsrv h612)\n" );
                    response.done();
                    return false;
                }
                if ( ! h.handle( request , response ) )
                    handler._inFork = true;
                if ( info.doneAfterHandles )
                    response.done();
                return false;
            }
        }
        response.setResponseCode( 404 );
        response.getJxpWriter().print( "no HTTP handlers available for : " + request.getURL() );
        response.done();
        
        return false;
    }
    
    class HttpSocketHandler extends SocketHandler {
        HttpSocketHandler( HttpServer server , SocketChannel sc ){
            super( server , sc );
            _server = server;
        }
        
        protected boolean shouldTimeout( long now ){
            if ( super.shouldTimeout( now ) )
                return true;
            
            if ( _inFork )
                return false;
            
            if ( _lastResponse != null )
                return false;

            long timeSinceLastRead = System.currentTimeMillis() - lastActionTime();
            return timeSinceLastRead > CLIENT_TIMEOUT;
        }
        
        protected boolean shouldClose()
            throws IOException {

            if ( _bad )
                return true;
            
            if ( _inFork )
                return false;
            
            if ( _lastResponse == null ){
                if ( _done )
                    return true;
                
                return false;
            }
            
            writeMoreIfWant();
            return false;
        }
        
        protected boolean writeMoreIfWant()
            throws IOException {
            
            if ( _bad )
                return false;
            
            if ( _inFork )
                return false;
            
	    if ( _lastRequest != null && _lastResponse == null )
		return false;

            if ( _lastResponse != null && ! _lastResponse.done() )
                return false;
	    
	    if ( D ) System.out.println( "writeMoreIfWant removing request/response" );

            _lastRequest = null;
            _lastResponse = null;

            if ( _in.position() == 0 )
                return false;

            pause();
            gotData( null );
            return true;
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
            catch ( HttpExceptions.BadRequest br ){
                _handleError( br.getResponseCode() , br.getBodyContent() , br );
            }
            catch ( RuntimeException re ){
                LOGGER.error( "_gotData error" , re );
                _handleError( 500 , re.toString() , re );
            }
            
            return false;
        }
        
        private void _handleError( int errorCode , String msg , RuntimeException exception )
            throws IOException {
            if ( _lastRequest == null ){
                LOGGER.error( "in handleError and no _lastRequest" , exception );
                throw exception;
            }
            
            if ( _lastResponse == null )
                _lastResponse = new HttpResponse( _lastRequest );
            
            _lastResponse.setResponseCode( errorCode );
            _lastResponse.getJxpWriter().print( "error : " + msg + "\n\n" );
            _lastResponse.done();
        }
        
        protected boolean _gotData( ByteBuffer inBuf )
            throws IOException {
            
            _action( "gotdata" );

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
		if ( D ) System.out.println( "got content length of : " + cl );
                PostData pd = null;

                if ( cl > 0 ){
                    if ( _lastRequest._postData == null )
                        _lastRequest._postData = PostData.create( _lastRequest );
                    
                    pd = _lastRequest._postData;
                    
                    while ( end < _in.position() && ! pd.done() )
                        pd.put( _in.get( end++ ) );
                    
                    if ( ! pd.done() ){
			if ( D ) System.out.println( "only read " + pd.gotSoFar() + " out of " + cl );
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
                return handle( this , _lastRequest , _lastResponse );
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
            
            if ( header.trim().length() == 0 ){
                // this means the socket is just dead
                _bad = true;
                _done = true;
                registerForWrites();
                return false;
            }

            _lastRequest = new HttpRequest( this , header );
            _lastResponse = null;
            if ( D ) System.out.println( "created new request : " + _lastRequest );
            
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
        
        protected Selector getSelector(){
            return _selector;
        }
        
        public void close(){
            if ( _lastResponse != null )
                _lastResponse.socketClosing();

            super.close();

            ByteBufferHolder holder = _in;
            _in = null;
            _connectionByteBufferHolder.done( holder );
        }
        
        public String toString(){
            String s = super.toString();
            if ( _lastRequest != null )
                s += " " + _lastRequest.getFullURL();
            return s;
        }
        
        protected String debugString(){
            StringBuilder buf = new StringBuilder( 40 );

            if ( _lastRequest != null )
                buf.append( "cur request : " ).append( _lastRequest.getFullURL() ).append( " " );
            
            if ( _lastResponse != null )
                buf.append( "response.  done: " ).append( _lastResponse._done );
            
            return buf.toString();
        }

        String getLastUrl(){
            if ( _lastRequest == null )
                return "";
            return _lastRequest.getFullURL();
        }

        long timeSinceLastRequestStart( long now ){
            if ( _lastRequest == null )
                return 0;
            return now - _lastRequest._startTime;
        }

        String dataSentString(){
            if ( _lastResponse == null )
                return "-";
            if ( ! _lastResponse._done )
                return "-";
            return _lastResponse.dataSent() + "/" + _lastResponse.dataSize();
        }

        final HttpServer _server;
        
        private ByteBufferHolder _in = _connectionByteBufferHolder.get();
        int _endOfHeader = 0;
        boolean _done = false;
        HttpRequest _lastRequest;
        HttpResponse _lastResponse;
        boolean _inFork = false;
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
    
    private final SimplePool<ByteBufferHolder> _connectionByteBufferHolder = new WatchedSimplePool<ByteBufferHolder>( "HttpServer._connectionByteBufferHolders" , 20 , -1 ){

        public ByteBufferHolder createNew(){
            return new ByteBufferHolder( 1024 * 1024 * 200 ); // 200 mb
        }
        
        public boolean ok( ByteBufferHolder holder ){
            holder.position( 0 );
            return holder.capacity() < 1024 * 1024;
        }
    
        protected long memSize( ByteBufferHolder holder ){
            return holder.capacity();
        }

    };
    
    private int _lastHandlerHash = 0;
    private List<HttpHandler> _lastHandlers;
    
    private final List<HttpHandler> _myHandlers = new ArrayList<HttpHandler>();
    private final WorkerThreadPool _forkThreads = new WorkerThreadPool( "main" , WORKER_THREADS , WORKER_THREAD_QUEUE_MAX );
    private final WorkerThreadPool _forkThreadsAdmin = new WorkerThreadPool( "admin" , ADMIN_WORKER_THREADS , ADMIN_THREAD_QUEUE_MAX );
    
    static class WorkerThreadPool extends ThreadPool<Task> {

        WorkerThreadPool( String name , int num , int maxQueue ){
            super( "HttpServer-" + name , num , maxQueue );
        }
        
        public void handle( Task t ) throws IOException {
            
            boolean inForkWhenDone = false;

            try {
                inForkWhenDone = ! t._handler.handle( t._request , t._response );
            }
            finally {
                t._request._handler._inFork = inForkWhenDone;
            }
                
            try {
                t._response.done();
            }
            catch ( IOException ioe ){
                if ( D ) ioe.printStackTrace();
                t._response.cleanup();
            }
        }
        
        public void handleError( Task t , Exception e ){
            Logger l = t._request.getLogger();
            
            l.error( "error handling a task" , e );
            try {
                t._response.done();
            }
            catch ( IOException ioe ){
                if ( D ) ioe.printStackTrace();
            }
        }

    };

    public void addHandler( HttpHandler h ){
        _myHandlers.add( h );
    }
    
    public void setStat( String name , int val ){
        _specialStats.put( name , val );
    }
    
    List<HttpHandler> getHandlers(){

        int curHash = handlerHash();
        if ( _lastHandlers != null && _lastHandlerHash == curHash )
            return _lastHandlers;
        
        List<HttpHandler> lst = new ArrayList<HttpHandler>();
        lst.addAll( _myHandlers  );
        lst.addAll( _globalHandlers );
        Collections.sort( lst , _handlerComparator );

        _lastHandlers = lst;
        _lastHandlerHash = curHash;

        return lst;
    }

    int handlerHash(){
        int h = 0;
        h += _myHandlers.size();
        h += ( 100 * _globalHandlers.size() );
        h += ( 1000 * _globalHandlersMods );
        return h;
    }

    public static void addGlobalHandler( HttpHandler h ){
        _globalHandlersMods++;
        _globalHandlers.add( h );
    }

    public static boolean removeGlobalHandler( HttpHandler toRemove ){
        _globalHandlersMods++;
        for ( Iterator<HttpHandler> i = _globalHandlers.iterator(); i.hasNext(); ){
            HttpHandler h = i.next();
            if ( h != toRemove )
                continue;
            
            i.remove();
            return true;
        }
        
        return false;
    }

    private static Set<Integer> _listentingPorts = new HashSet<Integer>();
    
    public static int numberListeningPorts(){
        return _listentingPorts.size();
    }

    public static Set<Integer> getListeningPorts(){
        return Collections.unmodifiableSet( _listentingPorts );
    }

    class Stats extends HttpMonitor {
        Stats(){
            super( "stats" );
            
        }
        
        public void handle( MonitorRequest mr ){
            mr.addHeader( "stats" );
            
            final HttpServer server = mr.getRequest()._handler._server;
            final int forkedQueueSize = server._forkThreads.queueSize();
            
            if ( forkedQueueSize >= WORKER_THREAD_QUEUE_MAX )
                mr.getResponse().setResponseCode( 510 );
            
            mr.startData();
            
            mr.addData( "forked queue length" , forkedQueueSize == 0 ? null : ( forkedQueueSize < 50 ? Status.WARN : Status.ERROR ) , forkedQueueSize );
            mr.addData( "admin queue length" , server._forkThreadsAdmin.queueSize() );
            
            mr.addData( "forked processing" , server._forkThreads.inProgress() );
            mr.addData( "admin processing" , server._forkThreadsAdmin.inProgress() );
            
            mr.addData( "&nbsp;" , "" );
            
            mr.addData( "numRequests" , _numRequests );
            mr.addData( "numRequestsForked" , _numRequestsForked );
            mr.addData( "numRequestsAdmin" , _numRequestsAdmin );
            mr.addData( "numTotalConnections" , _numTotalConnections );
            
            mr.addData( "&nbsp;" , "" );
            
            mr.addData( "numSelectors" , getNumSelectors() );
            for ( String ss : _specialStats.keySet() ){
                mr.addData( ss , _specialStats.get( ss ) );
            }

            mr.addData( "&nbsp;" , "" );

            mr.addData( "uptime" , ed.js.JSMath.sigFig( ( System.currentTimeMillis() - _startTime ) / ( 1000 * 60.0 ) ) + " min\n" );
            
            mr.endData();
            
            mr.addSpacingLine();
            
            if ( mr.html() )
                _tracker.displayGraph( mr.getWriter() , _trackerOptions );
        }
        
        final long _startTime = System.currentTimeMillis();
    }


    class MySelectorMonitor extends HttpMonitor {
        MySelectorMonitor(){
            super( "selectors:http" );
        }
        
        public void handle( MonitorRequest mr ){
            mr.addHeader( "NIO Selectors" );
            
            final long now = System.currentTimeMillis();
            
            mr.startData( "selectors" , "age" , "last action" , "last action" , "last ready ops" ,
                          "last request" , "resp xfer" , 
                          "total bytes sent" , "empty writes" ,
                          "url" );
            for ( SocketHandler sh : getCurrentHandlers() ){
                if ( ! ( sh instanceof HttpSocketHandler ) )
                    continue;
                
                HttpSocketHandler h = (HttpSocketHandler)sh;
                
                mr.addData( sh.getRemote().toString() ,
                            ((double)sh.age( now ))/1000 , 
                            sh.lastAction() , 
                            ((double)sh.timeSinceLastAction( now )) / 1000 ,
                            NIOUtil.readyOps( sh.lastReadyOps() ) ,
                            ((double)h.timeSinceLastRequestStart( now )) / 1000 ,
                            h.dataSentString() ,
                            h.bytesWritten() ,
                            h.emptyWritesInARow() ,
                            h.getLastUrl() // leave at end 
                            );
                
            }
            mr.endData();
        }
        
    }
    
    private Map<String,Integer> _specialStats = new HashMap<String,Integer>();

    static final List<HttpHandler> _globalHandlers = new ArrayList<HttpHandler>();
    static int _globalHandlersMods = 0;
    static final Comparator _handlerComparator = new Comparator<HttpHandler>(){
        public int compare( HttpHandler a , HttpHandler b ){
            return a.priority() > b.priority() ? 1 : -1;
        }
    };

    static {
        DummyHttpHandler.setup();
        addGlobalHandler( new HttpMonitor.MemMonitor() );
        addGlobalHandler( new HttpMonitor.ThreadMonitor() );
        addGlobalHandler( new HttpMonitor.FavIconHack() );
        addGlobalHandler( new HttpMonitor.LogMonitor() );
        addGlobalHandler( new WatchedSimplePool.WebView() );

        addGlobalHandler( new HttpMonitor.AdminStaticFile( "admin.css" ) );
        addGlobalHandler( new HttpMonitor.AdminStaticFile( "admin.js" ) );
    }
    
    private static int _numRequests = 0;
    private static int _numRequestsForked = 0;
    private static int _numRequestsAdmin = 0;
    private static int _numTotalConnections = 0;

    private static final int _trackerSmall = 10;

    private static HttpLoadTracker.Rolling _tracker = new HttpLoadTracker.Rolling( "webserver" );
    private static HttpLoadTracker.GraphOptions _trackerOptions = new HttpLoadTracker.GraphOptions( 600 , 120 , true , false , false );
    
    // ---

    public static void main( String args[] )
        throws Exception {
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }
    
}
