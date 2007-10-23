// HttpRequest.java

package ed.net.httpserver;

import java.net.*;
import java.util.*;

import ed.util.*;

public class HttpRequest implements ed.js.JSObject {
    
    HttpRequest( HttpServer.HttpSocketHandler handler , String header ){
        _handler = handler;

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

    public boolean getBoolean( String n , boolean def ){
        return StringParseUtil.parseBoolean( getParameter( n ) , def );
    }

    public int getInt( String n , int def ){
        return StringParseUtil.parseInt( getParameter( n ) , def );
    }

    public String getHeader( String h ){
        return _headers.get( h );
    }

    public int getIntHeader( String h , int def ){
        return StringParseUtil.parseInt( getHeader( h ) , def );
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
        return getParameter( n.toString() , null );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "can't set things on an HttpRequest" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "you're stupid" );
    }

    public Set<String> keySet(){
        throw new RuntimeException( "not implemented yet" );
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
             
            if ( getHeader("Content-Type") != null &&
                 getHeader("Content-Type").toLowerCase().trim().startsWith("multipart/form-data") ){
                _handleMultipartPost();
            }
            else {
                _handleRegularPost();
            }
            
        }
    }

    private void _handleMultipartPost(){
        throw new RuntimeException( "can't do multipart yet" );
    }
    
    private void _handleRegularPost(){
        for ( int i=0; i<_postData.length; i++ ){
            int start = i;
            for ( ; i<_postData.length; i++ )
                if ( _postData[i] == '=' ||
                     _postData[i] == '\n' ||
                     _postData[i] == '&' )
                    break;

            if ( i == _postData.length ){
                _addParm( new String( _postData , start , _postData.length - start ) , null );
                break;
            }
            
            if ( _postData[i] == '\n' ||
                 _postData[i] == '&' ){
                _addParm( new String( _postData , start , i - start ) , null );
                continue;
            }

            int eq = i;
            
            for ( ; i<_postData.length; i++ )
                if ( _postData[i] == '\n' ||
                     _postData[i] == '&' )
                    break;
            
            _addParm( new String( _postData , start , eq - start ) ,
                      new String( _postData , eq + 1 , i - ( eq + 1 ) ) );
                
        }
    }

    private void _addParm( String n , String val ){
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
    
    final HttpServer.HttpSocketHandler _handler;
    final String _firstLine;
    final Map<String,String> _headers = new StringMap<String>();

    boolean _parsedPost = false;
    byte _postData[];

    boolean _parsedURL = false;
    final Map<String,String> _parameters = new StringMap<String>();

    final String _command;
    final String _url;
    final String _uri;
    final String _queryString;
    final boolean _http11;

    private Object _attachment;

    private String _characterEncoding = "ISO-8859-1";
}

