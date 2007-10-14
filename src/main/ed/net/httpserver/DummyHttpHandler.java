// DummyHttpHandler.java

package ed.net.httpserver;

import ed.util.*;

public class DummyHttpHandler {

    public static class Echo implements HttpHandler {

        public boolean handles( HttpRequest request , Box<Boolean> fork ){
            fork.set( request.getBoolean( "fork" , false ) );
            return request.getURI().equals( "/~echo" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            JxpWriter w = response.getWriter();
            int copies = request.getInt( "copies" , 1 );
            for ( int i=0; i<copies; i++ )
                w.print( "abcdefghijklmnopqrstuvwxyv0123456789\n" );
        }
        
        public double priority(){
            return 0;
        }

    }

    static void setup(){}

    static {
        HttpServer.addGlobalHandler( new Echo() );
    }

}
