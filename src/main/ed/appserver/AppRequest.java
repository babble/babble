// AppRequest.java

package ed.appserver;

import java.io.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.log.*;

public class AppRequest {
    
    AppRequest( AppContext context , HttpRequest request , String uri ){
        _context = context;
        _request = request;

        if ( uri == null )
            uri = _request.getURI();
        
        _uri = uri.equals( "/" ) ? "/index" : uri;
    }

    void setResponse( HttpResponse response ){
	getScope().put( "response" , response , true );
    }

    public AppContext getContext(){
        return _context;
    }

    public Scope getScope(){
        
        if ( _scope == null ){
            _scope = _context.scopeChild();

            _scope.put( "request" , _request , true );
            _scope.lock( "request" );

            _scope.put( "head" , _head , true );
            _scope.lock( "head" );
            
            _scope.put( "session" , Session.get( _request.getCookie( Session.COOKIE_NAME ) , _context.getDB() ) , true );
            _scope.lock( "session" );
            
            _context.getScope().setTLPreferred( _scope );
        }
        
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

    private String getOverrideURI(){
        String foo = getOverrideURI( "mapUrlToJxpFileCore" );
        if ( foo != null )
            return foo;

        foo = getOverrideURI( "mapUrlToJxpFile" );
        if ( foo != null )
            return foo;
        
        return null;
    }
    
    private String getOverrideURI( String funcName ){
        Object o = getScope().get( funcName );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            return null;
        
        Object res = ((JSFunction)o).call( getScope() , new JSString( getURI() ) , _request , getScope().get( "response" ) );
        if ( res == null )
            return null;
        return res.toString();
    }
    
    private String getWantedURI(){
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

    public JSArray getHead(){
        return _head;
    }

    public JSArray getHeadToPrint(){
        _head.lock();
        return _head;
    }

    public Logger getLogger(){
        return _context._logger;
    }

    void done( HttpResponse response ){
        _context.getScope().setTLPreferred( null );
        Session session = (Session)_scope.get( "session" );
        if ( session.sync( _context.getDB() ) )
            response.addCookie( Session.COOKIE_NAME , session.getCookie() );
    }
    
    final String _uri;
    final HttpRequest _request;
    final AppContext _context;
    final JSArray _head = new HeadArray();

    private Scope _scope;
    
    String _wantedURI = null;
}
