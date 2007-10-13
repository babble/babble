// HttpHandler.java

package ed.net.httpserver;

import ed.util.*;

public interface HttpHandler {

    public boolean handles( HttpRequest request , Box<Boolean> fork );
    public void handle( HttpRequest request , HttpResponse response );
    /**
     * @larger means later
     */
    public double priority();


}
