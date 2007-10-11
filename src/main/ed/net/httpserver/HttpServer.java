// HttpServer.java

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.net.*;

public class HttpServer extends NIOServer {

    static final boolean D = true;

    public HttpServer( int port )
        throws IOException {
        super( port );
    }
    
    protected HttpSocketHandler accept( SocketChannel sc ){
        return new HttpSocketHandler( sc );
    }
    
    protected boolean handle( HttpRequest request )
        throws IOException {

        HttpResponse response = new HttpResponse( request );
        for ( int i=0; i<_handlers.size(); i++ ){
            if ( _handlers.get( i ).handles( request ) ){
                if ( _handlers.get( i ).fork() ){
                    throw new RuntimeException( "need to do this" );
                    //return true;
                }
                _handlers.get( i ).handle( request , response );
                response.flush();
                return false;
            }
        }
        response.setResponseCode( 404 );
        System.out.println( response );
        response.flush();
        return false;
        /*
        ByteBuffer out = ByteBuffer.allocateDirect( 1024 );
        CharBuffer cout = out.asCharBuffer();
        cout.append( "HTTP/1.1 404 OK\nConnection: Close\n\nfoo\n" );
        
        out.position( cout.position() * 2 );
        out.flip();
        request._handler.getChannel().write( out );
        
        if ( true )
            request._handler.getChannel().register( _selector , SelectionKey.OP_WRITE , request._handler );
        
        return false;
        */
    }
    
    class HttpSocketHandler extends SocketHandler {
        HttpSocketHandler( SocketChannel sc ){
            super( sc );
        }
        
        protected boolean shouldClose(){
            return _thisDataStart > 0;
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
                
            _thisDataStart = end;                

            final int headerSize = end - dataStart;
            byte bb[] = new byte[headerSize];
            for ( int i=0; i<bb.length; i++ )
                bb[i] = _in.get( i + dataStart );
                          
            String header = new String( bb );
            HttpRequest request = new HttpRequest( this , header );
            if ( D ) System.out.println( request );
                
            return handle( request );
        }
        
        protected SocketChannel getChannel(){
            return _channel;
        }

        protected Selector getSelector(){
            return _selector;
        }
        
        ByteBuffer _in = ByteBuffer.allocateDirect( 2048 );
        int _thisDataStart = 0;
    }

    public static void addGlobalHandler( HttpHandler h ){
        _handlers.add( h );
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
