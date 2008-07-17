// HttpRequest.java

package ed.net.httpserver;

import java.net.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;

public class HttpRequest extends JSObjectLame {
    
    public static HttpRequest getDummy( String url ){
        return getDummy( url , "" );
    }

    public static HttpRequest getDummy( String url , String extraHeaders ){
        return new HttpRequest( null , "GET " + url + " HTTP/1.0\n" + extraHeaders + "\n" );
    }

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

    public String getURI(){
        return _uri;
    }

    public String getURL(){
        return _url;
    }
    
    /**
     * Gets the entire http header
     * @return the entire http header
     */
    public String getRawHeader(){
        return _rawHeader;
    }

    /**
     * This will be either GET, POST, HEAD, etc...
     * @return http method used
     */
    public String getMethod(){
        return _command;
    }
    
    /**
     * for "/foo?a=1"
     * would return "a=1"
     * @return the query part of the request.  null if there was none
     */
    public String getQueryString(){
        return _queryString;
    }
    
    /**
     * @return the total size of the http message, header + body
     */
    public int totalSize(){
        int size = _rawHeader.length();
        size += getIntHeader( "Content-Length" , 0 );
        return size;
    }

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
     * this returns the host header minus ant port information if there is any
     * @return the host to which the request was sent
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
     * @return the port the request was for
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
     * gets the raw http header
     * @param h case insensitive
     * @return the header 
     */
    public String getHeader( String h ){
        return _headers.get( h );
    }

    /**
     * gets the http header parsed into an int
     * if the header wasn't sent or can't be parsed as an int, def is returned
     * @return the header parsed as an int
     */
    public int getIntHeader( String h , int def ){
        return StringParseUtil.parseInt( getHeader( h ) , def );
    }

    /**
     * @return an array of http header names
     */
    public JSArray getHeaderNames(){
        JSArray a = new JSArray();
        a.addAll( _headers.keySet() );
        return a;
    }

    // cookies


    /**
     * cookie access
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
     * Gets all cookie names
     * @return array of cookie names
     */
    public JSArray getCookieNames(){
        getCookie( "" );
        JSArray a = new JSArray();
        a.addAll( _cookies.keySet() );
        return a;
    }

    /**
     * expose cookies as a javascript object
     * this is so you can request.getCookies().username
     * NOTE: you can't add cookies like this
     * @return all cookies as a javascript obejct
     */
    public JSObject getCookies(){
        getCookie( "" );
        return _cookies;
    }
    
    // param stuff

    /** 
     * gets all the paramter names specified in the request.  
     * this include GET and POST paramters
     * @return array of paramter names
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
     * parses the parameter n as a boolean
     * if it doesn't exist, or can't be parsed as a boolean, def is returned
     * @return true/false if paramter is specified or def
     */
    public boolean getBoolean( String n , boolean def ){
        return StringParseUtil.parseBoolean( getParameter( n ) , def );
    }

    /**
     * parses the parameter n as an int
     * if it doesn't exist, or can't be parsed as an int, def is returned
     * @return number if paramter is specified or def
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
     * this is used when you have multiple values for the same paramter
     * i.e. "/foo?a=1&a=2"
     * in this case it would return [ "1" , "2" ]
     * @return array of values specified by name
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
     * parameter access
     * @return paramter specified by name, null if none
     */
    public String getParameter( String name ){
        return getParameter( name , null );
    }

    /**
     * parameter access
     * @return paramter specified by name, def if none
     */
    public String getParameter( String name , String def ){
        _finishParsing();
        List<String> s = _getParameter( name );
        if ( s != null && s.size() > 0 )
            return s.get(0);
        return def;
    }

    // -------

    public JSArray getParameters( String name , boolean post ){
        List<String> lst = post ? _postParameters.get( name ) : _urlParameters.get( name );
        if ( lst == null )
            return null;
        
        JSArray a = new JSArray();
        for ( String s : lst )
            a.add( new JSString( s ) );
	return a;
    }

    public String getParameter( String name , boolean post ){
        return getParameter( name , null , post  );
    }

    public String getParameter( String name , String def , boolean post  ){
        _finishParsing();
        List<String> lst = post ? _postParameters.get( name ) : _urlParameters.get( name );
        if ( lst != null && lst.size() > 0 )
            return lst.get(0);
        return def;
    }
    
    // -

    public JSArray getPostParmeters( String name ){
        return getParameters( name , true );
    }

    public String getPostParmeter( String name ){
        return getParameter( name , null , true );
    }

    public String getPostParmeter( String name , String def ){
        return getParameter( name , def , true );
    }

    // -

    public JSArray getURLParmeters( String name ){
        return getParameters( name , false );
    }

    public String getURLParmeter( String name ){
        return getParameter( name , null , false );
    }

    public String getURLParmeter( String name , String def ){
        return getParameter( name , def , false );
    }

    // -------

    public void addParameter( String name , Object val ){
        _finishParsing();
        _addParm( name , val == null ? null : val.toString() , true );
    }

    public Object set( Object n , Object v ){
        final String name = n.toString();
        _finishParsing();
        
        Object prev = getParameter( name );
        _getParamList( name , true , true ).clear();
        _addParm( name , v == null ? null : v.toString() , true );
        return prev;
    }
    
    public Object get( Object n ){
        final String name = n.toString();        

        String foo = getParameter( name , null );
        if ( foo == null )
            return null;
        return new ed.js.JSString( foo );
    }

    public UploadFile getFile( String name ){
        if ( _postData == null )
            return null;
        
        return _postData._files.get( name );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "can't set things on an HttpRequest" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "you're stupid" );
    }

    public Set<String> keySet(){
        Set<String> s = new HashSet<String>();
        s.addAll( _urlParameters.keySet() );
        s.addAll( _postParameters.keySet() );
        return s;
    }
    
    public Set<String> getURLParameterNames(){
        return _urlParameters.keySet();
    }

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
     * gets the ip of the client
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

