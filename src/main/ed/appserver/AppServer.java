// AppServer.java

package ed.appserver;

import ed.net.*;
import ed.net.httpserver.*;

public class AppServer implements HttpHandler {
    
    public boolean handles( HttpRequest request ){
        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){
        response.getWriter().print( "yo" );
    }
    
    public boolean fork( HttpRequest request ){
        return true;
    }

    public double priority(){
        return 10000;
    }

    public static void main( String args[] )
        throws Exception {
        
        AppServer as = new AppServer();
        HttpServer.addGlobalHandler( as );
        
        HttpServer hs = new HttpServer( 8080 );
        hs.start();
        hs.join();
    }

}
