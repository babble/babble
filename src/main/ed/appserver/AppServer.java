// AppServer.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.appserver;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import ed.*;
import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.net.*;
import ed.util.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;
import ed.security.*;
import ed.lang.*;


/** The server to handle HTTP requests.
 */
public class AppServer implements HttpHandler , MemUtil.MemHaltDisplay {

    /** This appserver's default port, 8080 */
    static final int DEFAULT_PORT = 8080;
    static final int DEFAULT_CACHE_S = 3600;

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );

    static final boolean LEAK_HUNT = Boolean.getBoolean( "LEAK-HUNT" );
    static final Semaphore LEAK_HUNT_SEMAPHORE = new Semaphore( 1 );

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

    public void addToServer(){
        HttpServer.addGlobalHandler( this );
        HttpServer.addGlobalHandler( new HttpMonitor( "appserverstats" ){
                public void handle( MonitorRequest mr ){
                    if ( mr.html() ){
                        mr.addHeader( "App Server Stats" );
                        mr.addHeader( "30 second intervals" );
                    }

                    List<String> lst = new ArrayList<String>();
                    lst.addAll( _stats.keySet() );

                    Collections.sort( lst );

                    for ( String site : lst ){
                        _stats.get( site ).displayGraph( mr.getWriter() , _displayOptions );
                    }
                }
            }
            );
        _contextHolder.addToServer();
    }

    /** Creates a new AppRequest using the given HttpRequest.
     * @param request The HTTP request that needs to be processed
     * @return The corresponding AppRequest
     */
    AppRequest createRequest( HttpRequest request ){
        AppContextHolder.Result r = _contextHolder.getContext( request );
        if ( r == null || r.context == null )
            return null;
        return r.context.createRequest( request , r.host , r.uri );
    }

    /** Checks if the request's uri starts with a "/" and, if so, sets some information about the request.
     * Otherwise, <tt>info.fork</tt> is set to true.
     * If the URI is "/~~/core/sys", <tt>info.admin</tt> is set to true.
     * @param request HTTP request to be processed
     * @param info The object to store URI information.
     */
    public boolean handles( HttpRequest request , Info info ){
        String uri = request.getURI();

        if ( ! uri.startsWith( "/" ) )
            return false;

        info.fork = true;
        info.admin = uri.startsWith( "/~~/core/sys/" );

        return true;
    }

    /** Handles an HTTP request and puts the response in the given HTTP response object.
     * @param request HTTP request to handle
     * @param response to fill in
     */
    public boolean handle( HttpRequest request , HttpResponse response ){
        try {
            _handle( request , response );
        }
        catch ( Exception e ){
            handleError( request , response , e , null );
        }
        finally {
            request.setAppRequest( null );
        }
        return true;
    }

    private void _handle( HttpRequest request , HttpResponse response ){

        final long start = System.currentTimeMillis();

        AppRequest ar = request.getAppRequest();
        if ( ar == null ){
            ar = createRequest( request );
            if ( ar == null ){
                handleNoSite( request , response );
                return;
            }
            request.setAppRequest( ar );
        }

        if ( request.getURI().equals( "/~reset" ) ){
            handleReset( ar , request , response );
            return;
        }

        if ( request.getURI().equals( "/~update" ) ){
            handleUpdate( ar , request , response );
            return;
        }

        if ( request.getURI().equals( "/~admin" ) ){
            handleAdmin( ar , request , response );
            return;
        }

        final AppContext ctxt = ar.getContext();

        ar.setResponse( response );
        ar.getScope().makeThreadLocal();
        ctxt.getLogger().makeThreadLocal();

        final UsageTracker usage = ctxt._usage;
        final HttpLoadTracker stats = getTracker( ctxt );

        {
            final int inSize  = request.totalSize();
            usage.hit( "bytes_in" , inSize );
            usage.hit( "requests" , 1 );
        }

        ctxt.setTLPreferredScope( ar , ar.getScope() );

        response.setHeader( "X-ctx" , ctxt._root );
        response.setHeader( "X-git" , ctxt.getGitBranch() );
        response.setHeader( "X-env" , ctxt._environment );
        response.setHeader( "X-ctxhash" , String.valueOf( System.identityHashCode( ctxt ) ) ); // this is kind of tempoary until AppContextHolder is totally vettedx

        response.setAppRequest( ar );

        ar.turnOnDevFeatures();

        final ed.db.DBBase db = ctxt.getDB();
        try {
            ar.makeThreadLocal();
            _requestMonitor.watch( ar );
            db.requestStart();
            AppSecurityManager.ready();

            _handle( request , response , ar );
        }
        finally {
            db.requestDone();
            ar.unmakeThreadLocal();
            Logger.setThreadLocalAppender( null );

            final long t = System.currentTimeMillis() - start;
            if ( t > 1500 )
                ar.getContext().getLogger().getChild( "slow" ).info( request.getURL() + " " + t + "ms" );

            ar.done( response );

            {
                final int outSize = response.totalSize();
                usage.hit( "cpu_millis" , t );
                usage.hit( "bytes_out" , outSize );
            }

            stats.hit( request , response );
            Scope.clearThreadLocal();
        }
    }

    private void _handle( HttpRequest request , HttpResponse response , AppRequest ar ){

        _currentRequests.add( ar );
        try {

            JSString jsURI = new JSString( ar.getURI() );

            if ( ar.getFromInitScope( "allowed" ) != null ){
                Object foo = ((JSFunction)ar.getFromInitScope( "allowed" )).call( ar.getScope() , request , response , jsURI );
                if ( foo != null ){
                    if ( response.getResponseCode() == 200 ){
                        response.setResponseCode( 401 );
                        response.getJxpWriter().print( "not allowed" );
                    }
                    return;
                }
            }

            if ( ar.getURI().equals( "/~f" ) ){
                JSFile f = ar.getContext().getJSFile( request.getParameter( "id" ) );
                if ( f == null ){
                    handle404( ar , request , response , null );
                    return;
                }
                response.sendFile( f );
                return;
            }

            File f = ar.getFile();

            if ( response.getResponseCode() >= 300 )
                return;

            if ( ar.isStatic() && f.exists() ){
                if ( D ) System.out.println( f );

                if ( f.isDirectory() ){
                    response.setResponseCode( 301 );
                    response.getJxpWriter().print( "listing not allowed\n" );
                    return;
                }

                int cacheTime = getCacheTime( ar , f , jsURI , request , response );
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

                if ( response.getHeader( "Content-Type" ).startsWith( "text/css" ) ){
                    CSSFixer fixer = new CSSFixer( ar.getURLFixer() );
                    fixer.fix( new FileInputStream( f ) , response.getJxpWriter() );
                }
                else {
                    response.sendFile( f );
                }
                return;
            }

            JxpServlet servlet = ar.getServlet( f );
            if ( servlet == null ){
                handle404( ar , request , response , null );
                return;
            }

            final AppContext ac = ar.getContext();
            
            // actually have a servlet here
            SeenPath reachableBefore = null;
            long sizeBefore = 0;
            if ( LEAK_HUNT ){
                LEAK_HUNT_SEMAPHORE.acquire();
                reachableBefore = new SeenPath( true );
                sizeBefore = ac.approxSize( reachableBefore );
            }

            servlet.handle( request , response , ar );
            _handleEndOfServlet( request , response , ar );

            if ( LEAK_HUNT )
                MemTools.leakHunt( ac , ar , reachableBefore , sizeBefore );
               
        }
        catch ( StackOverflowError internal ){
            handleError( request , response , internal , ar.getContext() );
        }
        catch ( AssertionError internal ){
            handleError( request , response , internal , ar.getContext() );
        }
        catch ( NoClassDefFoundError internal ){
            handleError( request , response , internal , ar.getContext() );
        }
        catch ( OutOfMemoryError oom ){
            handleOutOfMemoryError( oom , response );
        }
        catch ( FileNotFoundException fnf ){
            handle404( ar , request , response , fnf.getMessage() );
        }
        catch ( JSException.Quiet q ){
            response.setHeader( "X-Exception" , "quiet" );
        }
        catch ( JSException.Redirect r ){
            response.sendRedirectTemporary(r.getTarget());
        }
        catch ( AppServerError ase ){
            ar.getScope().clearToThrow();
            handleError( request , response , ase , ar.getContext() );
        }
        catch ( Exception e ){
            handleError( request , response , e , ar.getContext() );
        }
        finally {
            _currentRequests.remove( ar );
            if ( LEAK_HUNT )
                LEAK_HUNT_SEMAPHORE.release();
        }
    }

    void _handleEndOfServlet( HttpRequest request , HttpResponse response , AppRequest ar ){


        String contentType = response.getHeader( "Content-Type" );
        if ( contentType != null && contentType.indexOf( "text/html" ) < 0 )
            return;

        if (request.getHeader(_X10GEN_DEBUG) != null || request.get( _X10GEN_DEBUG ) != null ){
            return;
        }

        // TODO: Eliot, be smarter about this
        if ( response.getHeader( "Content-Length" ) != null )
            return;

        final JSObject user;
        {
            Object userMaybe = ar.getScope().get( "user" );
            if ( userMaybe instanceof JSObject )
                user = (JSObject)userMaybe;
            else
                user = null;
        }

        final JxpWriter out = response.getJxpWriter();

        out.print( "\n<!-- " );
        out.print( DNSUtil.getLocalHostString() );
        out.print( "  " );
        out.print( System.currentTimeMillis() - ar._created );
        out.print( "ms " );
        if ( ar._somethingCompiled )
            out.print( " compile " );
        out.print( " -->\n" );

        if ( ar._profiler != null && showProfilingInfo( request , user ) ){
            out.print( "<!--\n" );
            out.print( ar._profiler.toString() );
            out.print( "\n-->\n" );
        }

        if ( ar._appenderWriter != null ){
            out.print( "<!--\n" );
            ar._appenderWriter.flush();
            out.print( ar._appenderStream.toString() );
            out.print( "\n-->\n" );
        }
    }

    boolean showProfilingInfo( HttpRequest request , JSObject user ){
        if ( request.getBoolean( "profile" , false ) )
            return true;

        return user != null &&
                (user.get("permissions") instanceof JSArray) &&
                ((JSArray) (user.get("permissions"))).contains("admin");
    }

    /** Creates a 404 (page not found) response.
     * @param ar the server's request object
     * @param request the HTTP request for the non-existent page
     * @param response the HTTP response to send back to the client
     * @param extra any extra text to add to the response
     */
    void handle404( AppRequest ar , HttpRequest request , HttpResponse response , String extra ){
        response.setResponseCode( 404 );

        if ( ar.getFromInitScope( "handle404" ) instanceof JSFunction){
            JSFunction func = (JSFunction)ar.getFromInitScope( "handle404" );
            JxpServlet serv = new JxpServlet( ar.getContext() , func );
            serv.handle( request , response , ar );
            return;
        }

        response.getJxpWriter().print( "not found<br>" );

        if ( extra != null )
            response.getJxpWriter().print( extra + "<BR>" );

        response.getJxpWriter().print( request.getRawHeader().replaceAll( "[\r\n]+" , "<br>" ) );

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

        MemUtil.checkMemoryAndHalt( "AppServer" , oom , this );

        if ( response.isCommitted() ){
            // not much we can do with this
            return;
        }

        // either this thread was the problem, or whatever

        try {
            response.setResponseCode( 500 );
            JxpWriter writer = response.getJxpWriter();
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

        final Logger myLogger = ctxt == null ? _noContextLogger : ctxt.getLogger();

        if ( t.getCause() instanceof OutOfMemoryError ){
            handleOutOfMemoryError( (OutOfMemoryError)t.getCause() , response );
            return;
        }

        myLogger.error( request.getFullURL() , t );

        if ( response.isCommitted() ){
            myLogger.error( "handleError called but response already commited.  request:" + request.getFullURL() );
            return;
        }

        response.setResponseCode( 500 );

        JxpWriter writer = response.getJxpWriter();

        if ( t instanceof JSCompileException ){
            JSCompileException jce = (JSCompileException)t;

            writer.print( "<br><br><hr>" );
            writer.print( "<h3>Compile Error</h3>" );
            writer.print( "<b>Message:</b> " + jce.getError() + "<BR>" );
            writer.print( "<b>File Name:</b> " + jce.getFileName() + "<BR>" );
            writer.print( "<b>Line # :</b> " + jce.getLineNumber() + "<BR>" );

            writer.print( "<!--\n" );
            for ( StackTraceElement element : t.getStackTrace() ){
                writer.print( element + "<BR>\n" );
            }
            writer.print( "-->" );
        }
        else {
            writer.print( "\n<br><br><hr><b>Error</b><br>" );
            writer.print("<pre>\n");


            StackTraceElement[] parentTrace = new StackTraceElement[0];
            while(t != null) {
                //message
                writer.print( Encoding._escapeHTML(t.toString()) + "\n");

                //Compute # frames in common
                StackTraceElement[] currentTrace = t.getStackTrace();

                int m = currentTrace.length-1, n = parentTrace.length-1;
                while (m >= 0 && n >=0 && currentTrace[m].equals(parentTrace[n])) {
                    m--; n--;
                }
                int framesInCommon = currentTrace.length - 1 - m;

                //print the frames
                for(int i=0; i<= m; i++)
                    writer.print("\tat " + Encoding._escapeHTML(currentTrace[i].toString()) +"\n");
                if(framesInCommon != 0)
                    writer.print("\t... " + framesInCommon + " more\n");

                parentTrace = currentTrace;
                t = t.getCause();

                if(t != null)
                    writer.print("Caused by: ");
            }

            writer.print("</pre>\n");
        }
    }

    void handleNoSite( HttpRequest request , HttpResponse response ){
        response.setResponseCode( 404 );
        response.getJxpWriter().print( "No site for <b>" + request.getHost() + "</b>" );
    }

    /** Determines how long this response should be cached for.
        result is to set Cache-Time and Expires
     * @param ar The app request which contains the staticCacheTime function
     * @param jsURI The URI to find in the cache
     * @param request The HTTP request to process
     * @param response The HTTP response to generate
     * @return The time, in seconds
     */
    static int getCacheTime( AppRequest ar , File file , JSString jsURI , HttpRequest request , HttpResponse response ){

        if ( ar.getFromInitScope( "staticCacheTime" ) != null ){
            JSFunction f = (JSFunction)ar.getFromInitScope( "staticCacheTime" );
            if ( f != null ){
                Object ret = f.call( ar.getScope() , jsURI , request , response );
                if ( ret instanceof Number )
                    return ((Number)ret).intValue();
            }
        }
        
        if ( ! ar.isStatic() ||
             request.getParameter( "lm" ) == null ||
             request.getParameter( "ctxt" ) == null )
            return -1;

        if ( ! file.exists() )
            return -1;
        
        if ( request.getParameter( "lm" ).equals( URLFixer.LM404 ) ){
            // this is the really interesting one
            // the cache url is wrong, but it does exist
            return -1;
        }

        return DEFAULT_CACHE_S;
    }

    /** This appserver's priority for the HTTP request handler.
     * @return 10000
     */
    public double priority(){
        return 10000;
    }

    /** Resets the app content.
     * @param ar Used to find the context to reset and logger
     * @param request object representing client request
     * @param response Set to inform the user what happens
     */
    void handleReset( AppRequest ar , HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );

        JxpWriter out = response.getJxpWriter();

        out.print( "so, you want to reset?\n" );

        if ( _administrativeAllowed( request ) ){
            ar.getContext().getLogger().info("creating new context" );

            ar.getContext().updateCode();
            AppContext newContext = ar.getContext().newCopy();
            newContext.getScope();
            newContext.getFileSafe( "index.jxp" );
            _contextHolder.replace( ar.getContext() , newContext );

            ar.getContext().getLogger().info("done creating new context.  resetting" );
            ar.getContext().reset();

            out.print( "you did it!\n" );
            out.print( "old hash:" + System.identityHashCode( ar.getContext() ) + "\n" );
            out.print( "new hash:" + System.identityHashCode( newContext ) + "\n" );
        }
        else {
            ar.getContext().getLogger().error("Failed attempted context reset via /~reset from " + request.getRemoteIP());
            out.print( "you suck!" );
            response.setResponseCode(403);
        }

    }

    void handleUpdate( AppRequest ar , HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );

        JxpWriter out = response.getJxpWriter();

        out.print( "you are going to update\n" );

        if ( _administrativeAllowed( request ) ){
            out.print( "you did it!\n" );
            ar.getContext().getLogger().info("About to update context via /~update");
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
            ar.getContext().getLogger().error("Failed attempted context reset via /~update from " + request.getRemoteIP());
            out.print( "you suck!" );
            response.setResponseCode(403);
        }
    }

    void handleAdmin( AppRequest ar , HttpRequest request , HttpResponse response ){

        JxpWriter out = response.getJxpWriter();

        if ( ! _administrativeAllowed( request ) ){
            out.print( "you are not allowed here" );
            return;
        }

        out.print( "<html><head>" );
        out.print( "<title>admin | " + request.getHost() + "</title>" );
        out.print( "</head><body>" );

        out.print( "<h1>Quick Admin</h1>" );

        out.print( "<table border='1'>" );
        out.print( "<tr><th>Created</th><td>" + ar.getContext()._created + "</td></tr>" );
        out.print( "<tr><th>Memory (kb)</th><td>" + ( ar.getContext().approxSize() / 1024 ) + "</td></tr>" );
        out.print( "</table>" );

        out.print( "<h3>Stats</h3>" );
        getTracker( ar.getContext() ).displayGraph( out , _displayOptions );

        out.print( "<hr>" );

        out.print( "<a href='/~update'>Update Code</a> | " );
        out.print( "<a href='/~reset'>Reset Site (include code update)</a> | " );

        out.print( "</body></html>" );
    }

    boolean _administrativeAllowed( HttpRequest request ){
        return
            request.getPhysicalRemoteAddr().equals( "127.0.0.1" ) &&
            request.getHeader( "X-Cluster-Cluster-Ip" ) == null;
    }

    HttpLoadTracker getTracker( final AppContext ctxt ){
        final String site = ctxt._name;
        final String env = ctxt._environment;
        final String name = site + ":" + ( env == null ? "NONE" : env );

        HttpLoadTracker t = _stats.get( name );
        if ( t != null )
            return t;

        synchronized ( _stats ){
            t = _stats.get( name );
            if ( t == null ){
                t = new HttpLoadTracker( name , 30 , 30 );
                _stats.put( name , t );
            }
        }

        return t;
    }

    public void printMemInfo(){
        System.out.println( "AppServer.printMemInfo" );
        for ( AppRequest ar : _currentRequests ){
            System.err.print( "\t" );
            System.err.print( ar._host );
            System.err.print( "\t" );
            System.err.print( ar._uri );
            System.err.println();
        }
    }

    public String toString(){
        return "AppServer";
    }

    private final AppContextHolder _contextHolder;
    private final Map<String,HttpLoadTracker> _stats = Collections.synchronizedMap( new StringMap<HttpLoadTracker>() );
    private final WatchableRequestMonitor _requestMonitor = WatchableRequestMonitor.getInstance();
    private final HttpLoadTracker.GraphOptions _displayOptions = new HttpLoadTracker.GraphOptions( 400 , 100 , true , true , true );
    private final IdentitySet<AppRequest> _currentRequests = new IdentitySet<AppRequest>();

    protected final static String _X10GEN_DEBUG = "X-10gen-Debug"; // private header - if set, user/profile/timing info won't be appended to response

    static final Logger _noContextLogger = Logger.getLogger( "appserver.nocontext" );

    // ---------

    /** @unexpose */
    public static void main( String args[] )
        throws Exception {

        String webRoot = null;
        String sitesRoot = "/data/sites";

        int portNum = DEFAULT_PORT;
        boolean secure = false;

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
                EDFinder.whereIsEd = args[++i];
            }
            else if ( "--sitesRoot".equals( args[i] ) ){
                sitesRoot = args[++i];
            }
            else if ( "--secure" .equals( args[i] ) ){
                secure = true;
            }
            else if ( "--insecure" .equals( args[i] ) ){
                secure = false;
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
        System.out.println("     server root = " + EDFinder.whereIsEd);
        System.out.println("         webRoot = " + webRoot);
        System.out.println("       sitesRoot = " + sitesRoot);
        System.out.println("     listen port = " + portNum);
        System.out.println("          secure = " + secure);
        if ( LEAK_HUNT )
            System.out.println( "   LEAK HUNT ENABLED" );
        System.out.println("==================================");

        AppServer as = new AppServer( webRoot , sitesRoot );
        if ( as._contextHolder._getDefaultContext() != null )
            as._contextHolder._getDefaultContext().getScope();
        as.addToServer();

        HttpMonitor.setApplicationType( "Application Server" );
        HttpServer hs = new HttpServer(portNum);
        if ( secure )
            System.setSecurityManager( new AppSecurityManager() );
        hs.start();
        hs.join();
        if( !hs.hadCleanShutdown() )
            System.exit( -1 );
    }
}
