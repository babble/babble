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
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.log.*;
import ed.appserver.jxp.*;
import ed.lang.*;
import ed.util.*;

public class AppRequest implements Sizable {
    
    AppRequest( AppContext context , HttpRequest request ) {
        this( context , request , request.getHost() , request.getURI() );
        
    }

    AppRequest( AppContext context , HttpRequest request , String host , String uri ){
        _context = context;
        _request = request;

        if ( host == null )
            host = _request.getHost();
        if ( uri == null )
            uri = _request.getURI();
        
        _host = host;
        _uri = uri == null ? "/" : uri;

        _fixer = new URLFixer( _request , this );
	if ( _context._admin )
	    _fixer.setCDNPrefix( "" );
    }

    void setResponse( HttpResponse response ){
        _response = response;
	getScope().put( "response" , response , true );
    }

    public AppContext getContext(){
        return _context;
    }

    public RequestScope getScope(){
        
        if ( _scope == null ){
            _scope = new RequestScope( _context , this );

            _scope.put( "request" , _request , true );
            _scope.lock( "request" );

            _scope.put( "head" , _head , true );
            _scope.lock( "head" );
            
	    _session = Session.get( _request.getCookie( Session.COOKIE_NAME ) , _context.getDB() );
            _scope.put( "session" , _session , true );
            _scope.lock( "session" );
	    _request.set( "session" , _session );

            _scope.put( "print" , getServletWriter() , true );

            _context.setTLPreferredScope( this , _scope );
        }
        
        return _scope;
    }

    boolean isScopeInited(){
        return _scope != null;
    }
    
    public boolean hasDevFeatureEnabled( String name ){
        getScope();
        return JSInternalFunctions.JS_evalToBool( _session.get( "dev_" + name ) );
    }

    public boolean canBeLong(){
        return 
            _request.getBooleanHeader( "X-Long" , false ) || 
            _request.getBoolean( "allowLong" , false );
    }

    public String debugInfo(){
	return "uri:" + getURI() + " context:" + _context.debugInfo();
    }

    public static boolean isAdmin( final HttpRequest request ){
        
        final String uri = request.getURI();
        if ( isAdminURI( uri ) )
            return true;
        
        // is this an admin refer request
        // requirements
        // 1) static file, image/css/js
        // 2) has to be in corejs or external
        // 3) referer has to be from admin
        if ( ( isStatic( uri ) || uri.startsWith( "/~~/user/" ) ) && 
             ( uri.startsWith( "/~~/" ) || uri.startsWith( "/@@/" ) ) && 
             ! uri.startsWith( "/~~/modules/" ) 
             ){
            String referer = request.getRefererNoHost();
            return referer != null && isAdminURI( referer );
        }
        
        return false;
    }

    private static boolean isAdminURI( String uri ){
        if ( uri.startsWith( "/admin/" ) )
            return true;
        
        if ( uri.startsWith( "/~~/modules/admin/" ) )
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
        if ( _wantedFunction != null )
            return false;
        return isStatic( uri );
    }
    
    static boolean isStatic( final String uri ){
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
        Object o = _context.getFromInitScope( funcName );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            return null;
        
        Object res = ((JSFunction)o).call( getScope() , new JSString( getURI() ) , _request , _response );
        if ( res == null )
            return null;
        
        if ( res instanceof JSFunction ){
            _wantedFunction = (JSFunction)res;
            return FUNCTION_CALL_STRING;
        }

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
        String uri = getWantedURI();
        if ( _wantedFunction != null && uri == FUNCTION_CALL_STRING )
            return FUNCTION_CALL_FUNC;
        return _context.getFile( uri );
    }

    public JSArray getHead(){
        return _head;
    }

    public JSArray getHeadToPrint(){
        _head.lock();
        return _head;
    }

    public Logger getLogger(){
        return _context.getLogger();
    }

    public void print( String s ){
        Object o = _scope.get( "print" );
        if ( ! ( o instanceof ServletWriter ) )
            System.out.print( s );
        else
            ((ServletWriter)o).print( s );
    }

    void turnOnDevFeatures(){
        if ( hasDevFeatureEnabled( "profiling" ) )
            turnOnProfiling();
        
        if ( hasDevFeatureEnabled( "logs" ) )
            turnOnAppender();
    }

    public void turnOnAppender(){
        if ( _appenderWriter != null )
            return;

        _appenderStream = new ByteArrayOutputStream( 1024 );
        _appenderWriter = new PrintStream( _appenderStream );
        Logger.setThreadLocalAppender( new PrintStreamAppender( _appenderWriter ) );
    }
        

    public void turnOnProfiling(){
        if ( _profiler != null )
            return;
        
        _profiler = new ProfilingTracker("AppRequest : " + _context.getName() + ":" + _context.getEnvironmentName() + ":" + _request.getURL() );
        _profiler.makeThreadLocal();
    }

    JxpServlet getServlet( File f )
        throws IOException {
        
        if ( _wantedFunction == null )
            return _context.getServlet( f );

        return new JxpServlet( _context , _wantedFunction );
    }

    void done( HttpResponse response ){
        _done = true;
        _context.setTLPreferredScope( this , null );
        _scope.done();
        if ( _session.sync( _context.getDB() ) )
            response.addCookie( Session.COOKIE_NAME , _session.getCookie() );
    }
    
    void makeThreadLocal(){
        _tl.set( this );
    }

    void unmakeThreadLocal(){
        _tl.set( null );
    }

    public String getHost(){
        return _host;
    }

    public String getDirectory(){
        return HttpRequest.getDirectory( _uri );
    }

    public static AppRequest getThreadLocal(){
        return _tl.get();
    }

    public URLFixer getURLFixer(){
        return _fixer;
    }

    public boolean isDone(){
        return _done;
    }

    public String toString(){
        return "AppRequest: " + _host + _uri;
    }

    public HttpRequest getRequest(){
        return _request;
    }

    public HttpResponse getResponse(){
        return _response;
    }

    public ServletWriter getServletWriter(){
        if ( _writer == null )
            _writer = new ServletWriter( _response.getJxpWriter() , _fixer );
        return _writer;
    }

    public Session getSession(){
        return _session;
    }
    
    public Object getFromInitScope( String what ){
        return _context.getFromInitScope( what );
    }

    public static void somethingCompiled(){
        AppRequest ar = _tl.get();
        if ( ar != null )
            ar._somethingCompiled = true;
    }

    public long approxSize( SeenPath seen ){
        
        // we don't want to count stuff reaching from context here
        seen.pushSpecialDontTraverse( _context._rootContextReachable.keySet() );
        
        try {
            long s = 0;
            s += JSObjectSize.size( _request , seen , this );
            s += JSObjectSize.size( _scope , seen , this );
            s += JSObjectSize.size( _response , seen , this );
            return s;
        }
        finally {
            seen.popSpecialDontTraverse();
        }
    }

    public void setAttribute( String name , Object attr ){
        _attributes.put( name , attr );
    }

    public Object getAttribute( String name ){
        return _attributes.get( name );
    }

    public TimeZone getTimeZone(){
        if ( _tz != null )
            return _tz;
        return _context.getTimeZone();
    }

    public void setTimeZone( String tz ){
        _tz = TimeZone.getTimeZone( tz );
    }

    final String _uri;
    final String _host;
    final HttpRequest _request;
    final AppContext _context;

    final long _created = System.currentTimeMillis();
    boolean _somethingCompiled = false;

    private HttpResponse _response;
    private TimeZone _tz;

    final URLFixer _fixer;
    final JSArray _head = new HeadArray();
    
    private RequestScope _scope;
    private Session _session;
    private boolean _done = false;
    
    private ServletWriter _writer;
    
    String _wantedURI = null;
    JSFunction _wantedFunction = null;

    ProfilingTracker _profiler;
    OutputStream _appenderStream;
    PrintStream _appenderWriter;
    
    private Map<String,Object> _attributes = new HashMap<String,Object>();


    static ThreadLocal<AppRequest> _tl = new ThreadLocal<AppRequest>();
    static private final String FUNCTION_CALL_STRING = ( "getOverrideURI-FunctionCall-special" + Math.random() ).intern();
    static private final File FUNCTION_CALL_FUNC = new File( FUNCTION_CALL_STRING );
}
