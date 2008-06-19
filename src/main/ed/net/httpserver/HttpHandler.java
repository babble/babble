// HttpHandler.java

package ed.net.httpserver;

import ed.util.*;

public interface HttpHandler {

    public boolean handles( HttpRequest request , Info info );
    public void handle( HttpRequest request , HttpResponse response );
    /**
     * @larger means later
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
