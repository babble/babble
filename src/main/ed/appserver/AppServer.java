// AppServer.java

package ed.appserver;

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
    static String OUR_DOMAINS[] = new String[]{ ".latenightcoders.com" , ".10gen.com" };
    static String CDN_HOST[] = new String[]{ "origin." , "origin-local." , "static." , "static-local." , "secure." };

    public AppServer( String defaultWebRoot , String root ){
        _defaultWebRoot = defaultWebRoot;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }

    AppContext getContext( String host , String uri , String newUri[] ){
        if ( newUri != null )
            newUri[0] = null;

        if ( host != null )
            host = host.trim();

        if ( host == null || _root == null || host.length() == 0 ){
            if ( D ) System.out.println( " using default context for [" + host + "]" );
            return _getDefaultContext();
        }

        AppContext ac = _context.get( host );
        if ( ac != null )
            return ac;

        {
            int colon = host.indexOf( ":" );
            if ( colon > 0 )
                host = host.substring( 0 , colon );
        }

        // raw {admin.latenightcoders.com}
        File temp = new File( _root , host );
        if ( temp.exists() )
            return getFinalContext( temp , host , host );
        
        // check for virtual hosting under us 
        // foo.latenightcoders.com -> foo.com
        String useHost = host;
        for ( String d : OUR_DOMAINS ){
            if ( useHost.endsWith( d ) ){
                useHost = useHost.substring( 0 , useHost.length() - d.length() ) + ".com";
                break;
            }
        }
        if ( useHost.startsWith( "www." ) )
            useHost = useHost.substring( 4 );
        
        if ( uri != null && uri.length() > 0 && uri.indexOf( "/" , 1 ) > 0 ){
            for ( String d : CDN_HOST ){
                if ( useHost.startsWith( d ) ){
                    String thing = uri.substring(1);

                    int idx = thing.indexOf( "/" );
                    String newUriNow = thing.substring( idx );                    
                    thing = thing.substring( 0 , thing.indexOf( "/" ) );
                    
                    if ( newUri != null )
                        newUri[0] = newUriNow;
                    return getContext( thing , newUriNow , null );
                }
            }
        }
	
	if ( useHost.equals( "corejs.com" ) ){
	    return _coreContext;
	}

        if ( D ) System.out.println( "useHost : " + useHost );
        
        // check for full host
        temp = new File( _root , useHost );
        if ( temp.exists() )
            return getFinalContext( temp , host , useHost );
        
        // domain www.{alleyinsider.com}
        String domain = useHost.indexOf(".") >= 0 ? DNSUtil.getDomain( useHost ) : useHost;
        temp = new File( _rootFile , domain );
        if ( temp.exists() )
            return getFinalContext( temp , host , useHost );

        // just name www.{alleyinsider}.com
        int idx = domain.indexOf( "." );
        if ( idx > 0 ){
            temp = new File( _rootFile , domain.substring( 0 , idx ) );
            if ( temp.exists() )
                return getFinalContext( temp , host , useHost );
        }

        return _getDefaultContext();
    }

    AppContext getFinalContext( File f , String host , String useHost ){
        if ( ! f.exists() )
            throw new RuntimeException( "trying to map to " + f + " which doesn't exist" );

        AppContext ac = _context.get( host );
        if ( ac != null )
            return ac;
        
        ac = _context.get( f.toString() );
        if ( ac != null )
            return ac;
        
        if ( D ) System.out.println( "mapping directory [" + host + "] to " + f );
        
        if ( ! hasGit( f ) ){
            if ( D ) System.out.println( "\t this is a holder for branches" );
            f = getBranch( f , DNSUtil.getSubdomain( useHost ) );
            if ( D ) System.out.println( "\t using full path : " + f );
            
        }

        ac = new AppContext( f );
        _context.put( host , ac );
        _context.put( f.toString() , ac );
        return ac;
    }

    private boolean hasGit( File test ){
        File f = new File( test , ".git" );
        if ( f.exists() )
            return true;

        f = new File( test , "dot-git" );
        if ( f.exists() )
            return true;

        return false;
        
    }

    private static final String LOCAL_BRANCH_LIST[] = new String[]{ "master" , "test" , "www" };
    private static final String WWW_BRANCH_LIST[] = new String[]{ "test" , "master" };
    
    File getBranch( File root , String subdomain ){
        File test = new File( root , subdomain );
        if ( test.exists() )
            return test;
        
        if ( subdomain.equals( "dev" ) ){
            test = new File( root , "master" );
            if ( test.exists() )
                return test;
        }
        
        String searchList[] = null;
        
        if ( subdomain.equals( "local" ) )
            searchList = LOCAL_BRANCH_LIST;
        else if ( subdomain.equals( "www" ) )
            searchList = WWW_BRANCH_LIST;

        if ( searchList != null ){
            for ( int i=0; i<searchList.length; i++ ){
                test = new File( root , searchList[i] );
                if ( test.exists() )
                    return test;
            }
        }
        
        throw new RuntimeException( "can't find branch for subdomain : " + subdomain );
    }
            
    public AppContext getContext( HttpRequest request , String newUri[] ){
        return getContext( request.getHeader( "Host" ) , request.getURI() , newUri );
    }
    
    public AppRequest createRequest( HttpRequest request ){
        String newUri[] = new String[1];
        AppContext ac = getContext( request , newUri );
        return ac.createRequest( request , newUri[0] );
    }
    
    public boolean handles( HttpRequest request , Box<Boolean> fork ){
        String uri = request.getURI();
        
        if ( ! uri.startsWith( "/" ) )
            return false;
        
        AppRequest ar = createRequest( request );
        request.setAttachment( ar );
        fork.set( ar.fork() );
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

        response.setAppRequest( ar );
        try {
            _handle( request , response , ar );
        }
        finally {
            final long t = System.currentTimeMillis() - start;
            if ( t > 1500 )
                ar.getContext()._logger.getChild( "slow" ).info( request.getURL() + " " + t + "ms" );
            
            ar.getContext().getScope().setTLPreferred( null );
            
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
            ctxt = getContext( request , null );

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

    private AppContext _getContextFromMap( String host ){
        AppContext ac = _context.get( host );
        if ( ac == null )
            return null;
        
        if ( ! ac._reset )
            return ac;

        _context.put( host , null );
        return null;
    }
    
    private synchronized AppContext _getDefaultContext(){
        if ( _defaultContext != null && _defaultContext._reset )
            _defaultContext = null;
        
        if ( _defaultContext != null )
            return _defaultContext;
        
        _defaultContext = new AppContext( _defaultWebRoot );
        return _defaultContext;
    }

    private final String _defaultWebRoot;
    private AppContext _defaultContext;
    private final AppContext _coreContext = new AppContext( CoreJS.getDefaultRoot() );
    private final String _root;
    private final File _rootFile;
    private final Map<String,AppContext> _context = Collections.synchronizedMap( new StringMap<AppContext>() );
    
    public static void main( String args[] )
        throws Exception {

        String webRoot = "/data/sites/admin/";
        String serverRoot = "/data/sites";

        int portNum = DEFAULT_PORT;

        /*
         *     --port portnum   [root]
         */
        for (int i = 0; i < args.length; i++) {

            if ("--port".equals(args[i])) {
                portNum = Integer.valueOf(args[++i]);
            }
            else if ("--root".equals(args[i])) {
                portNum = Integer.valueOf(args[++i]);
            }
            else {
                if (i != args.length - 1) {
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
