// HttpRequest.java

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

import java.net.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;

/**
 * Class to represent an HTTP request 
 *
 * @expose
 */
public class HttpRequest extends JSObjectLame {
    
    /**
     * Generate a "dummy" request, coming from a browser trying to access the
     * given URL.
     * @param url a URL
     */
    public static HttpRequest getDummy( String url ){
        return getDummy( url , "" );
    }

    /**
     * Generate a "dummy" request, coming from a browser trying to access a
     * URL with some additional headers.
     * @param url a URL
     * @param extraHeaders a set of headers in HTTP format, separated by "\n"
     */
    public static HttpRequest getDummy( String url , String extraHeaders ){
        return new HttpRequest( null , "GET " + url + " HTTP/1.0\n" + extraHeaders + "\n" );
    }

    /**
     * Read a HttpRequest from a SocketHandler.
     */
    HttpRequest( HttpServer.HttpSocketHandler handler , String header ){
        _handler = handler;
        _rawHeader = header;

        int idx = header.indexOf( "\n" );
        if ( idx < 0 )
            throw new RuntimeException( "something is very wrong" );
        
        _firstLine = header.substring( 0 , idx ).trim();
        
        int start = idx + 1;
        while ( ( idx = header.indexOf( "\n" , start ) ) >= 0 ) {
            final String line = header.substring( start , idx ).trim();
            start = idx + 1;
            int foo = line.indexOf( ":" );
            if ( foo > 0 )
                _headers.put( line.substring( 0 , foo ).trim() , 
                              line.substring( foo + 1 ).trim() );
        }
        
        // parse first line
        idx = _firstLine.indexOf( " " );
        if ( idx < 0 )
            throw new RuntimeException( "malformed" );
        
        _command = _firstLine.substring( 0 , idx );
        int endURL = _firstLine.indexOf( " " , idx + 1 );
        if ( endURL < 0 ){
            _url = _firstLine.substring( idx + 1 ).trim();
            _http11 = false;
        }
        else {
            _url = _firstLine.substring( idx + 1 , endURL ).trim();
            _http11 = _firstLine.indexOf( "1.1" , endURL ) > 0;
        }

        int endURI = _url.indexOf( "?" );
        if ( endURI < 0 ){
            _uri = Encoding._unescape( _url );
            _queryString = null;
        }
        else {
            _uri = Encoding._unescape( _url.substring( 0 , endURI ) );
            _queryString = _url.substring( endURI + 1 );
        }
    }

    /**
     * Get the URI of the request (the path without hostname or query
     * arguments).
     */
    public String getURI(){
        return _uri;
    }

    /**
     * Get the URL of the request (the path and query arguments without
     * hostname).
     */
    public String getURL(){
        return _url;
    }
    
    /**
     * Gets the request's entire HTTP header as an unparsed string.
     * @return the entire HTTP header
     */
    public String getRawHeader(){
        return _rawHeader;
    }

    /**
     * Gets the HTTP method used in making this request.
     * This will be either GET, POST, HEAD, etc...
     * @return the HTTP method used
     */
    public String getMethod(){
        return _command;
    }
    
    /**
     * Returns the query string of a request as an unparsed string.
     * For a request with URL "/foo?a=1", this method would return "a=1".
     * @return the query part of the request,  null if there was none.
     */
    public String getQueryString(){
        return _queryString;
    }
    
    /**
     * Gets the total size of the HTTP request.
     * @return the total size of the HTTP message, including header and body
     */
    public int totalSize(){
        int size = _rawHeader.length();
        size += getIntHeader( "Content-Length" , 0 );
        return size;
    }

    /**
     * Returns the request in an approximation of HTTP protocol.
     */
    public String toString(){
        _finishParsing();
        return _command + " " + _uri + " HTTP/1." + ( _http11 ? "1" : "" ) + " : " + _headers + "  " + _urlParameters + " " + _postParameters;
    }

    /**
     * @unexpose
     */
    public boolean keepAlive(){
        String c = getHeader( "connection" );
        if ( c != null )
            return ! c.equalsIgnoreCase( "close" );
        return _http11;
    }
    
    // header stuff

    /**
     * Returns the hostname this request was directed at. This works by using
     * the Host header (mandated by HTTP/1.1), and subtracting any port
     * information (if any).
     * @return the host to which this request was sent as a string
     */
    public String getHost(){
        String host = getHeader( "Host" );
        if ( host == null )
            return null;
        host = host.trim();
        if ( host.length() == 0 )
            return null;

        int idx = host.indexOf( ":" );
        if ( idx == 0 )
            return null;
        if ( idx > 0 ){
            host = host.substring( 0 , idx ).trim();
            if ( host.length() == 0 )
                return null;
        }
        
        return host;
    }
    
    /**
     * Returns the port this request was directed at. This works by using the
     * Host header (mandated by HTTP/1.1), and subtracting the hostname.
     * @return the port this request was for
     */
    public int getPort(){
        String host = getHeader( "Host" );
        if ( host == null )
            return 0;

        int idx = host.indexOf( ":" );
        if ( idx < 0 )
            return 0;
        
        return StringParseUtil.parseInt( host.substring( idx + 1 ) , 0 );
    }
    

    /**
     * Gets a raw HTTP header specified by the parameter.
     * @param h the name of an HTTP header. Case insensitive.
     * @return the header as a string
     */
    public String getHeader( String h ){
        return _headers.get( h );
    }

    /**
     * Gets a http header parsed into an int.
     * If the header wasn't sent or can't be parsed as an int, def is returned.
     * @param h the name of an HTTP header. Case insensitive.
     * @return the header parsed as an int
     */
    public int getIntHeader( String h , int def ){
        return StringParseUtil.parseInt( getHeader( h ) , def );
    }

    /**
     * Get every HTTP header that was sent as an array of header names.
     * @return an array of HTTP header names
     */
    public JSArray getHeaderNames(){
        JSArray a = new JSArray();
        a.addAll( _headers.keySet() );
        return a;
    }

    // cookies


    /**
     * Get the value for a cookie, specified by name.
     * @param s a cookie name
     * @return the cookie value.  null if it doesn't exist
     */
    public String getCookie( String s ){
        if ( _cookies == null ){
            JSObjectBase m = new JSObjectBase();
            String temp = getHeader( "Cookie" );
            if ( temp != null ){

                for ( String thing : temp.split( ";" ) ){
                    
                    int idx = thing.indexOf("=");
                    
                    if ( idx < 0 )
                        continue;

                    m.set( thing.substring( 0 , idx ).trim() , 
                           thing.substring( idx + 1 ).trim() );
                }
            }
            _cookies = m;
        }
        return _cookies.getAsString( s );
    }


    /**
     * Gets the names of all cookies sent in this request.
     * @return array of cookie names
     */
    public JSArray getCookieNames(){
        getCookie( "" );
        JSArray a = new JSArray();
        a.addAll( _cookies.keySet() );
        return a;
    }

    /**
     * Expose cookies as a JavaScript object.
     * This is so you can request.getCookies().username
     * NOTE: you can't add cookies using this object.
     * @return all cookies as a JavaScript obejct
     */
    public JSObject getCookies(){
        getCookie( "" );
        return _cookies;
    }
    
    // param stuff

    /** 
     * Gets all the parameter names specified in this request.
     * This includes both GET and POST parameters.
     * @return an array of parameter names
     */
    public JSArray getParameterNames(){
        _finishParsing();
        
        JSArray a = new JSArray();

        for ( String s : _urlParameters.keySet() )
            a.add( new JSString( s ) );

        for ( String s : _postParameters.keySet() ){
            JSString js = new JSString( s );
            if ( ! a.contains( js ) )
                a.add( js );
        }
        
        return a;
    }
    
    /**
     * Parses the parameter specified by n as a boolean.
     * If it doesn't exist, or can't be parsed as a boolean, def is returned.
     * @param n the parameter to look up
     * @return true/false if the paramater was specified, or def otherwise
     */
    public boolean getBoolean( String n , boolean def ){
        return StringParseUtil.parseBoolean( getParameter( n ) , def );
    }

    /**
     * Parses the parameter specified by n as an int.
     * If it doesn't exist, or can't be parsed as an int, def is returned.
     * @param n the parameter to look up.
     * @return number if parameter is specified, or def otherwise
     */
    public int getInt( String n , int def ){
        return StringParseUtil.parseInt( getParameter( n ) , def );
    }
    
    List<String> _getParameter( String name ){
        List<String> l = _postParameters.get( name );
        if ( l != null )
            return l;
        return _urlParameters.get( name );
    }
    

    /**
     * Return all the parameters given for a key.
     * This is used when you have multiple values for the same parameter.
     * i.e. if this request was created with the URL "/foo?a=1&a=2",
     * calling getParameters("a") would return [ "1" , "2" ].
     * @param name a parameter name
     * @return array of all values associated with this parameter
     */
    public JSArray getParameters( String name ){
        List<String> lst = _getParameter( name );
        if ( lst == null )
            return null;
        
        JSArray a = new JSArray();
        for ( String s : lst )
            a.add( new JSString( s ) );
	return a;
    }

    /**
     * Return any value for the parameter specified by name.
     * This returns the "first" value for this parameter.
     * @return string value of the parameter specified by name, null if none
     */
    public String getParameter( String name ){
        return getParameter( name , null );
    }

    /**
     * Return any value for the parameter specified by name.
     * This returns the "first" value for the parameter, or def if this
     * parameter was not given.
     * @param name the name of the parameter to look up
     * @param def a default in case the parameter wasn't given
     * @return string value of the parameter specified by name, def if
     * none
     */
    public String getParameter( String name , String def ){
        _finishParsing();
        List<String> s = _getParameter( name );
        if ( s != null && s.size() > 0 )
            return s.get(0);
        return def;
    }

    // -------

    /**
     * Gets all values for the parameter specified by name from either
     * the query arguments or the POST arguments.  If post is true,
     * get all the values from the POST; otherwise, get them from the
     * query arguments.
     * @param name the name of a parameter to look up
     * @param post true to consult the POST; false to consult the URL
     * @return an array of parameter values as strings
     */
    public JSArray getParameters( String name , boolean post ){
        List<String> lst = post ? _postParameters.get( name ) : _urlParameters.get( name );
        if ( lst == null )
            return null;
        
        JSArray a = new JSArray();
        for ( String s : lst )
            a.add( new JSString( s ) );
	return a;
    }

    /**
     * Gets one value for a parameter specified by name from either the
     * query arguments or the POST arguments. Returns null if none was found.
     * @param name the name of the parameter to look up
     * @param post true to consult the POST; false to consult the URL
     * @return a parameter value as a string
     */
    public String getParameter( String name , boolean post ){
        return getParameter( name , null , post  );
    }

    /**
     * Gets one value for a parameter specified by name from either
     * the query arguments or the POST arguments, defaulting to def if
     * none was found.
     * @param name the name of the parameter to look up
     * @param def the default to use if none is found
     * @param post true to consult the POST; false to consult the URL
     * @return a parameter value as a string
     */
    public String getParameter( String name , String def , boolean post  ){
        _finishParsing();
        List<String> lst = post ? _postParameters.get( name ) : _urlParameters.get( name );
        if ( lst != null && lst.size() > 0 )
            return lst.get(0);
        return def;
    }
    
    // -

    /**
     * Gets all parameters matching the name from the POST.
     * @param name the name of the parameter to look up
     * @return an array of all the values for the matching parameters
     * in the POST
     */
    public JSArray getPostParameters( String name ){
        return getParameters( name , true );
    }

    /**
     * Gets the first parameter matching the name from the POST.
     * @param name the name of the parameter to look up
     * @return the value of the first matching parameter in the POST
     */
    public String getPostParameter( String name ){
        return getParameter( name , null , true );
    }

    /**
     * Gets the first parameter matching the name from the POST.
     * @param name the name of the parameter to look up
     * @param def the default to use if no matching parameter is found
     * @return the value of the first matching parameter in the POST,
     * or def if none
     */
    public String getPostParameter( String name , String def ){
        return getParameter( name , def , true );
    }

    // -

    /**
     * Gets all parameters matching the name from the URL.
     * @param name the name of the parameter to look up
     * @return an array of all the values for the matching parameters
     * in the POST
     */
    public JSArray getURLParameters( String name ){
        return getParameters( name , false );
    }

    /**
     * Gets the first parameter matching the name from the URL, or
     * null if none is found.
     * @param name the name of the parameter to look up
     * @return the value of the first matching parameter in the URL,
     * or null if none
     */
    public String getURLParameter( String name ){
        return getParameter( name , null , false );
    }

    /**
     * Gets the first parameter matching the name from the URL.
     * @param name the name of the parameter to look up
     * @param def the default to use if no matching parameter is found
     * @return the value of the first matching parameter in the URL,
     * or def if none
     */
    public String getURLParameter( String name , String def ){
        return getParameter( name , def , false );
    }

    // -------

    /**
     * Adds a parameter to this request.
     * @param name the name of the parameter to add
     * @param val the value of the parameter to add
     */
    public void addParameter( String name , Object val ){
        _finishParsing();
        _addParm( name , val == null ? null : val.toString() , true );
    }

    /**
     * Handler for setting attributes on this request.
     * Currently, overwrite all previous parameters with the given name,
     * and to convert all values to strings.
     * @jsset
     * @param n the name of the parameter to set
     * @param v the value to set as a string or an object with a
     * toString() method
     */
    public Object set( Object n , Object v ){
        final String name = n.toString();
        _finishParsing();
        
        Object prev = getParameter( name );
        _getParamList( name , true , true ).clear();
        _addParm( name , v == null ? null : v.toString() , true );
        return prev;
    }
    
    /**
     * Handler for getting an attribute from this object.
     * Equivalent to <tt>getParameter( attribute )</tt>.
     * @jsget
     * @param n the name of the attribute to get
     * @return the value of any parameter with this name, as a string
     */
    public Object get( Object n ){
        final String name = n.toString();        

        String foo = getParameter( name , null );
        if ( foo == null )
            return null;
        return new ed.js.JSString( foo );
    }

    /**
     * Get an uploaded file field from this request, or null if none.
     * Files are uploaded in POSTs like other form fields, and have
     * names like other form fields. This fetches a form value as a 
     * file.
     * @param name the name to look up in the POST
     * @return the file corresponding to this name
     */
    public UploadFile getFile( String name ){
        if ( _postData == null )
            return null;
        
        return _postData._files.get( name );
    }

    /**
     * @unexpose
     */
    public Object setInt( int n , Object v ){
        throw new RuntimeException( "can't set things on an HttpRequest" );
    }
    /**
     * @unexpose
     */
    public Object getInt( int n ){
        throw new RuntimeException( "you're stupid" );
    }

    /**
     * Handler for iterating over all the keys in this request.
     * @return all the keys in the URL and the POST
     */
    public Set<String> keySet(){
        Set<String> s = new HashSet<String>();
        s.addAll( _urlParameters.keySet() );
        s.addAll( _postParameters.keySet() );
        return s;
    }

    /** 
     * Get the names of all the parameters in the URL.
     * @return the names of all the parameters in the URL
     */    
    public Set<String> getURLParameterNames(){
        return _urlParameters.keySet();
    }

    /**
     * Get the names of all the parameters in the POST.
     * @return the names of all the parameters in the POST
     */
    public Set<String> getPostParameterNames(){
        return _postParameters.keySet();
    }

    private final String _urlDecode( String s ){

	try {
            return URLDecoder.decode( s , "UTF-8" );
	}
	catch ( Exception e ){}
        
        try {
            return URLDecoder.decode( s , _characterEncoding );
        }
        catch ( Exception e ){}

        try {
            return URLDecoder.decode( s );
        }
        catch ( Exception e ){}

        return s;
    }
    
    /**
     * Sets parameters in the request according to the given regular
     * expression.  The regular expression is matched against the URI,
     * and the captured groups in the regular expression become
     * parameters of the request with the names supplied by the <tt>names</tt>
     * parameter.
     * @param regex a regular expression to match against a URL
     * @param names the names of the parameters to fit to the captured
     * subgroups
     * @return true if the regular expression matched
     */
    public boolean applyServletParams( JSRegex regex , JSArray names ){
        
        Matcher m = regex.getCompiled().matcher( getURI() );
        if ( ! m.find() )
            return false;

        for ( int i=1; i<=m.groupCount() && ( i - 1 ) < names.size() ; i++ )
            _addParm( names.get( i - 1 ).toString() , m.group( i ) , false );

        return true;
    }

    private void _finishParsing(){
        
        if ( ! _parsedURL ){
            _parsedURL = true;
            if ( _queryString != null ){
                int start = 0;
                while ( start < _queryString.length() ){

                    int amp = _queryString.indexOf("&",start );
                    String thing = null;
                    if ( amp < 0 ){
                        thing = _queryString.substring( start );
                        start = _queryString.length();
                    }
                    else {
                        thing = _queryString.substring( start , amp );
                        start = amp + 1;
                        while ( start < _queryString.length() && _queryString.charAt( start ) == '&' )
                            start++;
                    }
                    
                    int eq = thing.indexOf( "=" );
                    if ( eq < 0 )
                        _addParm( thing , null , false );
                    else
                        _addParm( thing.substring( 0 , eq ) , thing.substring( eq + 1 ) , false );
                }
            }
        }

        if ( ! _parsedPost && _postData != null && _command.equalsIgnoreCase( "POST" ) ){
            _parsedPost = true;
            _postData.go( this );
        }

    }

    /**
     * @unexpose
     */
    void _addParm( String n , String val , boolean post ){
        
        n = n.trim();
	n = _urlDecode( n ); // TODO: check that i really should do this

	List<String> lst = _getParamList( n , post , true );

        if ( val == null ){
            lst.add( val );
            return;
        }
        val = val.trim();
        val = _urlDecode( val );
	lst.add( val );
    }

    List<String> _getParamList( String name , boolean post , boolean create ){
        Map<String,List<String>> m = post ? _postParameters : _urlParameters;
        List<String> l = m.get( name );

        if ( l != null || ! create )
            return l;

        l = new ArrayList<String>();
        m.put( name , l );
        return l;
    }

    /**
     * @unexpose
     */

    public Object getAttachment(){
        return _attachment;
    }

    /**
     * @unexpose
     */
    public void setAttachment( Object o ){
        if ( _attachment != null )
            throw new RuntimeException( "attachment already set" );
        _attachment = o;
    }

    /**
     * @unexpose
     */
    public PostData getPostData(){
        return _postData;
    }

    /**
     * Gets the time at which this request started.
     * @return date at which request started
     */
    public JSDate getStart(){
        return _start;
    }

    /**
     * @unexpose
     */
    public long[] getRange(){
        if ( _rangeChecked )
            return _range;
        
        String s = getHeader( "Range" );
        if ( s != null )
            _range = _parseRange( s );

        _rangeChecked = true;
        return _range;
    }


    /**
     * Gets the ip of the client as a string.
     * @return the ip of the client
     */
    public String getRemoteIP(){
        if ( _remoteIP != null )
            return _remoteIP;
        
        String ip = getHeader( "X-Cluster-Client-Ip" );
        if ( ip == null )
            ip = _handler.getInetAddress().getHostAddress();
        
        _remoteIP = ip;
        return _remoteIP;
    }

    /**
     * @unexpose
     */    
    public static long[] _parseRange( String s ){
        if ( s == null )
            return null;
        s = s.trim();
        if ( ! s.startsWith( "bytes=" ) )
            return null;
        
        s = s.substring( 6 ).trim();
        if ( s.length() == 0 )
            return null;
        
        
        if ( s.matches( "\\d+" ) )
            return new long[]{ Long.parseLong( s ) , Long.MAX_VALUE };
        
        String pcs[] = s.split( "," );
        if ( pcs.length == 0 )
            return null;

        if ( pcs.length == 1 ){ 
            // either has to be 
            // -100
            s = pcs[0].trim();

            if ( s.length() == 0 )
                return null;

            if ( s.charAt( 0 ) == '-' ) // we don't support this
                return null;
            
            Matcher m = Pattern.compile( "(\\d+)\\-(\\d+)" ).matcher( s );
            if ( m.find() )
                return new long[]{ Long.parseLong( m.group(1) ) , Long.parseLong( m.group(2) ) };
            return null;
        }

        long min = Long.MAX_VALUE;
        long max = -1;

        for ( int i=0; i<pcs.length; i++ ){
            String foo = pcs[i];
            
            Matcher m = Pattern.compile( "(\\d+)\\-(\\d+)" ).matcher( s );

            if ( ! m.find() )
                return null;
            
            long l = Long.parseLong( m.group(1) );
            long h = Long.parseLong( m.group(2) );
            
            min = Math.min( min , l );
            max = Math.max( min , h );
        }

        if ( max < 0 )
            return null;

        return new long[]{ min , max };
    }
    
    final HttpServer.HttpSocketHandler _handler;
    final String _firstLine;
    final Map<String,String> _headers = new StringMap<String>();
    final JSDate _start = new JSDate();
    JSObjectBase _cookies;
    String _remoteIP;

    boolean _parsedPost = false;
    PostData _postData;
    
    boolean _parsedURL = false;
    final Map<String,List<String>> _urlParameters = new StringMap<List<String>>();
    final Map<String,List<String>> _postParameters = new StringMap<List<String>>();

    final String _rawHeader;
    final String _command;
    final String _url;
    final String _uri;
    final String _queryString;
    final boolean _http11;

    Object _attachment;
    
    private boolean _rangeChecked = false;
    private long[] _range;

    private String _characterEncoding = "ISO-8859-1";

}
