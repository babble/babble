// HttpHandler.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.net.httpserver;

import ed.util.*;

public interface HttpHandler {

    /**
     * @return true if this Handler wants to handle this request
     */
    public boolean handles( HttpRequest request , Info info );
    
    /**
     * @return return true this request is done.  
              return false if this request is still being dealt with and the threading will be handled by someone else
     */
    public boolean handle( HttpRequest request , HttpResponse response );

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
            doneAfterHandles = true;
        }

        public boolean fork;
        public boolean doneAfterHandles; // only relevant if fork is false  
        public boolean admin;


    }
}
