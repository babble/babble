// HttpResponse.java

package ed.net.httpserver;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

public class HttpResponse {

    HttpResponse( HttpRequest request ){
        _request = request;
        _handler = _request._handler;
    }

    public void setResponseCode( int rc ){
        if ( _sentHeader )
            throw new RuntimeException( "already sent header " );
        _responseCode = rc;
    }
    
    public void flush()
        throws IOException {

        if ( ! _sentHeader ){
            ByteBuffer headOut = ByteBuffer.allocateDirect( 1024 );
            CharBuffer cout = headOut.asCharBuffer();
            _genHeader( cout );
            headOut.position( cout.position() * 2 );
            headOut.flip();
            _handler.getChannel().write( headOut );
            _sentHeader = true;
        }
        

        if ( _byteBuffers != null && _channelData != null )
            throw new RuntimeException( "can't have both :( " );
        
        if ( _channelData != null )
            throw new RuntimeException( "not implemented" );
        
        if ( _byteBuffers != null ){
            while ( _byteBuffers.size() > 0 ){
                ByteBuffer bb = _byteBuffers.remove(0);
                bb.flip();
                _handler.getChannel().write( bb );
            }
        }

        if ( true )
            _handler.getChannel().register( _handler.getSelector() , SelectionKey.OP_WRITE , _handler );
        
    }

    private Appendable _genHeader( Appendable a )
        throws IOException {
        // first line
        a.append( "HTTP/1.1 " );
        a.append( String.valueOf( _responseCode ) );
        a.append( "\n" );

        // headers
        if ( _headers != null ){
            for ( Map.Entry<String,String> v : _headers.entrySet() ){
                a.append( v.getKey() );
                a.append( ": " );
                a.append( v.getValue() );
                a.append( "\n" );
            }
        }
        
        // need to only do this if not chunked
        if ( _byteBuffers != null ){
            int cl = 0;
            for ( ByteBuffer bb : _byteBuffers )
                cl += bb.position();
            a.append( "Content-Length: " ).append( String.valueOf( cl ) ).append( "\n" );
        }
        
        // empty line
        a.append( "\n" );
        return a;
    }
    
    public String toString(){
        try {
            return _genHeader( new StringBuilder() ).toString();
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }

    public JxpWriter getWriter(){
        if ( _writer == null )
            _writer = new MyJxpWriter();
        return _writer;
    }
    
    final HttpRequest _request;
    final HttpServer.HttpSocketHandler _handler;
    
    // header
    int _responseCode = 200;
    Map<String,String> _headers;
    boolean _sentHeader = false;

    // data
    List<ByteBuffer> _byteBuffers = null;
    ReadableByteChannel _channelData = null;

    MyJxpWriter _writer = null;

    class MyJxpWriter implements JxpWriter {
        MyJxpWriter(){
            if ( _channelData != null )
                throw new RuntimeException( "can't do this if there is alredy _channelData " );
            if ( _byteBuffers != null )
                throw new RuntimeException( "already created it!" );
            _byteBuffers = new LinkedList<ByteBuffer>();

            _bcur = ByteBuffer.allocateDirect( 1024 * 64 );
            _ccur = _bcur.asCharBuffer();
            _byteBuffers.add( _bcur );
        }

        public JxpWriter print( int i ){
            return print( String.valueOf( i ) );
        }
        
        public JxpWriter print( double d ){
            return print( String.valueOf( d ) );
        }
        
        public JxpWriter print( boolean b ){
            return print( String.valueOf( b ) );
        }
        
        public JxpWriter print( String s ){
            _ccur.append( s );
            _bcur.position( _ccur.position() * 2 );
            return this;
        }
        
        public void flush(){
            throw new RuntimeException( "not implemented" );
        }

        public void reset(){
            _byteBuffers.clear();
        }

        private ByteBuffer _bcur;
        private CharBuffer _ccur;
    }
}
