// AppRequest.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver;

import java.io.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.log.*;
import ed.appserver.jxp.*;
import ed.lang.*;

public class AppRequest {
    
    AppRequest( AppContext context , HttpRequest request , String uri ){
        _context = context;
        _request = request;

        if ( uri == null )
            uri = _request.getURI();
        
        _uri = uri.equals( "/" ) ? "/index" : uri;

        _fixer = new URLFixer( _request , this );
    }

    void setResponse( HttpResponse response ){
	getScope().put( "response" , response , true );
    }

    public AppContext getContext(){
        return _context;
    }

    public Scope getScope(){
        
        if ( _scope == null ){
            _scope = _context.scopeChild( isAdmin() );

            _scope.put( "request" , _request , true );
            _scope.lock( "request" );

            _scope.put( "head" , _head , true );
            _scope.lock( "head" );
            
            _scope.put( "session" , Session.get( _request.getCookie( Session.COOKIE_NAME ) , _context.getDB() ) , true );
            _scope.lock( "session" );

            _context.setTLPreferredScope( this , _scope );
        }
        
        return _scope;
    }
    
    boolean isScopeInited(){
        return _scope != null;
    }

    public boolean isAdmin(){

        if ( true ) return false;
        
        if ( _uri.startsWith( "/admin/" ) )
            return true;

        if ( _uri.startsWith( "/~~/modules/admin/" ) )
            return true;
        
        return false;
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
        
        _wantedURI = _wantedURI.replaceAll( "//+" , "/" );

        return _wantedURI;
    }
    

    File getFile()
        throws FileNotFoundException {
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

    public void print( String s ){
        Object o = _scope.get( "print" );
        if ( ! ( o instanceof JxpServlet.MyWriter ) )
            System.out.print( s );
        else
            ((JxpServlet.MyWriter)o).print( s );
    }

    public void turnOnProfiling(){
        if ( _profiler != null )
            return;
        
        _profiler = new ProfilingTracker("AppRequest : " + _context.getName() + ":" + _context.getEnvironmentName() + ":" + _request.getURL() );
        _profiler.makeThreadLocal();
    }

    void done( HttpResponse response ){
        _context.setTLPreferredScope( this , null );
        Session session = (Session)_scope.get( "session" );
        if ( session.sync( _context.getDB() ) )
            response.addCookie( Session.COOKIE_NAME , session.getCookie() );
    }
    
    void makeThreadLocal(){
        _tl.set( this );
    }

    void unmakeThreadLocal(){
        _tl.set( null );
    }

    public static AppRequest getThreadLocal(){
        return _tl.get();
    }

    public URLFixer getURLFixer(){
        return _fixer;
    }

    final String _uri;
    final HttpRequest _request;
    final AppContext _context;

    final URLFixer _fixer;
    final JSArray _head = new HeadArray();

    private Scope _scope;
    
    String _wantedURI = null;
    ProfilingTracker _profiler;

    static ThreadLocal<AppRequest> _tl = new ThreadLocal<AppRequest>();
}
