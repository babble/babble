// AppRequest.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;

public class AppRequest {
    
    AppRequest( AppContext context , HttpRequest request ){
        _context = context;
        _request = request;
        _scope = _context.scopeChild();
    }

    public AppContext getContext(){
        return _context;
    }

    public Scope getScope(){
        return _scope;
    }

    String getRoot(){
        return _context.getRoot();
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
        return _context.getFile( _request.getURI() );
    }


    final HttpRequest _request;
    final AppContext _context;
    final Scope _scope;
}
