// DummyHttpHandler.java

package ed.net.httpserver;

import ed.util.*;

public class DummyHttpHandler {

    static void _handle( HttpRequest request , HttpResponse response ){
        response.getWriter().print( "abcdefghijklmnopqrstuvwxyv0123456789\n" );
    }

    public static class EchoNonFork implements HttpHandler {

        public boolean handles( HttpRequest request , Box<Boolean> fork ){
            fork.set( false );
            return request.getURI().equals( "/~echo" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            _handle( request , response );
        }
        
        public double priority(){
            return 0;
        }

    }

    public static class EchoFork implements HttpHandler {

        public boolean handles( HttpRequest request , Box<Boolean> fork ){
            fork.set( true );
            return request.getURI().equals( "/~echoFork" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            _handle( request , response );
        }

        public double priority(){
            return 0;
        }
        
    }

    static void setup(){}

    static {
        HttpServer.addGlobalHandler( new EchoNonFork() );
        HttpServer.addGlobalHandler( new EchoFork() );
    }

}
