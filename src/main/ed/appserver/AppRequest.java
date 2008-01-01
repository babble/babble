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
        _scope.put( "request" , request , true );

        if ( uri == null )
            uri = _request.getURI();

        _uri = uri.equals( "/" ) ? "/index" : uri;
    }

    void setResponse( HttpResponse response ){
	_scope.put( "response" , response , true );
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
        if ( _wantedURI != null )
            return _wantedURI;
        
        String override = getOverrideURI();
        if ( override != null )
            _wantedURI = override;
        else
            _wantedURI = getURI();
        
        return _wantedURI;
    }
    

    File getFile(){
        return _context.getFile( getWantedURI() );
    }

    final String _uri;
    final HttpRequest _request;
    final AppContext _context;
    final Scope _scope;

    String _wantedURI = null;
}
