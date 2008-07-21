// HttpHandler.java

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
