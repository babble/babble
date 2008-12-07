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
import javax.servlet.*;
import javax.servlet.http.*;

import ed.io.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.appserver.*;

/**
 * Represents response to an HTTP request.  On each request, the 10gen app server defines
 * the variable 'response' which is of this type.
 * @expose
 * @docmodule system.HTTP.response
 */
public class HttpResponse extends JSObjectBase implements HttpServletResponse {
    
    static final boolean USE_POOL = true;
    static final String DEFAULT_CHARSET = "utf-8";
    static final long MIN_GZIP_SIZE = 1000;
    static final Set<String> GZIP_MIME_TYPES;
    static {
	Set<String> s = new HashSet<String>();
	s.add( "application/x-javascript" );
	s.add( "text/css" );
	s.add( "text/html" );
	s.add( "text/plain" );
	GZIP_MIME_TYPES = Collections.unmodifiableSet( s );
    }
    
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

        setContentType( "text/html;charset=" + getContentEncoding() );
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
            throw new RuntimeException( "already sent header : " + hashCode() );
        _responseCode = rc;
    }

    public void setStatus( int rc ){
        setResponseCode( rc );
    }

    public void setStatusCode( int rc ){
        setResponseCode( rc );
    }

    /**
     * @deprecated
     */
    public void setStatus( int rc , String name ){
        setResponseCode( rc );
    }

    public void sendError( int rc ){
        setResponseCode( rc );
    }

    public void sendError( int rc , String msg ){
        setResponseCode( rc );
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
     *            0 = remove
     *          < 0 = session
     */
    public void addCookie( String name , String value , int maxAge ){
        Cookie c = new Cookie( name , value );
        c.setPath( "/" );
        c.setMaxAge( maxAge );
        _cookies.add( c );
    }

    /**
     * Add a cookie to be sent in this response. This sends a session cookie
     * (which is typically deleted when the browser is closed).
     * @param name cookie name
     * @param value cookie value
     */
    public void addCookie( String name , String value ){
        addCookie( name , value , -1 );
    }

    /**
     * API copied from appjet
     * Set a cookie in the response.
     * @param cookieObject 
     * fields
     *    name (required): The name of the cookie
     *    value (required): The value of the cookie. (Note: this value will be escaped).
     *    expires (optional): If an integer, means number of days until it expires; if a Date object, means exact date on which to expire.
     *    domain (optional): The cookie domain
     *    path (optional): To restrict the cookie to a specific path.
     *    secure (optional): Whether this cookie should only be sent securely.
     *                                          
     
response.setCookie({
  name: "SessionID",
  value: "25",
  secure: true,
  expires: 14 // 14 days
});
    */

    public void setCookie( JSObject o ){
        addCookie( objectToCookie( o ) );
    }

    public static Cookie objectToCookie( JSObject o ){
        if ( o.get( "name" ) == null )
            throw new IllegalArgumentException( "name is required" );

        if ( o.get( "value" ) == null )
            throw new IllegalArgumentException( "value is required" );
        
        Cookie c = new Cookie( o.get( "name" ).toString() , o.get( "value" ).toString() );
        
        { 
            Object expires = o.get( "expires" );
            
            if ( expires instanceof Number )
                c.setMaxAge( (int)( ((Number)expires).doubleValue() * 86400 ) );
            else if ( expires instanceof Date )
                c.setMaxAge( (int)( ( ((Date)expires).getTime() - System.currentTimeMillis() ) / 1000 ) );
            else if ( expires instanceof JSDate )
                c.setMaxAge( (int)( ( ((JSDate)expires).getTime() - System.currentTimeMillis() ) / 1000 ) );

        }
        if ( o.get( "domain" ) != null )
            c.setDomain( o.get( "domain" ).toString() );

        if ( o.get( "path" ) != null )
            c.setPath( o.get( "path" ).toString() );
        else 
            c.setPath( "/" );

        if ( JSInternalFunctions.JS_evalToBool( o.get( "secure" ) ) )
            c.setSecure( true );
        
        return c;
    }
     
    /**
     * Equivalent to "addCookie( name , null , 0 )". Tells the browser
     * that the cookie with this name is already expired.
     * @param name cookie name
     */
    public void removeCookie( String name ){
        addCookie( name , "none" , 0 );
    }

    public void deleteCookie( String name ){
        removeCookie( name );
    }

    public void addCookie( Cookie cookie ){
        _cookies.add( cookie );
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

    public void setCacheable( boolean cacheable ){
        if ( cacheable ){
            setCacheTime( 3600 );
        }
        else {
            removeHeader( "Expires" );
            setHeader( "Cache-Control" , "no-cache" );
        }
    }

    public void setCacheable( Object o ){
        setCacheable( JSInternalFunctions.JS_evalToBool( o ) );
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

    public void addDateHeader( String n , long t ){
        synchronized( HeaderTimeFormat ) {
            addHeader( n , HeaderTimeFormat.format( new Date(t) ) );
        }
    }

    public Locale getLocale(){
        throw new RuntimeException( "getLocal not implemented yet" );
    }

    public void setLocale( Locale l ){
        throw new RuntimeException( "setLocale not implemented yet" );
    }

    public void reset(){
        throw new RuntimeException( "reset not allowed" );
    }

    public void resetBuffer(){
        throw new RuntimeException( "resetBuffer not allowed" );
    }

    public void flushBuffer()
        throws IOException {
        flush();
    }

    public int getBufferSize(){
        return 0;
    }

    public void setBufferSize( int size ){
        // we ignore this
    }

    public boolean isCommitted(){
        return _cleaned || _done || _sentHeader;
    }

    public void setContentType( String ct ){
        setHeader( "Content-Type" , ct );
    }

    public String getContentType(){
        return getHeader( "Content-Type" );
    }
    
    public String getContentMimeType(){
	String ct = getContentType();
	if ( ct == null )
	    return null;
	
	int idx = ct.indexOf( ";" );
	if ( idx < 0 )
	    return ct.trim();

	return ct.substring( 0 , idx ).trim();
    }

    public void clearHeaders(){
        _headers.clear();
        _useDefaultHeaders = false;
    }

    /**
     * Set a header in the response.
     * Overwrites previous headers with the same name
     * @param n the name of the header to set
     * @param v the value of the header to set, as a string
     */
    public void setHeader( String n , String v ){
        List<String> lst = _getHeaderList( n , true );
        lst.clear();
        lst.add( v );
    }

    public void addHeader( String n , String v ){
        List<String> lst = _getHeaderList( n , true );
        if ( isSingleOutputHeader( n ) )
            lst.clear();
        lst.add( v );
    }

    public void addIntHeader( String n , int v ){
        List<String> lst = _getHeaderList( n , true );
        lst.add( String.valueOf( v ) );
    }

    public void setContentLength( long length ){
        setHeader( "Content-Length" , String.valueOf( length ) );
    }

    public void setContentLength( int length ){
        setHeader( "Content-Length" , String.valueOf( length ) );
    }

    public int getContentLength(){
        return getIntHeader( "Content-Length" , -1 );
    }

    public void setIntHeader( String n , int v ){
        List<String> lst = _getHeaderList( n , true );
        lst.clear();
        lst.add( String.valueOf( v ) );
    }

    public boolean containsHeader( String n ){
        List<String> lst = _getHeaderList( n , false );
        return lst != null && lst.size() > 0;
    }

    public String getHeader( String n ){
        List<String> lst = _getHeaderList( n , false );
        if ( lst == null || lst.size() == 0 )
            return null;
        return lst.get( 0 );
    }

    public int getIntHeader( String h , int def ){
        return StringParseUtil.parseInt( getHeader( h ) , def );
    }

    private List<String> _getHeaderList( String n , boolean create ){
        List<String> lst = _headers.get( n );
        if ( lst != null || ! create )
            return lst;
        lst = new LinkedList<String>();
        _headers.put( n , lst );
        return lst;
    }

    public void removeHeader( String name ){
        _headers.remove( name );
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
                    p.first.makeThreadLocal();
                    p.second.call( p.first );
                }
                catch ( Throwable t ){
                    Logger l = Logger.getLogger( "HttpResponse" );
                    if ( p.first.get( "log" ) instanceof Logger )
                        l = (Logger)p.first.get( "log" );
                    l.error( "error running done hook" , t );
                }
                finally {
                    p.first.clearThreadLocal();
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
        
        if ( _jsfile != null ){
            if ( _jsfile.available() > 0 )
                _jsfile.cancelled();
            _jsfile = null;
        }

    }

    /**
     * @unexpose
     */
    public boolean done()
        throws IOException {
	
        if ( _cleaned )
            return true;
	
        _done = true;
        if ( _doneTime <= 0 )
            _doneTime = System.currentTimeMillis();
	
	if ( ! _sentHeader )
	    _gzip = useGZIP();

        boolean f = flush();
        if ( f )
            cleanup();
        return f;
    }

    public String encodeRedirectURL( String loc ){
        return loc;
    }

    public String encodeRedirectUrl( String loc ){
        return loc;
    }

    public String encodeUrl( String loc ){
        return loc;
    }

    public String encodeURL( String loc ){
        return loc;
    }

    public void redirect( String loc ){
        sendRedirectTemporary( loc );
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

    public void sendRedirect( String loc ){
        sendRedirectTemporary( loc );
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
            _handler.write( headOut );
            _keepAlive = keepAlive();
            _sentHeader = true;
        }

        if ( _writer != null ){
            _writer._push();
            _charBufPool.done( _writer._cur );
            _writer._cur = null;
        }

        if (!_request.getMethod().equals("HEAD")) {
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
                    //_dataSent += _fileChannel.transferTo( _dataSent , Long.MAX_VALUE , _handler.getChannel() );
                    _dataSent += _handler.transerFile( _fileChannel , _dataSent , Long.MAX_VALUE );
                }
                catch ( IOException ioe ){
                    if ( ioe.toString().indexOf( "Resource temporarily unavailable" ) < 0 )
                        throw ioe;
                }
                if ( _dataSent < _file.length() ){
                    if ( HttpServer.D ) System.out.println( "only sent : " + _dataSent );
                    _handler._inFork = false;
                    _handler.registerForWrites();
                    return false;
                }
            }


            if ( _stringContent != null ){
                for ( ; _stringContentSent < _stringContent.size() ; _stringContentSent++ ){
                    
                    ByteBuffer bb = _stringContent.get( _stringContentSent );
                    int thisTime = _handler.write( bb );
                    _stringContentPos += thisTime;
                    _dataSent += thisTime;
                    if ( _stringContentPos < bb.limit() ){
                        if ( HttpServer.D ) System.out.println( "only wrote " + _stringContentPos + " out of " + bb );
                        _handler._inFork = false;
                        _handler.registerForWrites();
                        return false;
                    }
                    _stringContentPos = 0;
                }
            }
            
            if ( _jsfile != null ){
                if ( ! _jsfile.write( _handler ) ){
                    _dataSent = _jsfile.bytesWritten();
                    
                    _handler._inFork = false;
                    
                    if ( _jsfile.pause() )
                        _handler.pause();
                    else
                        _handler.registerForWrites();
                    
                    return false;
                }
                _dataSent = _jsfile.bytesWritten();
            }
        }

        cleanup();

        if ( keepAlive() && ! _handler.hasData() )
            _handler.registerForReads();
        else
            _handler.registerForWrites();

        return true;
    }
    
    long dataSent(){
        return _dataSent;
    }

    void socketClosing(){
        if ( _cleaned )
            return;

        // uh-oh
        cleanup();
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

        if ( _useDefaultHeaders )
            a.append( SERVER_HEADERS );

        // headers
        if ( _headers != null ){
            List<String> headers = new ArrayList<String>( _headers.keySet() );
            Collections.sort( headers );
            for ( int i=headers.size()-1; i>=0; i-- ){
                final String h = headers.get( i );
                List<String> values = _headers.get( h );
                for ( int j=0; j<values.size(); j++ ){
                    a.append( h );
                    a.append( ": " );
                    a.append( values.get( j ) );
                    a.append( "\r\n" );
                }
            }
        }

        // cookies
        for ( Cookie c : _cookies ){
            a.append( "Set-Cookie: " );
            a.append( c.getName() ).append( "=" ).append( c.getValue() ).append( ";" );
            if ( c.getPath() != null )
                a.append( " Path=" ).append( c.getPath() ).append( ";" );
            if ( c.getDomain() != null )
                a.append( " Domain=" ).append( c.getDomain() ).append( ";" );
            String expires = CookieUtil.getExpires( c );
            if ( expires != null )
                a.append( " Expires=" ).append( expires ).append( "; " );
            a.append( "\r\n" );
        }

        if ( keepAlive() )
            a.append( "Connection: keep-alive\r\n" );
        else
            a.append( "Connection: close\r\n" );

        if ( _writer != null )
            _writer._push();

        if ( _headers.get( "Content-Length") == null ){
            long cl = dataSize();
            if ( cl >= 0 ){
                a.append( "Content-Length: " ).append( String.valueOf( cl ) ).append( "\r\n" );
            }
        }

        // empty line
        a.append( "\r\n" );
        return a;
    }
    
    /**
     * @return size or -1 if i don't know
     */
    long dataSize(){
        if ( _headers.containsKey( "Content-Length" ) )
            return getContentLength();
        
        if ( _stringContent != null ){
            int cl = 0;
            for ( ByteBuffer buf : _stringContent )
                cl += buf.limit();
            return cl;
        }
        
        if ( _numDataThings() == 0 )
            return 0;

        return -1;
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

    public void write( String s ){
        getJxpWriter().print( s );
    }

    /**
     * @unexpose
     */
    public JxpWriter getJxpWriter(){
        if ( _writer == null ){
            if ( _cleaned )
                throw new RuntimeException( "already cleaned" );

            _writer = new MyJxpWriter();
        }
        return _writer;
    }

    public PrintWriter getWriter(){
        if ( _printWriter == null ){
            final JxpWriter jxpWriter = getJxpWriter();
            _printWriter = new PrintWriter( new Writer(){

                    public void close(){
                    }

                    public void flush(){
                    }

                    public void write(char[] cbuf, int off, int len){
                        jxpWriter.print( new String( cbuf , off , len ) );
                    }

                } , true );
        }
        return _printWriter;
    }

    public ServletOutputStream getOutputStream(){
        if ( _outputStream == null ){
            final JxpWriter jxpWriter = getJxpWriter();
            _outputStream = new ServletOutputStream(){
                    public void write( int b ){
                        jxpWriter.write( b );
                    }
                };
        }
        return _outputStream;
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

        if ( _sentHeader ){
            return _keepAlive;
        }

        if ( ! _handler.allowKeepAlive() )
            return false;

        if ( ! _request.keepAlive() ){
            return false;
        }
        
        if ( _headers.get( "Content-Length" ) != null ){
            return true;
        }
	
        if ( _stringContent != null ){
            // TODO: chunkinga
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

    public void setCharacterEncoding( String encoding ){
        throw new RuntimeException( "setCharacterEncoding not supported" );
    }

    public String getCharacterEncoding(){
        return getContentEncoding();
    }

    /**
     * Sends a file to the browser.
     * @param f a file to send
     */
    public void sendFile( File f ){
        if ( ! f.exists() )
            throw new IllegalArgumentException( "file doesn't exist" );
        _file = f;
        setContentType( MimeTypes.get( f ) );
        setContentLength( f.length() );
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

        if ( f.getFileName() != null && getHeader( "Content-Disposition" ) == null ){
            setHeader( "Content-Disposition" , f.getContentDisposition() + "; filename=\"" + f.getFileName() + "\"" );
        }

        long length = f.getLength();
        sendFile( f.sender() );

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
            setContentLength( 1 + range[1] - range[0] );
            System.out.println( "got range " + range[0] + " -> " + range[1] );


            return;
        }
        setContentLength( f.getLength() );
        setContentType( f.getContentType() );
    }

    public void sendFile( JSFile.Sender sender ){
        _jsfile = sender;
        _stringContent = null;
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

    public long handleTime(){
        long end = _doneTime;
        if ( end <= 0 )
            end = System.currentTimeMillis();
        return end - _request._startTime;
    }
    
    public int hashCode( IdentitySet seen ){
        return System.identityHashCode(this);
    }

    boolean useGZIP(){
	if ( ! _done )
	    return false;
	
	if ( ! _request.gzip() )
	    return false;
	
	if ( ! GZIP_MIME_TYPES.contains( getContentMimeType() ) )
	    return false;

	if ( _stringContent != null && _stringContent.size() > 0 && _stringContentSent == 0 && _stringContentPos == 0 ){

	    if ( _writer != null ){
		_writer._push();
                _charBufPool.done( _writer._cur );
		_writer = null;
	    }

	    if ( _stringContent.get(0).limit() < MIN_GZIP_SIZE )
		return false;

	    setHeader("Content-Encoding" , "gzip" );
	    setHeader("Vary" , "Accept-Encoding" );
	    
	    List<ByteBuffer> zipped = ZipUtil.gzip( _stringContent , _bbPool );
	    
	    for ( ByteBuffer buf : _stringContent )
		_bbPool.done( buf );
	    _stringContent = zipped;
	    _myStringContent = zipped;
	    
	    long length = 0;
	    for ( ByteBuffer buf : _stringContent ){
		length += buf.remaining();
	    }
	    setContentLength( length );
	    return true;
	}
	
	return false;
    }

    final HttpRequest _request;
    final HttpServer.HttpSocketHandler _handler;

    // header
    int _responseCode = 200;
    Map<String,List<String>> _headers = new StringMap<List<String>>();
    boolean _useDefaultHeaders = true;
    List<Cookie> _cookies = new ArrayList<Cookie>();
    boolean _sentHeader = false;
    private boolean _keepAlive = false;
    boolean _gzip = false;

    // data
    List<ByteBuffer> _stringContent = null;
    private List<ByteBuffer> _myStringContent = null; // tihs is the real one

    int _stringContentSent = 0;
    int _stringContentPos = 0;

    File _file;
    FileChannel _fileChannel;
    long _dataSent = 0;

    boolean _done = false;
    long _doneTime = -1;
    boolean _cleaned = false;
    MyJxpWriter _writer = null;
    PrintWriter _printWriter;
    ServletOutputStream _outputStream;

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

	public void write( int b ){
	    
	    if ( _done )
		throw new RuntimeException( "already done" );
	    
	    if ( b < Byte.MIN_VALUE || b  > Byte.MAX_VALUE )
		throw new RuntimeException( "what?" );
	    
	    if ( _cur.remaining() == 0 )
		_push();

	    if ( _mode == OutputMode.STRING ){
		_push();
		_mode = OutputMode.BYTES;
		_push();
	    }

	    byte real = (byte)( b & 0xFF );
	    _raw.put( real );
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
            if ( _done )
                throw new RuntimeException( "already done" );
	    
	    if ( _mode == OutputMode.BYTES ){
		_push();
		_mode = OutputMode.STRING;
	    }

            if ( s == null )
                s = "null";

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
	    
	    if ( _mode == OutputMode.BYTES ){
		if ( _raw != null ){
		    if ( _raw.position() == 0 )
			return;
		    
		    _raw.flip();
		    _myStringContent.add( _raw );
		}
		_raw = USE_POOL ? _bbPool.get() : ByteBuffer.wrap( new byte[ _cur.limit() * 2 ] );
		return;
	    }

            if ( _cur == null || _cur.position() == 0 )
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
	private ByteBuffer _raw;
        private int _mark = 0;

        private int _spot = -1;
        private boolean _inSpot = false;

	private OutputMode _mode = OutputMode.STRING;
    }

    enum OutputMode { STRING, BYTES };
    
    static final int CHAR_BUFFER_SIZE = 1024 * 128;
    static final int MAX_STRING_SIZE = CHAR_BUFFER_SIZE / 4;
    static SimplePool<CharBuffer> _charBufPool = new WatchedSimplePool<CharBuffer>( "Response.CharBufferPool" , 50 , -1 ){
        public CharBuffer createNew(){
            return CharBuffer.allocate( CHAR_BUFFER_SIZE );
        }

        protected long memSize( CharBuffer cb ){
            return CHAR_BUFFER_SIZE * 2;
        }

        public boolean ok( CharBuffer buf ){

            if ( buf == null )
                return false;

            buf.position( 0 );
            buf.limit( buf.capacity() );
            return true;
        }
    };
    static ByteBufferPool _bbPool = new ByteBufferPool( "HttpResponse" , 50 , CHAR_BUFFER_SIZE * 4  );
    static StringBuilderPool _headerBufferPool = new StringBuilderPool( "HttpResponse" , 25 , 1024 );
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

    static boolean isSingleOutputHeader( String name ){
        return SINGLE_OUTPUT_HEADERS.contains( name.toLowerCase() );
    }

    static final Set<String> SINGLE_OUTPUT_HEADERS;
    static {
        Set<String> s = new HashSet<String>();
        s.add( "content-type" );
        s.add( "content-length" );
        s.add( "date" );
        SINGLE_OUTPUT_HEADERS = Collections.unmodifiableSet( s );
    }
}
