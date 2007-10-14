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

    public boolean done()
        throws IOException {
        _done = true;
        return flush();
    }
    
    public boolean flush()
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
        
        if ( _numDataThings() == 0 )
            throw new RuntimeException( "need data" );
        if ( _numDataThings() > 1 )
            throw new RuntimeException( "too much data" );
        
        if ( _file != null ){
            FileChannel f = (new FileInputStream(_file)).getChannel();
            _fileSent += f.transferTo( _fileSent , Long.MAX_VALUE , _handler.getChannel() );
            if ( _fileSent < _file.length() ){
                _handler.registerForWrites();
                return false;
            }
        }
        
        if ( _stringContent != null ){
            for ( ; _stringContentSent < _stringContent.size() ; _stringContentSent++ ){
                StringBuilder buf = _stringContent.get( _stringContentSent );
                byte bs[] = buf.toString().substring( _stringContentPos ).getBytes( getContentEncoding() );
                ByteBuffer bb = ByteBuffer.allocateDirect( bs.length + 10 );
                bb.put( bs );
                bb.flip();
                int written = _handler.getChannel().write( bb );
                if ( written < bs.length ){
                    _stringContentPos += written;
                    _handler.registerForWrites();
                    return false;
                }
            }
        }
        
        if ( keepAlive() )
            _handler.registerForReads();
        else 
            _handler.registerForWrites();

        return true;
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
        
        if ( keepAlive() )
            a.append( "Connection: keep-alive\r\n" );
        else
            a.append( "Connection: close\r\n" );

        // need to only do this if not chunked
        if ( _done && _stringContent != null && _headers.get( "Content-Length") == null ){
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

        if ( _stringContent != null ){
            // TODO: chunking
            return _done;
        }
        return _headers.get( "Content-Length" ) != null;
    }

    public String getContentEncoding(){
        return "UTF-8";
    }

    public void sendFile( File f ){
        if ( ! f.exists() )
            throw new IllegalArgumentException( "file doesn't exist" );
        _file = f;
        _headers.put( "Content-Length" , String.valueOf( f.length() ) );
    }
    
    private int _numDataThings(){
        int num = 0;

        if ( _stringContent != null )
            num++;
        if ( _file != null )
            num++;

        return num;
    }

    private boolean _hasData(){
        return _numDataThings() > 0;
    }

    private void _checkNoContent(){
        if ( _hasData() )
            throw new RuntimeException( "already have data set" );
    }

    private void _checkContent(){
        if ( ! _hasData() )
            throw new RuntimeException( "no data set" );
    }
    
    final HttpRequest _request;
    final HttpServer.HttpSocketHandler _handler;
    
    // header
    int _responseCode = 200;
    Map<String,String> _headers;
    boolean _sentHeader = false;

    // data
    List<StringBuilder> _stringContent = null;
    int _stringContentSent = 0;
    int _stringContentPos = 0;

    File _file;
    long _fileSent = 0;

    boolean _done = false;
    MyJxpWriter _writer = null;
    
    class MyJxpWriter implements JxpWriter {
        MyJxpWriter(){
            _checkNoContent();

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
