// DummyHttpHandler.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.net.httpserver;

import ed.util.*;

public class DummyHttpHandler {

    public static class Echo implements HttpHandler {

        public boolean handles( HttpRequest request , Info info ){
            info.fork = request.getBoolean( "fork" , false );
            return request.getURI().equals( "/~echo" );
        }

        public void handle( HttpRequest request , HttpResponse response ){
            JxpWriter w = response.getJxpWriter();
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
