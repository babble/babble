// AppRequest.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;

public class AppRequest {
    
    AppRequest( AppContext context , HttpRequest request , String uri ){
        _context = context;
        _request = request;
        _scope = _context.scopeChild();
        
        if ( uri == null )
            uri = _request.getURI();

        _uri = uri.equals( "/" ) ? "/index" : uri;
    }

    public AppContext getContext(){
        return _context;
    }

    public Scope getScope(){
        return _scope;
    }

    public String getURI(){
        return _uri;
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
        String uri = getWantedURI();
        
        if ( uri.endsWith( ".jxp" ) )
            return false;
        
        int period = uri.indexOf( "." );
        if ( period < 0 )
            return false;

        String ext = uri.substring( period + 1 );
        if ( MimeTypes.get( ext.toLowerCase() ) == null )
            return false;

        return true;
    }

    String getOverrideURI(){
        String foo = getOverrideURI( "mapUrlToJxpFileCore" );
        if ( foo != null )
            return foo;

        foo = getOverrideURI( "mapUrlToJxpFile" );
        if ( foo != null )
            return foo;
        
        return null;
    }
    
    String getOverrideURI( String funcName ){
        Object o = _scope.get( funcName );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            return null;
        
        Object res = ((JSFunction)o).call( _scope , new JSString( getURI() ) , _request );
        if ( res == null )
            return null;
        return res.toString();
    }
    
    String getWantedURI(){
        String override = getOverrideURI();
        if ( override != null )
            return override;
        return getURI();
    }
    

    File getFile(){
        return _context.getFile( getWantedURI() );
    }

    final String _uri;
    final HttpRequest _request;
    final AppContext _context;
    final Scope _scope;
}
