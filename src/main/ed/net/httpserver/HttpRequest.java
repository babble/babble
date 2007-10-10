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
                _headers.put( line.substring( 0 , foo ).trim() , 
                              line.substring( foo + 1 ).trim() );
        }
        
    }
    
    
    public String toString(){
        return _firstLine + " : " + _headers;
    }
    
    final HttpServer.HttpSocketHandler _handler;
    final String _firstLine;
    final Map<String,String> _headers;
}

