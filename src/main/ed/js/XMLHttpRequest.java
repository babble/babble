// XMLHttpRequest.java

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

package ed.js;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import javax.net.ssl.*;
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
    }

    /** Create an XML HTTP request, setting the handler, url, and if it is asynchronous
     * @param method Method to handle the response.
     * @param url URL to which to send HTTP request.
     * @param aysnc If the request should be asynchronous.
     */
    public XMLHttpRequest( String method , String url , boolean async ){
        super( _cons );
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
        String name = n.toString();

       if ( n.equals( "onreadystatechange" ) ){

           JSFunction f = (JSFunction)v;

           if ( f != null )
               _async = true;

           _onreadystatechangeScope = f.getScope().child();
           _onreadystatechangeScope.setThis( this );

           return super.set( n , v );
        }

       return super.set( n , v );
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

    class Handler implements HttpResponseHandler {

        Handler( String postData ){
            this( postData == null ? null : postData.getBytes() );
        }

        Handler( byte[] postData ){
            _postData = postData;
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

        public void gotCookie( Cookie c ){}

        public void setFinalUrl( URL url ){
            set( "finalURL" , url.toString() );
        }

        public boolean followRedirect( URL url ){
            return true;
        }

		public boolean wantHttpErrorExceptions () {
			return false;
		}

        public Map<String,String> getHeadersToSend(){
            return _headersToSend;
        }

        public Map<String,Cookie> getCookiesToSend(){
            return null;
        }

        public byte[] getPostDataToSend(){
            return _postData;
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
    }


    String _method;
    String _urlString;
    boolean _async;
    Map<String,String> _headersToSend = new HashMap<String,String>();

    URL _url;
    Scope _onreadystatechangeScope;
}
