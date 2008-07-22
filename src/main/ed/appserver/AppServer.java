// AppServer.java

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

import ed.db.JSHook;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.net.*;
import ed.util.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;
import ed.security.*;

/** The server to handle HTTP requests.
 * @expose
 */
public class AppServer implements HttpHandler {

    /** This appserver's default port, 8080 */
    private static final int DEFAULT_PORT = 8080;

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );

    /** Constructs a newly allocated AppServer object for the site <tt>defaultWebRoot</tt>.
     * @example If one is running the appserver for the site foo.10gen.com and has the directory structure
     *
     * gitroot
     * |- ed
     * |  |- master
     * |  |- 2.1.1
     * |  |- 2.2.0
     * |- foo
     * |  |-bar1.jxp
     * |  |-bar2.jxp
     *
     * And this appserver is started via the commands:
     * $ cd gitroot/ed/master
     * $ ./runAnt.bash ed.appserver.AppServer ../../foo
     *
     * Then this appserver's defaultWebRoot will be "../../foo"
     *
     * @param defaultWebRoot The site that will be run.
     * @param root The location in git of sites ("/data/sites").
     */
    public AppServer( String defaultWebRoot , String root ){
        _contextHolder = new AppContextHolder( defaultWebRoot , root );
    }


    /** Creates a new AppRequest using the given HttpRequest.
     * @param request The HTTP request that needs to be processed
     * @return The corresponding AppRequest
     */
    AppRequest createRequest( HttpRequest request ){
        String newUri[] = new String[1];
        AppContext ac = _contextHolder.getContext( request , newUri );
        return ac.createRequest( request , newUri[0] );
    }

    /** Checks if the request's uri starts with a "/" and, if so, sets some information about the request.
     * If the request's URI is "/~appserverstats", <tt>info.fork</tt> is set to false and <tt>info.admin</tt> is set to true.
     * Otherwise, <tt>info.fork</tt> is set to true.
     * If the URI is "/~~/core/sys", <tt>info.admin</tt> is set to true.
     * @param request HTTP request to be processed
     * @param info The object to store URI information.
     */
    public boolean handles( HttpRequest request , Info info ){
        String uri = request.getURI();

        if ( ! uri.startsWith( "/" ) )
            return false;

        if ( uri.equals( "/~appserverstats" ) ){
            info.fork = false;
            info.admin = true;
            return true;
        }

        info.fork = true;
        info.admin = uri.startsWith( "/~~/core/sys/" );

        return true;
    }

    /** Handles an HTTP request and puts the response in the given HTTP response object.
     * @param request HTTP request to handle
     * @param HTTP response to fill in
     */
    public void handle( HttpRequest request , HttpResponse response ){
        try {
            _handle( request , response );
        }
        catch ( Exception e ){
            handleError( request , response , e , null );
        }
    }

    private void _handle( HttpRequest request , HttpResponse response ){

        if ( request.getURI().equals( "/~appserverstats" ) ){
            handleStats( request , response );
            return;
        }

        final long start = System.currentTimeMillis();

        AppRequest ar = (AppRequest)request.getAttachment();
        if ( ar == null )
            ar = createRequest( request );

        if ( request.getURI().equals( "/~reset" ) ){
            handleReset( ar , request , response );
            return;
        }

        if ( request.getURI().equals( "/~update" ) ){
            handleUpdate( ar , request , response );
            return;
        }

        final AppContext ctxt = ar.getContext();

        ar.getScope().makeThreadLocal();

        final UsageTracker usage = ctxt._usage;
        final SimpleStats stats = _getStats( ctxt._name + ":" + ctxt._environment );

        {
            final int inSize  = request.totalSize();

            usage.hit( "bytes_in" , inSize );
            usage.hit( "requests" , 1 );

            stats.req.hit();
            stats.netIn.hit( inSize );
        }


	ar.setResponse( response );
	ctxt.getScope().setTLPreferred( ar.getScope() );

        response.setHeader( "X-ctx" , ctxt._root );
        response.setHeader( "X-git" , ctxt.getGitBranch() );
        response.setHeader( "X-env" , ctxt._environment );

        response.setAppRequest( ar );

        final ed.db.DBBase db = ctxt.getDB();
        try {
            ar.makeThreadLocal();
            db.requestStart();
            _handle( request , response , ar );
        }
        finally {
            db.requestDone();
            ar.unmakeThreadLocal();

            final long t = System.currentTimeMillis() - start;
            if ( t > 1500 )
                ar.getContext()._logger.getChild( "slow" ).info( request.getURL() + " " + t + "ms" );

            ar.done( response );

            {

                final int outSize = response.totalSize();

                usage.hit( "cpu_millis" , t );
                usage.hit( "bytes_out" , outSize );

                stats.cpu.hit( (int)t );
                stats.netOut.hit( outSize );
            }

            Scope.clearThreadLocal();
        }
    }

    private void _handle( HttpRequest request , HttpResponse response , AppRequest ar ){

        try {

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
		    handle404( request , response , null );
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

            JxpServlet servlet = ar.getContext().getServlet( f );
            if ( servlet == null ){
		handle404( request , response , null );
            }
            else {
                servlet.handle( request , response , ar );
            }
        }
        catch ( OutOfMemoryError oom ){
            handleOutOfMemoryError( oom , response );
        }
        catch ( FileNotFoundException fnf ){
	    handle404( request , response , fnf.getMessage() );
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

    void handle404( HttpRequest request , HttpResponse response , String extra ){
	response.setResponseCode( 404 );
	response.getWriter().print( "not found<br>" );
	
	if ( extra != null )
	    response.getWriter().print( extra + "<BR>" );

	response.getWriter().print( request.getRawHeader().replaceAll( "[\r\n]+" , "<br>" ) );
			
    }

    /** Kills this appserver if it runs out of memory.  Does a garbage collection
     * and checks if enough memory was freed.  If not, or if the response writer fails,
     * the appserver quits with a -3 exit code.
     * @param oom The out-of-memory error
     * @param response The HTTP response to inform the user of the error
     */
    void handleOutOfMemoryError( OutOfMemoryError oom , HttpResponse response ){
        // 2 choices here, this request caused all sorts of problems
        // or the server is screwed.
        
        MemUtil.checkMemoryAndHalt( "AppServer" , oom );

        // either this thread was the problem, or whatever

        try {
            response.setResponseCode( 500 );
            JxpWriter writer = response.getWriter();
            writer.print( "There was an error handling your request (appsrv 123)<br>" );
            writer.print( "ERR 71<br>" );
        }
        catch ( OutOfMemoryError oom2 ){
            // forget it - we're super hosed
            Runtime.getRuntime().halt( -3 );
        }
    }

    /** Handles this appserver's errors.
     * @param request The request that caused the error
     * @param response The response to fill
     * @param t The error thrown
     * @param ctxt The app context to use for logging
     */
    void handleError( HttpRequest request , HttpResponse response , Throwable t , AppContext ctxt ){

        if ( t.getCause() instanceof OutOfMemoryError ){
            handleOutOfMemoryError( (OutOfMemoryError)t.getCause() , response );
            return;
        }

        if ( ctxt == null )
	    Logger.getLogger( "appserver.nocontext" ).error( request.getURI() , t );
	else
	    ctxt._logger.error( request.getURL() , t );

        response.setResponseCode( 500 );

        JxpWriter writer = response.getWriter();

        if ( t instanceof JSCompileException ){
            JSCompileException jce = (JSCompileException)t;

            writer.print( "<br><br><hr>" );
            writer.print( "<h3>Compile Error</h3>" );
            writer.print( "<b>Message:</b> " + jce.getError() + "<BR>" );
            writer.print( "<b>File Name:</b> " + jce.getFileName() + "<BR>" );
            writer.print( "<b>Line # :</b> " + jce.getLineNumber() + "<BR>" );
        }

        else {
            writer.print( "\n<br><br><hr><b>Error</b><br>" );
            writer.print( t.toString() + "<BR>" );

            while ( t != null ){
                for ( StackTraceElement element : t.getStackTrace() ){
                    writer.print( element + "<BR>\n" );
                }
                t = t.getCause();
            }
        }

    }

    /** Determines how long this response should be cached for.
        result is to set Cache-Time and Expires
     * @param ar The app request which contains the staticCacheTime function
     * @param jsURI The URI to find in the cache
     * @param request The HTTP request to process
     * @param response The HTTP response to generate
     * @return The time, in milliseconds
     */
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

    /** Processes a request for a .cgi script.  This checks if the site allows access to cgi scripts and
     * if the script exists.  If the request passes both tests, this function sets up an environment and
     * sysexecs the script and sets the response based on its output.
     * @param request The HTTP request
     * @param response The HTTP response to set
     * @param ar The app request, used to get the site's name to determine whether cgi scripts are allowed
     * @param f The cgi file to execute
     */
    void handleCGI( HttpRequest request , HttpResponse response , AppRequest ar , File f ){

        try {

            if ( ! Security.isAllowedSite( ar.getContext().getName() ) ){
                response.setResponseCode( 501 );
                response.getWriter().print( "you are not allowed to run cgi programs" );
                return;
            }


            if ( ! f.exists() ){
		handle404( request , response , null );
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

                    line = line.trim();
                    final int idx = line.indexOf( ":" );
                    if ( idx > 0 ){
                        final String name = line.substring( 0 , idx ).trim();
                        final String val = line.substring( idx + 1 ).trim();
                        response.setHeader( name , val );
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
            response.setResponseCode( 500 );
            response.getWriter().print( "<br><br><hr>" );
            response.getWriter().print( e.toString() );
        }
    }

    /** This appserver's priority for the HTTP request handler.
     * @return 10000
     */
    public double priority(){
        return 10000;
    }

    /** @unexpose */
    SimpleStats _getStats( String name ){
        SimpleStats stats = _stats.get( name );

        if ( stats == null ){
            stats = new SimpleStats( name );
            _stats.put( name , stats );
        }

        return stats;
    }

    /** Get stats about this site.
     * @param request
     * @param response Sets stats as content of this
     */
    void handleStats( HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );

        JxpWriter out = response.getWriter();

        List<String> lst = new ArrayList<String>();
        lst.addAll( _stats.keySet() );

        Collections.sort( lst );

        out.print( "app server stats\n----\n\n" );

        for ( String site : lst ){
            _stats.get( site ).print( out );
            out.print( "\n--- \n " );
        }
    }

    /** Resets the app content.
     * @param ar Used to find the context to reset and logger
     * @param request
     * @param response Set to inform the user what happens
     */
    void handleReset( AppRequest ar , HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );

        JxpWriter out = response.getWriter();

        out.print( "so, you want to reset?\n" );

        if ( _administrativeAllowed( request ) ){
            out.print( "you did it!\n" );
            ar.getContext()._logger.info("About to reset context via /~reset");
            ar.getContext().reset();
        }
        else {
            ar.getContext()._logger.error("Failed attempted context reset via /~reset from " + request.getRemoteIP());
            out.print( "you suck!" );
            response.setResponseCode(403);
        }

    }

    void handleUpdate( AppRequest ar , HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );
        
        JxpWriter out = response.getWriter();

        out.print( "you are going to update\n" );
        
        if ( _administrativeAllowed( request ) ){
            out.print( "you did it!\n" );
            ar.getContext()._logger.info("About to update context via /~update");
            try {
                String branch = ar.getContext().updateCode();
                if ( branch == null )
                    out.print( "couldn't update." );
                else 
                    out.print( "updated to [" + branch + "]" );
            }
            catch ( Exception e ){
                out.print( "Fail : " + e );
            }
            out.print( "\n" );
        }
        else {
            ar.getContext()._logger.error("Failed attempted context reset via /~update from " + request.getRemoteIP());
            out.print( "you suck!" );
            response.setResponseCode(403);
        }

    }

    boolean _administrativeAllowed( HttpRequest request ){
        return 
            request.getRemoteIP().equals( "127.0.0.1" ) &&
            request.getHeader( "X-Cluster-Cluster-Ip" ) == null;
    }

    class SimpleStats  {

        SimpleStats( String name ){
            this.name = name;
        }

        void print( JxpWriter out ){
            out.print( name + " over 30 seconds\n" );
            printTracker( "req" , req , out );
            printTracker( "cpu (total seconds)" , cpu , out );
            printTracker( "net-in kbps" , netIn , out );
            printTracker( "net-out kbps" , netOut , out );
        }

        void printTracker( String name , ThingsPerTimeTracker tracker , JxpWriter out ){

            tracker.validate();

            out.print( "\t" + name + "\n\t" );
            for ( int i=0; i<tracker.size(); i++ ){

                if ( name.startsWith( "cpu" ) )
                    out.print( JSMath.sigFig( ( (double)tracker.get( i )) / seconds ) );
                else if ( name.startsWith( "net" ) ){
                    double perSec = ((double)tracker.get( i )) / seconds;
                    perSec = perSec / 1024;
                    out.print( JSMath.sigFig( perSec ) );
                }
                else
                    out.print( tracker.get( i ) );

                out.print( " " );
            }

            out.print( "\n" );
        }



        final String name;
        final int seconds = 30;

        final ThingsPerTimeTracker req = new ThingsPerTimeTracker( 1000 * seconds , 30 );
        final ThingsPerTimeTracker cpu = new ThingsPerTimeTracker( 1000 * seconds , 30 );
        final ThingsPerTimeTracker netIn = new ThingsPerTimeTracker( 1000 * seconds , 30 );
        final ThingsPerTimeTracker netOut = new ThingsPerTimeTracker( 1000 * seconds , 30 );
    }

    private final AppContextHolder _contextHolder;
    private final Map<String,SimpleStats> _stats = Collections.synchronizedMap( new StringMap<SimpleStats>() );

    // ---------

    /** @unexpose */
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
