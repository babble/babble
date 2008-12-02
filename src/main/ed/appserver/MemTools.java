// MemTools.java


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

package ed.appserver;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;

import ed.net.httpserver.*;

public class MemTools {

    static void leakHunt( AppContext ac , AppRequest request , SeenPath reachableBefore , long sizeBefore ){

        if ( ac._numRequests < 2 )
            return;
        
        final Logger logger = ac.getLogger( "leak" ).getChild( request.getRequest().getFullURL() );

        if ( logger._simpleGet( "__seen" ) == null ){
            // only want to do this for pages we've seen before
            logger.set( "__seen" , "abc" );
            return;
        }

        SeenPath now = new SeenPath( true );
        long sizeNow = ac.approxSize( now );
        
        if ( sizeNow <= sizeBefore && now.size() <= reachableBefore.size() )
            return;
        
        if ( now.contains( request ) ){
            logger.error( "now contains the request" );
            debugPath( logger , now , ac , request );
        }

        gotMemoryLeak( ac , logger ,
                       reachableBefore , now , 
                       sizeBefore , sizeNow );
        
    }
    

    
    static void gotMemoryLeak( AppContext context , Logger logger , 
                                      SeenPath before , SeenPath after ,
                                      long sizeBefore , long sizeAfter
                                      ){
        
        
        IdentitySet newThings = new IdentitySet( after.keySet() );
        newThings.removeAll( before.keySet() );

        logger.error( "mem leak detected.  " + before.size() + "->" + after.size() + " (" + ( after.size() - before.size() ) + ") (" + newThings.size() + ")"  );

        for ( Object o : newThings ){

            if ( o == null )
                continue;
            
            System.out.println( "\t" + o.getClass() );
            System.out.println( "\t\t" + o );

            ObjectPath path = debugPath( logger , after , context , o );
            if ( path == null )
                break;
            
        }
        

    }

    
    static ObjectPath debugPath( Logger logger , SeenPath seen , Object from , Object to ){
        ObjectPath path = new ObjectPath();
        path.add( to );

        ObjectPath result = seen.path( from , to , path );
        if ( result == null ){
            System.out.println( "couldn't find path : " + ObjectPath.pathElementsToString( seen.get( to ) ) );
            path.debug();
            return null;
        }        
        
        logger.error( "found leak at : " + path );        
        return null;
    }
    
}
