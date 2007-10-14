// HttpRequest.java

package ed.net.httpserver;

import java.util.*;

public class HttpRequest {
    
    HttpRequest( HttpServer.HttpSocketHandler handler , String header ){
        _handler = handler;

        int idx = header.indexOf( "\n" );
        if ( idx < 0 )
            throw new RuntimeException( "something is very wrong" );
        
        _firstLine = header.substring( 0 , idx ).trim();
        
        _headers = new TreeMap<String,String>();
        
        int start = idx + 1;
        while ( ( idx = header.indexOf( "\n" , start ) ) >= 0 ) {
            final String line = header.substring( start , idx ).trim();
            start = idx + 1;
            int foo = line.indexOf( ":" );
            if ( foo > 0 )
                _headers.put( line.substring( 0 , foo ).trim().toLowerCase() , 
                              line.substring( foo + 1 ).trim() );
        }
        
        // parse first line
        idx = _firstLine.indexOf( " " );
        if ( idx < 0 )
            throw new RuntimeException( "malformed" );
        
        _command = _firstLine.substring( 0 , idx );
        int endURI = _firstLine.indexOf( " " , idx + 1 );
        if ( endURI < 0 ){
            _uri = _firstLine.substring( idx + 1 ).trim();
            _http11 = false;
        }
        else {
            _uri = _firstLine.substring( idx + 1 , endURI ).trim();
            _http11 = _firstLine.indexOf( "1.1" , endURI ) > 0;
        }
    }

    public String getURI(){
        return _uri;
    }
    
    public String toString(){
        return _command + " " + _uri + " HTTP/1." + ( _http11 ? "1" : "" ) + " : " + _headers;
    }
    
    public boolean keepAlive(){
        String c = getHeader( "connection" );
        if ( c != null )
            return ! c.equalsIgnoreCase( "close" );
        return _http11;
    }

    public String getHeader( String h ){
        return _headers.get( h.toLowerCase() );
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
    final Map<String,String> _headers;

    final String _command;
    final String _uri;
    final boolean _http11;

    private Object _attachment;
}

