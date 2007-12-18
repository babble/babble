// HttpRequest.java

package ed.net.httpserver;

import java.net.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;

public class HttpRequest implements ed.js.JSObject {
    
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
            Map<String,String> m = new StringMap<String>();
            String temp = getHeader( "Cookie" );
            if ( temp != null ){

                for ( String thing : temp.split( ";" ) ){
                    
                    int idx = thing.indexOf("=");
                    
                    if ( idx < 0 )
                        continue;

                    m.put( thing.substring( 0 , idx ).trim() , 
                           thing.substring( idx + 1 ).trim() );
                }
            }
            _cookies = m;
        }
        return _cookies.get( s );
    }

    public JSArray getCookieNames(){
        getCookie( "" );
        JSArray a = new JSArray();
        a.addAll( _cookies.keySet() );
        return a;
    }
    
    // param stuff

    public JSArray getParameterNames(){
        _finishParsing();
        
        JSArray a = new JSArray();
        a.addAll( _parameters.keySet() );
        return a;
    }

    public boolean getBoolean( String n , boolean def ){
        return StringParseUtil.parseBoolean( getParameter( n ) , def );
    }

    public int getInt( String n , int def ){
        return StringParseUtil.parseInt( getParameter( n ) , def );
    }


    public String getParameter( String name ){
        return getParameter( name , null );
    }

    public String getParameter( String name , String def ){
        _finishParsing();
        String s = _parameters.get( name );
        if ( s != null )
            return s;
        return def;
    }

    public Object set( Object n , Object v ){
        throw new RuntimeException( "can't set things on an HttpRequest" );
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
	    return URLDecoder.decode( s , _characterEncoding );
	}
	catch ( Exception e ){}
        
        try {
            return URLDecoder.decode( s , "UTF-8" );
        }
        catch ( Exception ee ){}

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
        
        if ( val == null ){
            _parameters.put( n , val );
            return;
        }
        val = val.trim();
        val = _urlDecode( val );
        _parameters.put( n , val );
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
    
    final HttpServer.HttpSocketHandler _handler;
    final String _firstLine;
    final Map<String,String> _headers = new StringMap<String>();
    Map<String,String> _cookies;

    boolean _parsedPost = false;
    PostData _postData;

    boolean _parsedURL = false;
    final Map<String,String> _parameters = new StringMap<String>();

    final String _rawHeader;
    final String _command;
    final String _url;
    final String _uri;
    final String _queryString;
    final boolean _http11;

    private Object _attachment;

    private String _characterEncoding = "ISO-8859-1";
}

