// AppRequest.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.net.httpserver.*;

public class AppRequest {
    
    AppRequest( HttpRequest request ){
        _request = request;
    }

    String getRoot(){
        return "crap/www";
    }
    

    String getCustomer(){
        return "";
    }

    boolean fork(){
        return true;
    }

    boolean isStatic(){
        String uri = _request.getURI();
        
        if ( uri.endsWith( ".jxp" ) )
            return false;
        
        return true;
    }

    File getFile(){
        return new File( getRoot() + _request.getURI() );
    }


    final HttpRequest _request;
}
