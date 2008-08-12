// HttpResponse.java

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
import java.util.*;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;
import ed.appserver.*;

/**
 * Represents response to an HTTP request.  On each request, the 10gen app server defines 
 * the variable 'response' which is of this type.
 * @expose
 */
public class HttpResponse extends JSObjectBase {

    static final boolean USE_POOL = true;
    static final String DEFAULT_CHARSET = "utf-8";

    public static final DateFormat HeaderTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    public static final String SERVER_HEADERS;

    static {
	HeaderTimeFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
	
	String hostname = "unknown";
	try {
	    hostname = InetAddress.getLocalHost().getHostName();
	}
	catch ( Throwable t ){
	    t.printStackTrace();
	}

	SERVER_HEADERS = "Server: ED\r\nX-svr: " + hostname + "\r\n";
    }

    /**
     * Create a response to a given request.
     */
    HttpResponse( HttpRequest request ){
        _request = request;
        _handler = _request._handler;

        _headers = new StringMap<String>();
        _headers.put( "Content-Type" , "text/html;charset=" + getContentEncoding() );
	setDateHeader( "Date" , System.currentTimeMillis() );

        set( "prototype" , _prototype );
    }

    /**
     * Set the HTTP response code for this response.
     * A list of HTTP status codes is available at
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html .
     * @param rc a response code according to the HTTP spec
     */
    public void setResponseCode( int rc ){
        if ( _sentHeader )
            throw new RuntimeException( "already sent header " );
        _responseCode = rc;
    }

    /**
     * Get the HTTP response code for this response.
     * @return this response's HTTP status code
     */
    public int getResponseCode(){
        return _responseCode;
    }


    /**
     * Add an additional cookie to be sent in this response.
     * All cookies will be sent to the browser, but browsers typically only
     * keep the last cookie with a given name.
     * @param name cookie name
     * @param value cookie value
     * @param maxAge
     *          > 0 = seconds into the future
     *            0 = browser session
     *          < 0 remove
     */
    public void addCookie( String name , String value , int maxAge ){
        _cookies.add( new Cookie( name , value , maxAge ) );
    }

    /**
     * Add a cookie to be sent in this response. This sends a session cookie
     * (which is typically deleted when the browser is closed).
     * @param name cookie name
     * @param value cookie value
     */
    public void addCookie( String name , String value ){
        _cookies.add( new Cookie( name , value ) );
    }
    
    /**
     * Equivalent to "addCookie( name , null , -1 )". Tells the browser
     * that the cookie with this name is already expired.
     * @param name cookie name
     */
    public void removeCookie( String name ){
        _cookies.add( new Cookie( name , "asd" , -1 ) );
    }

    /**
     * Set a cache time for this response.
     * Sets the Cache-Control and Expires headers
     * @param seconds the number of seconds that this response should be cached
     */
    public void setCacheTime( int seconds ){
	setHeader("Cache-Control" , "max-age=" + seconds );
	setDateHeader( "Expires" , System.currentTimeMillis() + ( 1000 * seconds ) );
    }
    
    /**
     * Set a header as a date.
     *
     * Formats a time, passed as an integer number of milliseconds, as a date,
     * and sets a header with this date.
     *
     * @param n the name of the header
     * @param t the value of the header, as a number of milliseconds
     */
    public void setDateHeader( String n , long t ){
	synchronized( HeaderTimeFormat ) {
	    setHeader( n , HeaderTimeFormat.format( new Date(t) ) );
	}
    }

    /**
     * Set a header in the response.
     * Overwrites previous headers with the same name
     * @param n the name of the header to set
     * @param v the value of the header to set, as a string
     */
    public void setHeader( String n , String v ){
        _headers.put( n , v );
    }

    public String getHeader( String n ){
        return _headers.get( n );
    }

    /**
     * @unexpose
     */
    void cleanup(){
        if ( _cleaned )
            return;
        
        if ( _doneHooks != null ){
            for ( Pair<Scope,JSFunction> p : _doneHooks ){
                try {
                    p.second.call( p.first );
                }
                catch ( Throwable t ){
                    Logger l = Logger.getLogger( "HttpResponse" );
                    if ( p.first.get( "log" ) instanceof Logger )
                        l = (Logger)p.first.get( "log" );
                    l.error( "error running done hook" , t );
                }
            }
        }

        if ( _appRequest != null && _appRequest.getScope() != null )
            _appRequest.getScope().kill();
        
        _handler._done = ! keepAlive();
        
        _cleaned = true;
        if ( _myStringContent != null ){

            for ( ByteBuffer bb : _myStringContent ){
                if ( USE_POOL )
                    _bbPool.done( bb );
            }
            
            _myStringContent.clear();
            _myStringContent = null;
        }
        
        if ( _writer != null ){
            _charBufPool.done( _writer._cur );
            _writer._cur = null;
            _writer = null;
        }
        
    }

    /**
     * @unexpose
     */
    protected boolean done()
        throws IOException {

        if ( _cleaned )
            return true;

        _done = true;
        boolean f = flush();
        if ( f )
            cleanup();
        return f;
    }

    /**
     * Send a permanent (301) redirect to the given location.
     * Equivalent to calling setResponseCode( 301 ) followed by
     * setHeader( "Location" , loc ).
     * @param loc the location to redirect to
     */
    public void sendRedirectPermanent(String loc){
        setResponseCode( 301 );
        setHeader("Location", loc);
    }

    /**
     * Send a temporary (302) redirect to the given location.
     * Equivalent to calling setResponseCode( 302 ) followed by
     * setHeader( "Location" , loc ).
     * @param loc the location to redirect to
     */
    public void sendRedirectTemporary(String loc){
        setResponseCode( 302 );
        setHeader("Location", loc);
    }

    private boolean flush()
        throws IOException {
        return _flush();
    }
    
    private boolean _flush()
        throws IOException {

        if ( _cleaned )
            throw new RuntimeException( "already cleaned" );
        
        if ( _numDataThings() > 1 )
            throw new RuntimeException( "too much data" );


        if ( ! _sentHeader ){
            final String header = _genHeader();
            final byte[] bytes = header.getBytes();
            final ByteBuffer headOut = ByteBuffer.wrap( bytes );
            _handler.getChannel().write( headOut );
            _sentHeader = true;
        }
        
        if ( _file != null ){
            if ( _fileChannel == null ){
		try {
		    _fileChannel = (new FileInputStream(_file)).getChannel();
		}
		catch( IOException ioe ){
		    throw new RuntimeException( "can't get file : " + _file , ioe );
		}
	    }
            
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
        
        cleanup();
        
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
        {
            String rc = String.valueOf( _responseCode );
            a.append( rc ).append( " " );
            Object msg = _responseMessages.get( rc );
            if ( msg == null )
                a.append( "OK" );
            else
                a.append( msg.toString() );
            a.append( "\n" );
        }

	a.append( SERVER_HEADERS );

        // headers
        if ( _headers != null ){
            List<String> headers = new ArrayList<String>( _headers.keySet() );
            Collections.sort( headers );
            for ( int i=headers.size()-1; i>=0; i-- ){
                String h = headers.get( i );
                a.append( h );
                a.append( ": " );
                a.append( _headers.get( h ) );
                a.append( "\r\n" );
            }
        }

        // cookies
        for ( Cookie c : _cookies ){
            a.append( "Set-Cookie: " );
            a.append( c._name ).append( "=" ).append( c._value ).append( ";" );
	    a.append( " " ).append( "Path=" ).append( c._path ).append( ";" );
            String expires = c.getExpires();
            if ( expires != null )
                a.append( "Expires=" ).append( expires ).append( "; " );
            a.append( "\r\n" );
        }
        
        if ( keepAlive() )
            a.append( "Connection: keep-alive\r\n" );
        else
            a.append( "Connection: close\r\n" );

        if ( _writer != null )
            _writer._push();

        if ( _headers.get( "Content-Length") == null ){
            
            if ( _stringContent != null ){
                int cl = 0;
                for ( ByteBuffer buf : _stringContent )
                    cl += buf.limit();
                if ( HttpServer.D ) System.out.println( "_stringContent.length : " + cl );
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

    /**
     * Tries to compute the size of this entire response
     * Adds the size of the headers plus the content-length know about so far
     * Slightly expensive.
     * @return the size of the headers
     */
    public int totalSize(){
        final int size[] = new int[]{ 0 };
        Appendable a = new Appendable(){

                public Appendable append( char c ){
                    size[0] += 1;
                    return this;
                }

                public Appendable append( CharSequence s ){
                    if ( s == null )
                        return this;
                    return append( s , 0 , s.length() );
                }

                public Appendable append( CharSequence s , int start , int end ){
                    size[0] += ( end - start );

                    if ( _count == 1 ){
                        int add = StringParseUtil.parseInt( s.toString() , 0 );
                        size[0] += add;
                    }

                    _count--;
                    
                    if ( "Content-Length".equalsIgnoreCase( s.toString() ) )
                        _count = 2;
                    if ( "Content-Length: ".equalsIgnoreCase( s.toString() ) )
                        _count = 1;
                    
                    return this;
                }
                
                int _count = 0;
            };
        try {
            _genHeader( a );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
        return size[0];
    }
    
    /**
     * Return this response, formatted as a string: HTTP response, headers,
     * cookies.
     */
    public String toString(){
        try {
            return _genHeader();
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }

    /**
     * @unexpose
     */
    public JxpWriter getWriter(){
        if ( _writer == null ){
            if ( _cleaned )
                throw new RuntimeException( "already cleaned" );
            
            _writer = new MyJxpWriter();
        }
        return _writer;
    }

    /**
     * @unexpose
     */
    public void setData( ByteBuffer bb ){
        _stringContent = new LinkedList<ByteBuffer>();
        _stringContent.add( bb );
    }

    /**
     * @unexpose
     */
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

    /**
     * @unexpose
     */
    public String getContentEncoding(){
        return DEFAULT_CHARSET;
    }

    /**
     * Sends a file to the browser.
     * @param f a file to send
     */
    public void sendFile( File f ){
        if ( ! f.exists() )
            throw new IllegalArgumentException( "file doesn't exist" );
        _file = f;
        _headers.put( "Content-Length" , String.valueOf( f.length() ) );
	_stringContent = null;
    }

    /**
     * Sends a file to the browser, including (for database-stored files)
     * sending a sensible Content-Disposition.
     * @param f a file to send
     */
    public void sendFile( JSFile f ){
        if ( f instanceof JSLocalFile ){
            sendFile( ((JSLocalFile)f).getRealFile() );
            return;
        }
        
        if ( f.getFileName() != null && _headers.get( "Content-Disposition" ) == null ){
            _headers.put( "Content-Disposition" , f.getContentDisposition() + "; filename=\"" + f.getFileName() + "\"" );
        }

        long length = f.getLength();
        _jsfile = f.sender();
        _stringContent = null;
        
        long range[] = _request.getRange();
        if ( range != null && ( range[0] > 0 || range[1] < length ) ){
            
            if ( range[1] > length )
                range[1] = length;
            
            try {
                _jsfile.skip( range[0] );
            }
            catch ( IOException ioe ){
                throw new RuntimeException( "can't skip " , ioe );
            }

            _jsfile.maxPosition( range[1] + 1 );

            setResponseCode( 206 );
            setHeader( "Content-Range" , "bytes " + range[0] + "-" + range[1] + "/" + length );
            setHeader( "Content-Length" , String.valueOf(  1 + range[1] - range[0] ) );
            System.out.println( "got range " + range[0] + " -> " + range[1] );
            

            return;
        }
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

    /**
     * Adds a "hook" function to be called after this response has been sent.
     * @param f a function
     */
    public void addDoneHook( Scope s , JSFunction f ){
        if ( _doneHooks == null )
            _doneHooks = new ArrayList<Pair<Scope,JSFunction>>();
        _doneHooks.add( new Pair<Scope,JSFunction>( s , f ) );
    }
    
    /**
     * @unexpose
     */
    public void setAppRequest( AppRequest ar ){
        _appRequest = ar;
    }

    final HttpRequest _request;
    final HttpServer.HttpSocketHandler _handler;
    
    // header
    int _responseCode = 200;
    Map<String,String> _headers;
    List<Cookie> _cookies = new ArrayList<Cookie>();
    boolean _sentHeader = false;

    // data
    List<ByteBuffer> _stringContent = null;
    private List<ByteBuffer> _myStringContent = null; // tihs is the real one
    
    int _stringContentSent = 0;
    int _stringContentPos = 0;

    File _file;
    FileChannel _fileChannel;
    long _fileSent = 0;

    boolean _done = false;
    boolean _cleaned = false;
    MyJxpWriter _writer = null;
    
    JSFile.Sender _jsfile;

    private AppRequest _appRequest;
    private List<Pair<Scope,JSFunction>> _doneHooks;
    
    class MyJxpWriter implements JxpWriter {
        MyJxpWriter(){
            _checkNoContent();

            _myStringContent = new LinkedList<ByteBuffer>();
            _stringContent = _myStringContent;
            
            _cur = _charBufPool.get();
            _resetBuf();
        }

        public Appendable append(char c){
            print( String.valueOf( c ) );
            return this;
        }
        
        public Appendable append(CharSequence csq){
            print( csq.toString() );
            return this;
        }
        public Appendable append(CharSequence csq, int start, int end){
            print( csq.subSequence( start , end ).toString() );
            return this;
        }


        public JxpWriter print( int i ){
            return print( String.valueOf( i ) );
        }
        
        public JxpWriter print( double d ){
            return print( String.valueOf( d ) );
        }

        public JxpWriter print( long l ){
            return print( String.valueOf( l ) );
        }
        
        public JxpWriter print( boolean b ){
            return print( String.valueOf( b ) );
        }

        public boolean closed(){
            return _done;
        }
        
        public JxpWriter print( String s ){
            if ( _done ){
                throw new RuntimeException( "already done" );
            }
            
            if ( s.length() > MAX_STRING_SIZE ){
                for ( int i=0; i<s.length(); ){
                    String temp = s.substring( i , Math.min( i + MAX_STRING_SIZE , s.length() ) );
                    print( temp );
                    i += MAX_STRING_SIZE;
                }
                return this;
            }

            if ( _cur.position() + ( 3 * s.length() ) > _cur.capacity() ){
                if ( _inSpot )
                    throw new RuntimeException( "can't put that much stuff in spot" );
                _push();
            }
            

            if ( _cur.position() + ( 3 * s.length() ) > _cur.capacity() )
                throw new RuntimeException( "still too big" );

            _cur.append( s );
            return this;
        }
        
        void _push(){
            if ( _cur.position() == 0 )
                return;
            
            _cur.flip();
            ByteBuffer bb = USE_POOL ? _bbPool.get() : ByteBuffer.wrap( new byte[ _cur.limit() * 2 ] );
            if ( bb.position() != 0 || bb.limit() != bb.capacity() )
                throw new RuntimeException( "something is wrong with _bbPool" );
            
            CharsetEncoder encoder = _defaultCharset.newEncoder(); // TODO: pool
            try {
                CoderResult cr = encoder.encode( _cur , bb , true );
                if ( cr.isUnmappable() )
                    throw new RuntimeException( "can't map some character" );
                if ( cr.isOverflow() )
                    throw new RuntimeException( "buffer overflow here is a bad thing.  bb after:" + bb );
                
                bb.flip();
                
                if ( _inSpot )
                    _myStringContent.add( _spot , bb );
                else
                    _myStringContent.add( bb );
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
            _myStringContent.clear();
            _resetBuf();
        }

        public String getContent(){
            throw new RuntimeException( "not implemented" );
        }

        void _resetBuf(){
            _cur.position( 0 );
            _cur.limit( _cur.capacity() );
        }


        // reset
        public void mark( int m ){
            _mark = m;
        }
        public void clearToMark(){
            throw new RuntimeException( "not implemented yet" );
        }
        public String fromMark(){
            throw new RuntimeException( "not implemented yet" );
        }
        
        // going back
        public void saveSpot(){
            if ( _spot >= 0 )
                throw new RuntimeException( "already have spot saved" );
            _push();
            _spot = _myStringContent.size();
        }
        public void backToSpot(){
            if ( _spot < 0 )
                throw new RuntimeException( "don't have spot" );
            _push();
            _inSpot = true;
        }
        public void backToEnd(){
            _push();
            _inSpot = false;
            _spot = -1;
        }        
        public boolean hasSpot(){
            return _spot >= 0;
        }

        private CharBuffer _cur;
        private int _mark = 0;
        
        private int _spot = -1;
        private boolean _inSpot = false;
    }
    
    static final int CHAR_BUFFER_SIZE = 1024 * 128;
    static final int MAX_STRING_SIZE = CHAR_BUFFER_SIZE / 4;
    static SimplePool<CharBuffer> _charBufPool = new SimplePool<CharBuffer>( "Response.CharBufferPool" , 50 , -1 ){
            public CharBuffer createNew(){
                return CharBuffer.allocate( CHAR_BUFFER_SIZE );
            }
            
            public boolean ok( CharBuffer buf ){
                buf.position( 0 );
                buf.limit( buf.capacity() );
                return true;
            }
        };
    static ByteBufferPool _bbPool = new ByteBufferPool( 50 , CHAR_BUFFER_SIZE * 4  );
    static StringBuilderPool _headerBufferPool = new StringBuilderPool( 25 , 1024 );
    static Charset _defaultCharset = Charset.forName( DEFAULT_CHARSET );

    static final Properties _responseMessages = new Properties();
    static {
        try {
            _responseMessages.load( ClassLoader.getSystemClassLoader().getResourceAsStream( "ed/net/httpserver/responseCodes.properties" ) );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( ioe );
        }
    }
    
    
    static final JSObjectBase _prototype = new JSObjectBase();
    static {

        _prototype.set( "afterSendingCall" , new JSFunctionCalls1(){
                public Object call( Scope s , Object func , Object foo[] ){
                    
                    if ( ! ( func instanceof JSFunction ) )
                        throw new RuntimeException( "can't call afterSendingCall w/o function" );
                    
                    HttpResponse res = (HttpResponse)s.getThis();
                    res.addDoneHook( s , (JSFunction)func );
                    
                    return null;
                }
            } );
        
    }

}
