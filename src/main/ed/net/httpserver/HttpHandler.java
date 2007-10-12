// HttpHandler.java

package ed.net.httpserver;

public interface HttpHandler {

    public boolean handles( HttpRequest request );
    public void handle( HttpRequest request , HttpResponse response );
    public boolean fork();

}
