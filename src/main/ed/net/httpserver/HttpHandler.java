// HttpHandler.java

package ed.net.httpserver;

import ed.util.*;

public interface HttpHandler {

    public boolean handles( HttpRequest request , Info info );
    public void handle( HttpRequest request , HttpResponse response );

    /** Returns the priority of an HTTP request.  The smaller the number returned, the higher the priority.
     * @return This HTTP handler's priority number
     */
    public double priority();

    static class Info {

        public Info(){
            reset();
        }

        public void reset(){
            fork = false;
            admin = false;
        }

        public boolean fork;
        public boolean admin;
    }
}
