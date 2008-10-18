// PingHandler.java

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

public class PingHandler implements HttpHandler {

    static final String DATA = "abc123";

    public boolean handles( HttpRequest request , Info info ){
	if ( ! request.getURI().equals( "/~ping" ) )
	    return false;
	
	info.fork = request.getBoolean( "fork" , false );

	return true;
    }
    
    public boolean handle( HttpRequest request , HttpResponse response ){
	int num = request.getInt( "num" , 1 );
	JxpWriter out = response.getJxpWriter();
	for ( int i=0; i<num; i++ )
	    out.print( "abc123" );
	return true;
    }

    public double priority(){
	return Double.MIN_VALUE;
    }

}
