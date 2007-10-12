// DummyHttpHandler.java

package ed.net.httpserver;

public class DummyHttpHandler {

    static void _handle( HttpRequest request , HttpResponse response ){
        response.getWriter().print( "abcdefghijklmnopqrstuvwxyv0123456789\n" );
    }

    public static class EchoNonFork implements HttpHandler {

        public boolean handles( HttpRequest request ){
            return request.getURI().equals( "/~echo" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            _handle( request , response );
        }
        
        public boolean fork(){
            return false;
        }
    }

    public static class EchoFork implements HttpHandler {
        public boolean handles( HttpRequest request ){
            return request.getURI().equals( "/~echoFork" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            _handle( request , response );
        }
        
        public boolean fork(){
            return true;
        }
        
    }

    static void setup(){}

    static {
        HttpServer.addGlobalHandler( new EchoNonFork() );
        HttpServer.addGlobalHandler( new EchoFork() );
    }

}
