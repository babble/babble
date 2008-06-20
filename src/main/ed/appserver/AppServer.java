// AppServer.java

package ed.appserver;

import ed.db.JSHook;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.*;
import ed.util.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppServer implements HttpHandler {

    private static final int DEFAULT_PORT = 8080;

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );

    public AppServer( String defaultWebRoot , String root ){
        _contextHolder = new AppContextHolder( defaultWebRoot , root );
    }


    AppRequest createRequest( HttpRequest request ){
        String newUri[] = new String[1];
        AppContext ac = _contextHolder.getContext( request , newUri );
        return ac.createRequest( request , newUri[0] );
    }
    
    public boolean handles( HttpRequest request , Info info ){
        String uri = request.getURI();
        
        if ( ! uri.startsWith( "/" ) )
            return false;
        
        info.fork = true;
        info.admin = uri.startsWith( "/~~/core/sys/" );

        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){
        try {
            _handle( request , response );
        }
        catch ( Exception e ){
            handleError( request , response , e , null );
        }
    }

    private void _handle( HttpRequest request , HttpResponse response ){

        final long start = System.currentTimeMillis();
        
        AppRequest ar = (AppRequest)request.getAttachment();
        if ( ar == null )
            ar = createRequest( request );
        
        ar.getScope().makeThreadLocal();
        ar.getContext()._usage.hit( "bytes_in" , request.totalSize() );
        ar.getContext()._usage.hit( "requests" , 1 );
        
	ar.setResponse( response );
	ar.getContext().getScope().setTLPreferred( ar.getScope() );

        response.setHeader( "X-ctx" , ar.getContext()._root );
        response.setHeader( "X-git" , ar.getContext()._gitBranch );
        response.setHeader( "X-env" , ar.getContext()._environment );

        response.setAppRequest( ar );
        try {
            _handle( request , response , ar );
        }
        finally {
            final long t = System.currentTimeMillis() - start;
            if ( t > 1500 )
                ar.getContext()._logger.getChild( "slow" ).info( request.getURL() + " " + t + "ms" );
            
            ar.done( response );

            ar.getContext()._usage.hit( "cpu_millis" , t );
            ar.getContext()._usage.hit( "bytes_out" , response.totalSize() );
            
            Scope.clearThreadLocal();
        }
    }
    
    private void _handle( HttpRequest request , HttpResponse response , AppRequest ar ){
	JSString jsURI = new JSString( ar.getURI() );
	
        if ( ar.getScope().get( "allowed" ) != null ){
            Object foo = ar.getScope().getFunction( "allowed" ).call( ar.getScope() , request , response , jsURI );
            if ( foo != null ){
                if ( response.getResponseCode() == 200 ){
                    response.setResponseCode( 401 );
                    response.getWriter().print( "not allowed" );
                }
                return;
            }
        }
        
        if ( ar.getURI().equals( "/~f" ) ){
            JSFile f = ar.getContext().getJSFile( request.getParameter( "id" ) );
            if ( f == null ){
                response.setResponseCode( 404 );
                response.getWriter().print( "not found\n\n" );
                return;
            }
            response.sendFile( f );
            return;
        }

        File f = ar.getFile();

	if ( response.getResponseCode() >= 300 )
	    return;

        if ( f.toString().endsWith( ".cgi" ) ){
            handleCGI( request , response , ar , f );
            return;
        }
        
        if ( ar.isStatic() && f.exists() ){
            if ( D ) System.out.println( f );

            if ( f.isDirectory() ){
                response.setResponseCode( 301 );
                response.getWriter().print( "listing not allowed\n" );
                return;
            }
            
	    int cacheTime = getCacheTime( ar , jsURI , request , response );
	    if ( cacheTime >= 0 )
		response.setCacheTime( cacheTime );

            final String fileString = f.toString();
            int idx = fileString.lastIndexOf( "." );
            if ( idx > 0 ){
                String ext = fileString.substring( idx + 1 );
                String type = MimeTypes.get( ext );
                if ( type != null )
                    response.setHeader( "Content-Type" , type );
            }
            response.sendFile( f );
            return;
        }
        
        try {
            JxpServlet servlet = ar.getContext().getServlet( f );
            if ( servlet == null ){
                response.setResponseCode( 404 );
                response.getWriter().print( "not found" );
            }
            else {
                servlet.handle( request , response , ar );
            }
        }
        catch ( JSException.Quiet q ){
            response.setHeader( "X-Exception" , "quiet" );
        }
        catch ( JSException.Redirect r ){
            response.sendRedirectTemporary(r.getTarget());
        }
        catch ( Exception e ){
            handleError( request , response , e , ar.getContext() );
            return;
        }
    }

    void handleError( HttpRequest request , HttpResponse response , Throwable t , AppContext ctxt ){
        if ( ctxt == null )
            ctxt = _contextHolder.getContext( request , null );

        ctxt._logger.error( request.getURL() , t );

        response.setResponseCode( 500 );
        
        JxpWriter writer = response.getWriter();
            
        writer.print( "\n<br><br><hr><b>Error</b><br>" );
        writer.print( t.toString() + "<BR>" );
        
        while ( t != null ){
            for ( StackTraceElement element : t.getStackTrace() ){
                writer.print( element + "<BR>\n" );
            }
            t = t.getCause();
        }
        
    }
    
    int getCacheTime( AppRequest ar , JSString jsURI , HttpRequest request , HttpResponse response ){
        if ( ar.getScope().get( "staticCacheTime" ) == null )
            return -1;

	JSFunction f = ar.getScope().getFunction( "staticCacheTime" );
	if ( f == null )
	    return -1;
	
	Object ret = f.call( ar.getScope() , jsURI , request , response );
	if ( ret == null )
	    return -1;
	
	if ( ret instanceof Number )
	    return ((Number)ret).intValue();

	return -1;
    }

    void handleCGI( HttpRequest request , HttpResponse response , AppRequest ar , File f ){
        try {
            
            if ( ! f.exists() ){
                response.setResponseCode( 404 );
                response.getWriter().print("file not found" );
                return;
            }

            List<String> env = new ArrayList<String>();
            env.add( "REQUEST_METHOD=" + request.getMethod() );
            env.add( "SCRIPT_NAME=" + request.getURI() );
            env.add( "QUERY_STRING=" + request.getQueryString() );
	    env.add( "SERVER_NAME=" + request.getHost() );

            String envarr[] = new String[env.size()];
            env.toArray( envarr );

            Process p = Runtime.getRuntime().exec( new String[]{ f.getAbsolutePath() } , envarr , f.getParentFile() );

            boolean inHeader = true;

            BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            String line;
            while ( ( line = in.readLine() ) != null ){
                if ( inHeader ){
                    if ( line.trim().length() == 0 ){
                        inHeader = false;
                        continue;
                    }
                    continue;
                }
                response.getWriter().print( line );
                response.getWriter().print( "\n" );
            }

            in = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );
            while ( ( line = in.readLine() ) != null ){
                response.getWriter().print( line );
                response.getWriter().print( "\n" );
            }
        }
        catch ( Exception e ){
            ar.getContext()._logger.error( request.getURL() , e );
            response.setResponseCode( 501 );
            response.getWriter().print( "<br><br><hr>" );
            response.getWriter().print( e.toString() );
        }
    }
    
    public double priority(){
        return 10000;
    }

    private final AppContextHolder _contextHolder;

    // ---------

    public static void main( String args[] )
        throws Exception {
        

        String webRoot = "/data/sites/admin/";
        String serverRoot = "/data/sites";

        int portNum = DEFAULT_PORT;

        /*
         *     --port portnum   [root]
         */

        int aLength = 0;
        for ( int i=0; i<args.length; i++ ){
            if ( args[i] != null && args[i].trim().length() > 0 )
                aLength = i +1;
        }
        for (int i = 0; i < aLength; i++) {

            if ("--port".equals(args[i])) {
                portNum = Integer.valueOf(args[++i]);
            }
            else if ("--root".equals(args[i])) {
                portNum = Integer.valueOf(args[++i]);
            }
            else if ("--serverroot".equals(args[i])) {
            	JSHook.whereIsEd = args[++i];
            }
            else {
                if (i != aLength - 1) {
                    System.out.println("error - unknown param " + args[i]);
                    System.exit(1);
                }
                else {
                    webRoot = args[i];
                }
            }
        }

        System.out.println("==================================");
        System.out.println("  10gen AppServer vX");
        System.out.println("     listen port = " + portNum);
        System.out.println("     server root = " + JSHook.whereIsEd);
        System.out.println("         webRoot = " + webRoot);
        System.out.println("      serverRoot = " + serverRoot);
        System.out.println("     listen port = " + portNum);
                System.out.println("==================================");

        AppServer as = new AppServer( webRoot , serverRoot);
        
        HttpServer.addGlobalHandler( as );
        
        HttpServer hs = new HttpServer(portNum);
        hs.start();
        hs.join();
    }
}
