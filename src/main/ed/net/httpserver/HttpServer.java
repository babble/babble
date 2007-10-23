// HttpServer

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.io.*;
import ed.net.*;
import ed.util.*;

public class HttpServer extends NIOServer {

    static final boolean D = Boolean.getBoolean( "DEBUG.HTTP" );

    public HttpServer( int port )
        throws IOException {
        super( port );
    }
    
    protected HttpSocketHandler accept( SocketChannel sc ){
        return new HttpSocketHandler( this , sc );
    }
    
    protected boolean handle( HttpRequest request , HttpResponse response )
        throws IOException {

        Box<Boolean> fork = new Box<Boolean>(true);
        for ( int i=0; i<_handlers.size(); i++ ){
            if ( _handlers.get( i ).handles( request , fork ) ){
                request._handler.pause();
                if ( fork.get() ){
                    if ( _forkThreads.offer( new Task( request , response , _handlers.get( i ) ) ) ){
                        if ( D ) System.out.println( "successfully gave thing to a forked thing" );
                        return false;
                    }
                    
                    response.setResponseCode( 501 );
                    response.getWriter().print( "no more threads\n" );
                    response.done();
                    return false;
                }
                _handlers.get( i ).handle( request , response );
                response.done();
                return false;
            }
        }
        response.setResponseCode( 404 );
        response.getWriter().print( "go away\n" );
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
            
            if ( inBuf != null ){
                if ( inBuf.remaining() > _in.remaining() ){
                    throw new RuntimeException( "problem" );
                }
                
                _in.put( inBuf );
            }
            if ( D ) System.out.println( _in );
            
            if ( _lastRequest != null && _lastResponse == null ){
                int end = _endOfHeader;
                end++;
                                
                final int cl = _lastRequest.getIntHeader( "Content-Length" , 0 );
                if ( cl > 0 ){
                    if ( cl > ( 1024 * 1024 * 10  ) )
                        throw new RuntimeException( "content-length way too big" );
                    
                    int dataReady = _in.position() - _endOfHeader;
                    if ( dataReady < cl )
                        return false;
                    
                    byte postData[] = new byte[cl];
                    for ( int i=0; i<postData.length; i++ )
                        postData[i] = _in.get( i + end );
                    _lastRequest._postData = postData;
                    
                    end += cl;
                }

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
        ByteBufferHolder _in = new ByteBufferHolder();
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

    private final ThreadPool<Task> _forkThreads = 
        new ThreadPool<Task>( "HttpServer" , 250 ){
        
        public void handle( Task t ) throws IOException {
            t._handler.handle( t._request , t._response );
            t._response.done();
        }
        
        public void handleError( Task t , Exception e ){
            e.printStackTrace();
            try {
                t._response.done();
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
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
    static List<HttpHandler> _handlers = new ArrayList<HttpHandler>();

    static final HttpHandler _stats = new HttpHandler(){

            public boolean handles( HttpRequest request , Box<Boolean> fork ){
                return request.getURI().equals( "/~stats" );
            }
            
            public void handle( HttpRequest request , HttpResponse response ){
                response.setHeader( "Content-Type" , "text/plain" );
                
                JxpWriter out = response.getWriter();

                
                out.print( "stats\n" );
                out.print( "---\n" );
                
                out.print( "forked queue length : " + request._handler._server._forkThreads.queueSize()  + "\n" );
                
            }
            
            public double priority(){
                return Double.MIN_VALUE;
            }
        };
    
    static {
        DummyHttpHandler.setup();
        addGlobalHandler( _stats );
    }

    public static void main( String args[] )
        throws Exception {
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }
    
}
