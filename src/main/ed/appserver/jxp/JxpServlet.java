// JxpServlet.java

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

package ed.appserver.jxp;

import java.io.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.lang.*;

import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpServlet {
    
    public static final boolean NOCDN = Config.get().getBoolean( "NO-CDN" );

    public JxpServlet( AppContext context , JSFunction func ){
        _context = context;
        _theFunction = func;
    }

    public void handle( HttpRequest request , HttpResponse response , AppRequest ar ){
        
        final Scope scope = ar.getScope();

        if ( scope.get( "request" ) == null )
            scope.put( "request" , request , true );
        if ( scope.get( "response" ) == null )
            scope.put( "response" , response , true );

        
        Object cdnFromScope = scope.get( "CDN" );
             
        ServletWriter writer = ar.getServletWriter();
        scope.put( "print" , writer  , true );
        
        try {
            _theFunction.call( scope , request , response , writer );
            
            if ( writer._writer.hasSpot() ){
                writer._writer.backToSpot();
                
                if ( ar.getContext() != null )
                    for ( Object foo : ar.getContext().getGlobalHead() ) {
                        writer.print( foo.toString() );
                        writer.print( "\n" );
                    }
                
                if ( ar != null )
                    for ( Object foo : ar.getHeadToPrint() ) {
                        writer.print( foo.toString() );
                        writer.print( "\n" );
                    }
                writer._writer.backToEnd();
            }
            else {
                if ( ( ar.getContext() != null && ar.getContext().getGlobalHead().size() > 0 ) || 
                     ( ar != null && ar.getHeadToPrint().size() > 0  ) ){
		    // this is interesting
		    // maybe i should do it only for html files
		    // so i have to know that
                    //throw new RuntimeException( "have head to print but no </head>" );
		}
            }
        }
        catch ( RuntimeException re ){
            if ( re instanceof JSException ){
                if ( re.getCause() != null && re.getCause() instanceof RuntimeException )
                    re = (RuntimeException)re.getCause();
            }
            StackTraceHolder.getInstance().fix( re );
            throw re;
        }
        
    }
   
    final AppContext _context;
    final JSFunction _theFunction;
}
