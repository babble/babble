// HttpResponse.java

package ed.net.httpserver;

import java.io.*;
import java.util.*;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import ed.js.*;
import ed.util.*;

public class HttpResponse {

    public static final DateFormat HeaderTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    static {
	HeaderTimeFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
    }


    HttpResponse( HttpRequest request ){
        _request = request;
        _handler = _request._handler;

        _headers = new StringMap<String>();
        _headers.put( "Content-Type" , "text/html; charset=" + getContentEncoding() );
        _headers.put( "Server" , "ED" );
	setDateHeader( "Date" , System.currentTimeMillis() );
    }

    public void setResponseCode( int rc ){
        if ( _sentHeader )
            throw new RuntimeException( "already sent header " );
        _responseCode = rc;
    }

    public void setCacheTime( int seconds ){
	setHeader("Cache-Control" , "max-age=" + seconds );
	setDateHeader( "Expires" , System.currentTimeMillis() + ( 1000 * seconds ) );
    }
    
    public void setDateHeader( String n , long t ){
	synchronized( HeaderTimeFormat ) {
	    setHeader( n , HeaderTimeFormat.format( new Date(t) ) );
	}
    }

    public void setHeader( String n , String v ){
        _headers.put( n , v );
    }

    void cleanup(){
        _handler._done = ! keepAlive();
        
        _cleaned = true;
        if ( _stringContent != null ){
            for ( ByteBuffer bb : _stringContent )
                _bbPool.done( bb );
            
            _stringContent.clear();

            if ( _writer != null ){
                _charBufPool.done( _writer._cur );
                _writer._cur = null;
                _writer = null;
            }
        }
        
    }
    
    public boolean done()
        throws IOException {

        if ( _cleaned )
            return true;

        _done = true;
        boolean f = flush();
        if ( f )
            cleanup();
        return f;
    }

    public boolean flush()
        throws IOException {
        return _flush();
    }
    
    boolean _flush()
        throws IOException {

        if ( _cleaned )
            throw new RuntimeException( "already cleaned" );
        
        if ( _numDataThings() > 1 )
            throw new RuntimeException( "too much data" );


        if ( ! _sentHeader ){
            final String header = _genHeader();
            
            ByteBuffer headOut = ByteBuffer.allocateDirect( 1024 );
            headOut.put( header.getBytes() );
            headOut.flip();
            _handler.getChannel().write( headOut );
            _sentHeader = true;
        }
        
        if ( _file != null ){
            if ( _fileChannel == null )
                _fileChannel = (new FileInputStream(_file)).getChannel();
            
            try {
                _fileSent += _fileChannel.transferTo( _fileSent , Long.MAX_VALUE , _handler.getChannel() );
            }
            catch ( IOException ioe ){
                if ( ioe.toString().indexOf( "Resource temporarily unavailable" ) < 0 )
                    throw ioe;
            }
            if ( _fileSent < _file.length() ){
                if ( HttpServer.D ) System.out.println( "only sent : " + _fileSent );
                _handler.registerForWrites();
                return false;
            }
        }

        if ( _writer != null )
            _writer._push();
        
        if ( _stringContent != null ){
            for ( ; _stringContentSent < _stringContent.size() ; _stringContentSent++ ){
                
                ByteBuffer bb = _stringContent.get( _stringContentSent );
                _stringContentPos += _handler.getChannel().write( bb );
                if ( _stringContentPos < bb.limit() ){
                    if ( HttpServer.D ) System.out.println( "only wrote " + _stringContentPos + " out of " + bb );
                    _handler.registerForWrites();
                    return false;
                }
                _stringContentPos = 0;
            }
        }

        if ( _jsfile != null ){
            if ( ! _jsfile.write( _handler.getChannel() ) ){
                _handler.registerForWrites();
                return false;
            }
        }
        
        if ( keepAlive() && ! _handler.hasData() )
            _handler.registerForReads();
        else 
            _handler.registerForWrites();

        return true;
    }

    private String _genHeader()
        throws IOException {
        StringBuilder buf = _headerBufferPool.get();
        _genHeader( buf );
        String header = buf.toString();
        _headerBufferPool.done( buf );
        return header;
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

        if ( _writer != null )
            _writer._push();

        // need to only do this if not chunked
        if ( _done && _headers.get( "Content-Length") == null ){
            
            if ( _stringContent != null ){
                int cl = 0;
                for ( ByteBuffer buf : _stringContent )
                    cl += buf.limit();
                a.append( "Content-Length: " ).append( String.valueOf( cl ) ).append( "\r\n" );
            }
            else if ( _numDataThings() == 0 ) {
                a.append( "Content-Length: 0\r\n" );
            }
            
        }
        
        // empty line
        a.append( "\r\n" );
        return a;
    }
    
    public String toString(){
        try {
            return _genHeader();
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

        if ( _headers.get( "Content-Length" ) != null )
            return true;

        if ( _stringContent != null ){
            // TODO: chunking
            return _done;
        }

        return false;
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

    public void sendFile( JSFile f ){
        _jsfile = f.sender();
        _headers.put( "Content-Length" , String.valueOf( f.getLength() ) );
        _headers.put( "Content-Type" , f.getContentType() );
    }
    
    private int _numDataThings(){
        int num = 0;

        if ( _stringContent != null )
            num++;
        if ( _file != null )
            num++;
        if ( _jsfile != null )
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
    List<ByteBuffer> _stringContent = null;
    int _stringContentSent = 0;
    int _stringContentPos = 0;

    File _file;
    FileChannel _fileChannel;
    long _fileSent = 0;

    boolean _done = false;
    boolean _cleaned = false;
    MyJxpWriter _writer = null;
    
    JSFile.Sender _jsfile;
    
    class MyJxpWriter implements JxpWriter {
        MyJxpWriter(){
            _checkNoContent();

            _stringContent = new LinkedList<ByteBuffer>();

            _cur = _charBufPool.get();
            _resetBuf();
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

            if ( _cur.position() + s.length() > _cur.capacity() ){
                _push();
            }
            _cur.append( s );
            return this;
        }

        void _push(){
            if ( _cur.position() == 0 )
                return;
            
            _cur.flip();
            ByteBuffer bb = _bbPool.get();
            
            CharsetEncoder encoder = _utf8.newEncoder(); // TODO: pool
            try {
                encoder.encode( _cur , bb , true );
                bb.flip();

                _stringContent.add( bb );
                _resetBuf();
            }
            catch ( Exception e ){
                throw new RuntimeException( "no" , e );
            }
        }
        
        public void flush()
            throws IOException {
            _flush();
        }

        public void reset(){
            _stringContent.clear();
        }

        public String getContent(){
            throw new RuntimeException( "not implemented" );
        }

        void _resetBuf(){
            _cur.position( 0 );
            _cur.limit( _cur.capacity() );
        }
        
        private CharBuffer _cur;
    }
    
    static final int CHAR_BUFFER_SIZE = 1024 * 32;
    static SimplePool<CharBuffer> _charBufPool = new SimplePool( "Response.CharBufferPool" , 50 , -1 ){
            public CharBuffer createNew(){
                return CharBuffer.allocate( CHAR_BUFFER_SIZE );
            }
        };
    static ByteBufferPool _bbPool = new ByteBufferPool( 50 , CHAR_BUFFER_SIZE * 2 );
    static StringBuilderPool _headerBufferPool = new StringBuilderPool( 25 , 1024 );
    static Charset _utf8 = Charset.forName( "UTF-8" );
    
}
