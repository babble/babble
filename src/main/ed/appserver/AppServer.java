// AppServer.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.util.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppServer implements HttpHandler {
    
    public boolean handles( HttpRequest request , Box<Boolean> fork ){
        String uri = request.getURI();
        
        if ( ! uri.startsWith( "/" ) || uri.endsWith( "~" ) || uri.contains( "/.#" ) )
            return false;
        
        AppRequest ar = new AppRequest( request );
        request.setAttachment( ar );
        fork.set( ar.fork() );
        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){
        AppRequest ar = (AppRequest)request.getAttachment();
        if ( ar == null )
            ar = new AppRequest( request );
        
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
        
        
        JxpSource source = _sources.get( f );
        if ( source == null ){
            source = JxpSource.getSource( f );
            _sources.put( f , source );
        }
        JxpServlet servlet = null;
        try {
            servlet = source.getServlet();
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


    private Map<File,JxpSource> _sources = new HashMap<File,JxpSource>();

    public static void main( String args[] )
        throws Exception {
        
        AppServer as = new AppServer();
        HttpServer.addGlobalHandler( as );
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }

}
