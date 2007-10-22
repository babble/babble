// AppServer.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.util.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppServer implements HttpHandler {

    public AppServer( AppContext defaultContext ){
        _defaultContext = defaultContext;
    }

    public AppContext getContext( HttpRequest request ){
        return _defaultContext;
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
        AppRequest ar = (AppRequest)request.getAttachment();
        if ( ar == null )
            ar = createRequest( request );
        
        File f = ar.getFile();
        
        if ( ar.isStatic() ){
            System.out.println( f );
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

    }
    
    public double priority(){
        return 10000;
    }

    
    private final AppContext _defaultContext;

    public static void main( String args[] )
        throws Exception {
        
        String root = "src/test/samplewww";
        if ( args != null && args.length > 0 ) 
            root = args[0];

        AppContext ac = new AppContext( root );
        AppServer as = new AppServer( ac );
        
        HttpServer.addGlobalHandler( as );
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }

}
