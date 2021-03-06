// XMLHttpRequest.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.js;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import javax.servlet.http.*;

import ed.io.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.net.*;
import ed.net.httpclient.*;

/** @expose */
public class XMLHttpRequest extends JSObjectBase {

    /** @unexpose */
    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.XHR" );

    /** Unsent request value (0) */
    public static final int UNSENT = 0;
    /** Opened request value (1) */
    public static final int OPENED = 1;
    /** Headers received request value (2) */
    public static final int HEADERS_RECEIVED = 2;
    /** Loading request value (3) */
    public static final int LOADING = 3;
    /** Done request value (4) */
    public static final int DONE = 4;

    /** @unexpose */
    public final static JSFunction _cons = new JSFunctionCalls3(){

            public JSObject newOne(){
                return new XMLHttpRequest();
            }

            public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                return open( s , methodObj , urlObj , asyncObj , args );
            }

            public Object open( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){

                XMLHttpRequest r = null;
                if ( s.getThis() instanceof XMLHttpRequest )
                    r = (XMLHttpRequest)s.getThis();

                if ( r == null )
                    r = new XMLHttpRequest();

                if ( urlObj != null )
                    r.open( methodObj , urlObj , asyncObj );

                return r;
            }

            protected void init(){

                _prototype.set( "open" , new JSFunctionCalls3() {
                        public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                            return open( s , methodObj , urlObj , asyncObj , args );
                        }
                    } );

                _prototype.set( "getJSON" , new JSFunctionCalls0(){
                        public Object call( Scope s , Object [] extra ){
                            XMLHttpRequest r = (XMLHttpRequest)s.getThis();
                            return r.getJSON();
                        }
                    } );

                _prototype.dontEnumExisting();
                _prototype.lock();
                this.lock();
            }
        };

    /** Create an XML HTTP request */
    public XMLHttpRequest(){
        super( _cons );
        
        _cookieJar = new CookieJar();
        _cookieJarDelegate = new CookieJarDelegate();
        _headersToSend = new HashMap<String,String>();
    }

    public XMLHttpRequest( URL url ){
        this( "GET" , url.toString() , false );
    }

    /** Create an XML HTTP request, setting the handler, url, and if it is asynchronous
     * @param method Method to handle the response.
     * @param url URL to which to send HTTP request.
     */
    public XMLHttpRequest( String method , String url ){
        this( method , url , false );
    }

    /** Create an XML HTTP request, setting the handler, url, and if it is asynchronous
     * @param method Method to handle the response.
     * @param url URL to which to send HTTP request.
     * @param aysnc If the request should be asynchronous.
     */
    public XMLHttpRequest( String method , String url , boolean async ){
        this();
        init( method , url , async );
    }

    /** Initialize this request.
     * @param method Method to handle the response.
     * @param url URL to which to send HTTP request.
     * @param aysnc If the request should be asynchronous.
     */
    void open( Object method , Object url , Object async ){
        init( method.toString() , url.toString() , JSInternalFunctions.JS_evalToBool( async ) );
    }

    /** Initialize this request.
     * @param method Method to handle the response.
     * @param url URL to which to send HTTP request.
     * @param aysnc If the request should be asynchronous.
     */
    void init( String method , String url , boolean async ){

        if ( ! url.substring( 0 , 4 ).equalsIgnoreCase( "http" ) )
            url = "http://" + url;

        _method = method;
        _urlString = url;
        _async = async;

        set( "method" , _method );
        set( "url" , _urlString );
        set( "async" , _async );
        setStatus( UNSENT );
    }

    /** Set the readyState of the request to the given value. If the status is greater than 0
     * and onreadystatechange is set, the onreadystatechange function is called.
     * @param status The status of the request.
     */
    void setStatus( int status ){
        set( "readyState" , status );

        JSFunction onreadystatechange = getFunction( "onreadystatechange" );
        if ( onreadystatechange != null && status > 0 )
            onreadystatechange.call( _onreadystatechangeScope , null );

    }

    /** Sets the property <tt>n</tt> of this to <tt>v</tt>.  If <tt>n</tt> is "onreadystatechange",
     * and <tt>v</tt> is a function, this sets the scope of the onreadystatechange to the function's scope.
     * @param n Key to set.
     * @param v Value to set.
     * @return <tt>v</tt>
     */
    public Object set( Object n , Object v ){
        if ( n.equals( "onreadystatechange" ) ){

            JSFunction f = (JSFunction)v;

            if ( f != null )
                _async = true;

            _onreadystatechangeScope = f.getScope().child();
            _onreadystatechangeScope.setThis( this );

            return super.set( n , v );
        }
        
        if( n.equals( "cookies" ) ) {
            throw new UnsupportedOperationException( "cookies is a readonly property, please use the setCookieJar method to change the cookie jar");
        }

        return super.set( n , v );
    }
    
    public Object get(Object n) {
        if( n.equals( "cookies" ) )
            return _cookieJarDelegate;

        return super.get( n );
    }

    /** Send an XML HTTP request.
     * @return The XML HTTP request
     */
    public XMLHttpRequest send()
        throws IOException {
        return send( null );
    }

    /** Send an XML HTTP request with the given text.
     * @param post The text to be send with the request.
     * @return The XML HTTP request
     */
    public XMLHttpRequest send( final String post )
        throws IOException {

        if ( ! _async )
            return _doSend( post );

        final Thread t = new Thread( "XMLHttpRequest-sender" ){
                public void run(){
                    try {
                        if ( DEBUG ) System.out.println( "starting async" );
                        _doSend( post );
                        if ( DEBUG ) System.out.println( "done with async" );
                    }
                    catch ( Exception e ){
                        e.printStackTrace();
                    }
                }
            };

        t.start();
        return this;
    }

    private XMLHttpRequest _doSend( String post )
        throws IOException {

        try {
            setStatus( OPENED );
            Handler handler = new Handler( post );
            HttpClient.download( _checkURL() , handler );
        }
        catch ( IOException ioe ){
            set( "error" , ioe );
            return null;
        }
        finally {
            setStatus( DONE );
        }

        return this;
    }

    public IOException getError(){
        Object err = get( "error" );
        if ( err == null )
            return null;
        if ( err instanceof IOException )
            return (IOException)err;
        return new IOException( "weird XMLHttpRequest error : " + err );
    }

    public int getResponseCode(){
        return ((Number)(get("status"))).intValue();
    }

    /** Gets the length of the header.
     * @param Buffer containing this request string.
     * @return The number of characters in first line of the request.
     */
    int startsWithHeader( ByteBuffer buf ){
        boolean lastNewLine = false;

        int max =
            buf.limit() == buf.capacity() ?
            buf.position() :
            buf.limit();

        for ( int i=0; i < max; i++ ){
            char c = (char)(buf.get(i));

            if ( c == '\r' )
                continue;

            if ( c == '\n' ){
                if ( lastNewLine )
                    return i + 1;
                lastNewLine = true;
                continue;
            }

            lastNewLine = false;
        }
        return -1;
    }

    /** Returns the method the response should be sent to and the URL, separated by a space.
     * @return "handler url"
     */
    public String toString(){
        return _method + " " + _urlString;
    }


    /** Add a label/value pair to the request headers.
     * @param label Header label.
     * @param value Header value.
     */
    public void setRequestHeader(String label, String value) {
        if ( label.equalsIgnoreCase( "connection" ) )
            return;
        _headersToSend.put( label , value );
    }

    /** Get the path and query parts of the URL, if they exist.
     * @return path?query
     */
    public String getLocalURL(){
        _checkURL();

        String path = _url.getPath();
        if ( path.length() == 0 )
            path = "/";

        if ( _url.getQuery() == null )
            return path;
        return path + "?" + _url.getQuery();
    }

    /** Gets the hostname of this request's destination URL, if possible.
     * @return The hostname.
     */
    public String getHost(){
        _checkURL();
        return _url.getHost();
    }

    /** Get the port to connect to, depending on if the connection is secure.  For secure connections, this uses port 443.  For unsecure, this uses port 80.
     * @return Port number.
     */
    public int getPort(){
        _checkURL();
        int port = _url.getPort();
        if ( port > 0 )
            return port;

        return isSecure() ? 443 : 80;
    }

    /** Returns if the url is an https
     * @return If the url is an https
     */
    public boolean isSecure(){
        return _urlString.startsWith( "https:/" );
    }

    /** If the response text is in json form, returns the object that it represents.
     * @return An object parsed from the response text.
     */
    public Object getJSON(){
        Object r = get( "responseText" );
        if ( r == null )
            throw new JSException( "no 'responseText' " );
        return JSON.parse( r.toString() );
    }

    public String getResponseText(){
        Object r = get( "responseText" );
        if ( r == null )
            return null;
        return r.toString();
    }

    private URL _checkURL(){
        if ( _urlString == null || _urlString.trim().length() == 0 )
            throw new JSException( "no url" );

        if ( _url == null ){
            try {
                _url = new URL( _urlString );
            }
            catch ( MalformedURLException e ){
                throw new JSException( "bad url [" + _urlString + "]" );
            }
        }
        return _url;
    }

    public CookieJar getCookieJar() {
        return _cookieJar;
    }
    public void setCookieJar( CookieJar cookieJar ) {
        _cookieJar = cookieJar;
    }

    class Handler implements HttpResponseHandler {

        Handler( String postData ){
            this( postData == null ? null : postData.getBytes() );
        }

        Handler( byte[] postData ){
            _postData = postData;
            lastUrl = _checkURL();
        }

        public int read( InputStream is )
            throws IOException {

            set( "header" , new JSString( _header.toString() ) );

            setStatus( LOADING );
            ByteArrayOutputStream bout = new ByteArrayOutputStream( _contentLength > 0 ? _contentLength : 128 );
            StreamUtil.pipe( is , bout );
            byte[] data = bout.toByteArray();
            set( "responseText" , new String( data , _contentEncoding ) );
            return data.length;
        }

        public void gotHeader( String name , String value ){

            boolean firstLine = name.equals( "FIRST_LINE" );


            if ( ! firstLine )
                _header.append( name ).append( ": " );
            _header.append( value ).append( "\n" );

            if ( firstLine ){
                String statusText = value;
                int idx = statusText.indexOf( " " );
                if ( idx > 0 ){
                    statusText = statusText.substring( idx + 1 ).trim();
                    idx = statusText.indexOf( " " );
                    if ( idx > 0 ){
                        statusText = statusText.substring( idx + 1 ).trim();
                    }
                }

                set( "statusText" , new JSString( statusText ) );
            }

            JSObject headers = (JSObject)get( "headers" );
            if ( headers == null ){
                headers = new JSObjectBase();
                set( "headers" , headers );
            }
            headers.set( name , new JSString( value ) );
        }

        public void removeHeader( String name ){}

        public void gotResponseCode( int responseCode ){
            //Need to reset state in case of redirect
            _header.setLength( 0 );
            set( "headers", null );

            set( "status" , responseCode );
            setStatus( HEADERS_RECEIVED );
        }

        public void gotContentLength( int contentLength ){
            _contentLength = contentLength;
            set( "contentLength" , contentLength );
        }

        public void gotContentEncoding( String contentEncoding ){
            _contentEncoding = contentEncoding;
            set( "contentEncoding" , contentEncoding );
        }

        public void gotCookie( Cookie c ){
            CookieJar cookieJar = getCookieJar();
            if( cookieJar == null ) {
                cookieJar = new CookieJar();
                setCookieJar( cookieJar );
            }
            
            cookieJar.addCookie( lastUrl , c );
        }
        
        public void setFinalUrl( URL url ){
            set( "finalURL" , url.toString() );
            lastUrl = url;
        }

        public boolean followRedirect( URL url ){
            if( JSInternalFunctions.JS_evalToBool( get( "nofollow" ) ) )
                return false;

            lastUrl = url;
            return true;
        }
        
        public boolean wantHttpErrorExceptions () {
            return false;
        }
        
        public Map<String,String> getHeadersToSend(){
            return _headersToSend;
        }

        public Map<String,Cookie> getCookiesToSend(){
            if( getCookieJar() == null )
                return null;
            
            return getCookieJar().getActiveCookies( _checkURL() );
        }

        public byte[] getPostDataToSend(){
            return _postData;
        }

        public String getMethodToUse() {
            return _method;
        }

        public long getDesiredTimeout(){
            Object foo = get( "timeout" );
            if ( ! ( foo instanceof Number ) )
                return -1;
            return ((Number)foo).longValue();
        }

        final byte[] _postData;
        int _contentLength = 0;
        String _contentEncoding = "UTF8";
        StringBuilder _header = new StringBuilder();
        private URL lastUrl;
    }

    class CookieJarDelegate implements JSObject {
        public Object get(Object n) {
            String name = n.toString();
            
            if( containsKey( name ) )
                return _cookieJar.getAll().get( name ).getValue();
            else
                return null;
        }
        public Object getInt(int n) {
            return get( String.valueOf( n ) );
        }
        
        public Object set(Object n, Object v) {
            URL url = _checkURL();
            Cookie c = new Cookie( n.toString(), v.toString() );
            c.setDomain( url.getHost() );
            c.setPath( url.getPath() );
            
            _cookieJar.addCookie( url , c );
            return v;
        }
        public Object setInt(int n, Object v) {
            return set( String.valueOf( n ), v);
        }
        
        public Set<String> keySet() {
            return _cookieJar.getAll().keySet();
        }
        public Set<String> keySet(boolean includePrototype) {
            return keySet();
        }
        public boolean containsKey(String s) {
            return _cookieJar.getAll().containsKey( s );
        }
        public boolean containsKey(String s, boolean includePrototype) {
            return containsKey( s );
        }
        public Object removeField(Object n) {
            String name = n.toString();
            Cookie removed = _cookieJar.remove( name );
            
            return (removed == null)? null : removed.getName();
        }
        
        public JSFunction getConstructor() {
            return null;
        }
        public JSFunction getFunction(String name) {
            return null;
        }
        public JSObject getSuper() {
            return null;
        }

    }

    String _method;
    String _urlString;
    boolean _async;
    Map<String,String> _headersToSend;
    CookieJar _cookieJar;
    CookieJarDelegate _cookieJarDelegate;

    URL _url;
    Scope _onreadystatechangeScope;
}
