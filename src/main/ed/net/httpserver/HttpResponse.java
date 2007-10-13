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

        _headers = new TreeMap<String,String>();
        _headers.put( "Content-Type" , "text/html; charset=" + getContentEncoding() );
        _headers.put( "Server" , "ED" );
        _headers.put( "Date" , "Sat, 13 Oct 2007 02:31:32 GMT" );
    }

    public void setResponseCode( int rc ){
        if ( _sentHeader )
            throw new RuntimeException( "already sent header " );
        _responseCode = rc;
    }

    public void done()
        throws IOException {
        _done = true;
        flush();
    }
    
    public void flush()
        throws IOException {

        if ( ! _sentHeader ){
            StringBuilder buf = new StringBuilder();
            _genHeader( buf );
            
            ByteBuffer headOut = ByteBuffer.allocateDirect( 1024 );
            headOut.put( buf.toString().getBytes() );
            headOut.flip();
            _handler.getChannel().write( headOut );
            _sentHeader = true;
        }
        
        if ( _stringContent != null && _channelData != null )
            throw new RuntimeException( "can't have both :( " );
        
        if ( _channelData != null )
            throw new RuntimeException( "not implemented" );
        
        if ( _stringContent != null ){
            while ( _stringContent.size() > 0 ){
                StringBuilder buf = _stringContent.remove(0);
                byte bs[] = buf.toString().getBytes( getContentEncoding() );
                ByteBuffer bb = ByteBuffer.allocateDirect( bs.length + 10 );
                bb.put( bs );
                bb.flip();
                int written = _handler.getChannel().write( bb );
            }
        }
        
        if ( keepAlive() )
            _handler.registerForReads();
        else 
            _handler.registerForWrites();
    }

    private Appendable _genHeader( Appendable a )
        throws IOException {
        // first line
        a.append( "HTTP/1.1 " );
        a.append( String.valueOf( _responseCode ) ).append( " " );
        a.append( "OK" );
        a.append( "\n" );

        // headers
        if ( _headers != null ){
            for ( Map.Entry<String,String> v : _headers.entrySet() ){
                a.append( v.getKey() );
                a.append( ": " );
                a.append( v.getValue() );
                a.append( "\r\n" );
            }
        }
        
        // need to only do this if not chunked
        if ( _done && _stringContent != null ){
            int cl = 0;
            for ( StringBuilder buf : _stringContent )
                cl += buf.length();
            a.append( "Content-Length: " ).append( String.valueOf( cl ) ).append( "\r\n" );
        }
        
        // empty line
        a.append( "\r\n" );
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

    public boolean keepAlive(){
        if ( ! _request.keepAlive() )
            return false;
        // TODO make sure we can send Content-Length or are chunking
        return true;
    }

    public String getContentEncoding(){
        return "UTF-8";
    }
    
    final HttpRequest _request;
    final HttpServer.HttpSocketHandler _handler;
    
    // header
    int _responseCode = 200;
    Map<String,String> _headers;
    boolean _sentHeader = false;

    // data
    List<StringBuilder> _stringContent = null;
    ReadableByteChannel _channelData = null;

    boolean _done = false;
    MyJxpWriter _writer = null;
    
    class MyJxpWriter implements JxpWriter {
        MyJxpWriter(){
            if ( _channelData != null )
                throw new RuntimeException( "can't do this if there is alredy _channelData " );
            if ( _stringContent != null )
                throw new RuntimeException( "already created it!" );

            _stringContent = new LinkedList<StringBuilder>();

            _cur = new StringBuilder();
            _stringContent.add( _cur );
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
            if ( _done )
                throw new RuntimeException( "already done" );
            _cur.append( s );
            return this;
        }
        
        public void flush(){
            throw new RuntimeException( "not implemented" );
        }

        public void reset(){
            _stringContent.clear();
        }

        private StringBuilder _cur;
    }
}
