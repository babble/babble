// MemTools.java


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
