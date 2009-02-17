// DummyHttpHandler.java

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

public class DummyHttpHandler {

    public static class Echo implements HttpHandler {

        public boolean handles( HttpRequest request , Info info ){
            info.fork = request.getBoolean( "fork" , false );
            return request.getURI().equals( "/~echo" );
        }

        public boolean handle( HttpRequest request , HttpResponse response ){
            JxpWriter w = response.getJxpWriter();
            int copies = request.getInt( "copies" , 1 );
            for ( int i=0; i<copies; i++ )
                w.print( "abcdefghijklmnopqrstuvwxyv0123456789\n" );
            return true;
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
