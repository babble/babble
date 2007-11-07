// AppServer.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.net.*;
import ed.util.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppServer implements HttpHandler {

    static boolean D = Boolean.getBoolean( "DEBUG.APP" );
    static String OUR_DOMAINS[] = new String[]{ ".latenightcoders.com" };

    public AppServer( AppContext defaultContext ){
        this( defaultContext , null );
    }
    
    public AppServer( String defaultContext , String root ){
        this( new AppContext( defaultContext ) , root );
    }

    public AppServer( AppContext defaultContext , String root ){
        _defaultContext = defaultContext;
        _root = root;
        _rootFile = _root == null ? null : new File( _root );
    }

    public AppContext getContext( HttpRequest request ){
        return getContext( request.getHeader( "Host" ) );
    }
    
    public AppContext getContext( String host ){
        if ( host != null )
            host = host.trim();
        if ( host == null || _root == null || host.length() == 0 )
            return _defaultContext;
        


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
            return getFinalContext( temp , host );
        
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
        
        
        // check for full host
        temp = new File( _root , useHost );
        if ( temp.exists() )
            return getFinalContext( temp , host );
        
        // domain www.{alleyinsider.com}
        String domain = DNSUtil.getDomain( useHost );
        temp = new File( _rootFile , domain );
        if ( temp.exists() )
            return getFinalContext( temp , host );

        // just name www.{alleyinsider}.com
        int idx = domain.indexOf( "." );
        if ( idx > 0 ){
            temp = new File( _rootFile , domain.substring( 0 , idx ) );
            if ( temp.exists() )
                return getFinalContext( temp , host );
        }

        return _defaultContext;
    }

    AppContext getFinalContext( File f , String host ){
        AppContext ac = _context.get( host );
        if ( ac != null )
            return ac;
        
        // TODO: branches, etc...
        ac = new AppContext( f );
        _context.put( host , ac );
        return ac;
    }
    
    public AppRequest createRequest( HttpRequest request ){
        return getContext( request ).createRequest( request );
    }
    
    public boolean handles( HttpRequest request , Box<Boolean> fork ){
        String uri = request.getURI();
        
        if ( ! uri.startsWith( "/" ) || uri.endsWith( "~" ) || uri.contains( "/.#" ) )
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
            e.printStackTrace();
            response.setResponseCode( 501 );
            response.getWriter().print( "<br><br><hr>" );
            response.getWriter().print( e.toString() );
        }
    }

    private void _handle( HttpRequest request , HttpResponse response ){
        final long startTime = System.currentTimeMillis();
        
        AppRequest ar = (AppRequest)request.getAttachment();
        if ( ar == null )
            ar = createRequest( request );

	JSString jsURI = new JSString( request.getURI() );
	
        JSFunction allowed = ar.getScope().getFunction( "allowed" );
        if ( allowed != null ){
            Object foo = allowed.call( ar.getScope() , request , response , jsURI );
            if ( foo != null ){
                response.setResponseCode( 401 );
                response.getWriter().print( "not allowed" );
                return;
            }
        }
        
        File f = ar.getFile();

        if ( f.toString().endsWith( ".cgi" ) ){
            handleCGI( request , response , ar , f );
            return;
        }
        
        if ( ar.isStatic() ){
            if ( D ) System.out.println( f );
            if ( ! f.exists() ){
                response.setResponseCode( 404 );
                response.getWriter().print( "file not found\n" );
                return;
            }
            if ( f.isDirectory() ){
                response.setResponseCode( 301 );
                response.getWriter().print( "listing not allowed\n" );
                return;
            }
            
	    int cacheTime = getCacheTime( ar , jsURI );
	    if ( cacheTime >= 0 )
		response.setCacheTime( cacheTime );

            final String fileString = f.toString();
            int idx = fileString.lastIndexOf( "." );
            if ( idx > 0 ){
                String ext = fileString.substring( idx + 1 );
                String type = _mimeTypes.getProperty( ext );
                if ( type != null )
                    response.setHeader( "Content-Type" , type );
            }
            response.sendFile( f );
            return;
        }
        
        try {
            JxpServlet servlet = ar.getContext().getServlet( f );
            servlet.handle( request , response , ar );
        }
        catch ( Exception e ){
            e.printStackTrace();
            response.setResponseCode( 501 );
            response.getWriter().print( "<br><br><hr>" );
            response.getWriter().print( e.toString() );
            return;
        }
        finally {
            final long endTime = System.currentTimeMillis();
            response.getWriter().print( "\n<!-- full page exec time : " + ( endTime - startTime ) + "ms -->\n" );
        }
        
    }
    
    int getCacheTime( AppRequest ar , JSString jsURI ){
	JSFunction f = ar.getScope().getFunction( "staticCacheTime" );
	if ( f == null )
	    return -1;
	
	Object ret = f.call( ar.getScope() , jsURI );
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
            e.printStackTrace();
            response.setResponseCode( 501 );
            response.getWriter().print( "<br><br><hr>" );
            response.getWriter().print( e.toString() );
        }
    }
    
    public double priority(){
        return 10000;
    }

    
    private final AppContext _defaultContext;
    private final String _root;
    private final File _rootFile;
    private final Map<String,AppContext> _context = Collections.synchronizedMap( new StringMap<AppContext>() );
    
    static final Properties _mimeTypes;
    static {
        try {
            _mimeTypes = new Properties();
            _mimeTypes.load( ClassLoader.getSystemClassLoader().getResourceAsStream( "mimetypes.properties" ) );
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }

    public static void main( String args[] )
        throws Exception {
        
        String root = "src/test/samplewww";
        if ( args != null && args.length > 0 ) 
            root = args[0];

        AppContext ac = new AppContext( root );

        AppServer as = new AppServer( ac , "src/www/" );
        
        HttpServer.addGlobalHandler( as );
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }

}
