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
            _uri = _url;
            _queryString = null;
        }
        else {
            _uri = _url.substring( 0 , endURI );
            _queryString = _url.substring( endURI + 1 );
        }
    }

    public String getURI(){
        return _uri;
    }

    public String getURL(){
        return _url;
    }

    public String getRawHeader(){
        return _rawHeader;
    }

    public String getMethod(){
        return _command;
    }
    
    public String getQueryString(){
        return _queryString;
    }

    public int totalSize(){
        int size = _rawHeader.length();
        size += getIntHeader( "Content-Length" , 0 );
        return size;
    }

    public String toString(){
        _finishParsing();
        return _command + " " + _uri + " HTTP/1." + ( _http11 ? "1" : "" ) + " : " + _headers + "  " + _parameters;
    }
    
    public boolean keepAlive(){
        String c = getHeader( "connection" );
        if ( c != null )
            return ! c.equalsIgnoreCase( "close" );
        return _http11;
    }

    // header stuff

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

    public int getPort(){
        String host = getHeader( "Host" );
        if ( host == null )
            return 0;

        int idx = host.indexOf( ":" );
        if ( idx < 0 )
            return 0;
        
        return StringParseUtil.parseInt( host.substring( idx + 1 ) , 0 );
    }

    public String getHeader( String h ){
        return _headers.get( h );
    }

    public int getIntHeader( String h , int def ){
        return StringParseUtil.parseInt( getHeader( h ) , def );
    }

    public JSArray getHeaderNames(){
        JSArray a = new JSArray();
        a.addAll( _headers.keySet() );
        return a;
    }

    // cookies

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

    public JSArray getCookieNames(){
        getCookie( "" );
        JSArray a = new JSArray();
        a.addAll( _cookies.keySet() );
        return a;
    }

    public JSObject getCookies(){
        getCookie( "" );
        return _cookies;
    }
    
    // param stuff

    public JSArray getParameterNames(){
        _finishParsing();
        
        JSArray a = new JSArray();
        for ( String s : _parameters.keySet() )
            a.add( new JSString( s ) );
        return a;
    }

    public boolean getBoolean( String n , boolean def ){
        return StringParseUtil.parseBoolean( getParameter( n ) , def );
    }

    public int getInt( String n , int def ){
        return StringParseUtil.parseInt( getParameter( n ) , def );
    }

    public JSArray getParameters( String name ){
        List<String> lst = _parameters.get( name );
        if ( lst == null )
            return null;
        
        JSArray a = new JSArray();
        for ( String s : lst )
            a.add( new JSString( s ) );
	return a;
    }

    public String getParameter( String name ){
        return getParameter( name , null );
    }

    public String getParameter( String name , String def ){
        _finishParsing();
        List<String> s = _parameters.get( name );
        if ( s != null && s.size() > 0 )
            return s.get(0);
        return def;
    }

    public Object set( Object n , Object v ){
        String name = n.toString();
        _finishParsing();
        
        Object prev = getParameter( name );
        _addParm( name , v == null ? null : v.toString() );
        return prev;
    }

    public Object get( Object n ){
        String foo = getParameter( n.toString() , null );
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
        return _parameters.keySet();
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
            _addParm( names.get( i - 1 ).toString() , m.group( i ) );

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
                        _addParm( thing , null );
                    else
                        _addParm( thing.substring( 0 , eq ) , thing.substring( eq + 1 ) );
                }
            }
        }

        if ( ! _parsedPost && _postData != null && _command.equalsIgnoreCase( "POST" ) ){
            _parsedPost = true;
            _postData.go( this );
        }

    }

    void _addParm( String n , String val ){
        
        n = n.trim();
	n = _urlDecode( n ); // TODO: check that i really should do this

	List<String> lst = _parameters.get( n );
	if ( lst == null ){
	    lst = new ArrayList<String>();
	    _parameters.put( n , lst );
	}

        if ( val == null ){
            lst.add( val );
            return;
        }
        val = val.trim();
        val = _urlDecode( val );
	lst.add( val );
    }

    public Object getAttachment(){
        return _attachment;
    }

    public void setAttachment( Object o ){
        if ( _attachment != null )
            throw new RuntimeException( "attachment already set" );
        _attachment = o;
    }

    public PostData getPostData(){
        return _postData;
    }

    public JSDate getStart(){
        return _start;
    }

    public long[] getRange(){
        if ( _rangeChecked )
            return _range;
        
        String s = getHeader( "Range" );
        if ( s != null )
            _range = _parseRange( s );

        _rangeChecked = true;
        return _range;
    }

    public String getRemoteIP(){
        if ( _remoteIP != null )
            return _remoteIP;
        
        String ip = getHeader( "X-Cluster-Client-Ip" );
        if ( ip == null )
            ip = _handler.getInetAddress().getHostAddress();
        
        _remoteIP = ip;
        return _remoteIP;
    }

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
    final Map<String,List<String>> _parameters = new StringMap<List<String>>();

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

