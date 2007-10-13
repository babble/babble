// HttpServer.java

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.net.*;
import ed.util.*;

public class HttpServer extends NIOServer {

    static final boolean D = true;

    public HttpServer( int port )
        throws IOException {
        super( port );
    }
    
    protected HttpSocketHandler accept( SocketChannel sc ){
        return new HttpSocketHandler( sc );
    }
    
    protected boolean handle( HttpRequest request , HttpResponse response )
        throws IOException {

        Box<Boolean> fork = new Box<Boolean>(true);
        for ( int i=0; i<_handlers.size(); i++ ){
            if ( _handlers.get( i ).handles( request , response , fork ) ){
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
        HttpSocketHandler( SocketChannel sc ){
            super( sc );
        }
        
        protected boolean shouldClose(){
            if ( _lastResponse != null )
                return ! _lastResponse.keepAlive();
            return _thisDataStart > 0 ;
        }
        
        protected boolean gotData( ByteBuffer inBuf )
            throws IOException {
            
            if ( inBuf.remaining() > _in.remaining() ){
                throw new RuntimeException( "problem" );
            }
            
            _in.put( inBuf );
            if ( D ) System.out.println( _in );
            
            boolean lastWasNewLine = false;
            boolean finishedHeader = false;

            final int dataStart = _thisDataStart;
            final int dataEnd = _in.position();
            int end = dataStart;
            for ( ; end < dataEnd ; end++ ){
                byte b = _in.get(end);
                if ( b == '\r' )
                    continue;
                    
                if ( b == '\n' ){
                    if ( lastWasNewLine ){
                        finishedHeader = true;
                        if ( D ) System.err.println( "got end of header! " );
                        break;
                    }
                    lastWasNewLine = true;
                }
                else
                    lastWasNewLine = false;
            }
                
            if ( ! finishedHeader )
                return false;
                
            _thisDataStart = end + 1; 

            final int headerSize = end - dataStart;
            byte bb[] = new byte[headerSize];
            for ( int i=0; i<bb.length; i++ )
                bb[i] = _in.get( i + dataStart );
                          
            String header = new String( bb );
            if ( D ) System.out.println( "---\n" + header + "\n---" );
            _lastRequest = new HttpRequest( this , header );
            _lastResponse = new HttpResponse( _lastRequest );
            if ( D ) System.out.println( _lastRequest );
                
            return handle( _lastRequest , _lastResponse );
        }
        
        protected SocketChannel getChannel(){
            return _channel;
        }

        protected Selector getSelector(){
            return _selector;
        }
        
        ByteBuffer _in = ByteBuffer.allocateDirect( 2048 );
        int _thisDataStart = 0;
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
            System.out.println( "handler" );
            t._handler.handle( t._request , t._response );
            t._response.done();
        }
        
        public void handleError( Task t , Exception e ){
            e.printStackTrace();
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

    static {
        DummyHttpHandler.setup();
    }

    public static void main( String args[] )
        throws Exception {
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }
    
}
