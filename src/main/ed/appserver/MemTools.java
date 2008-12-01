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

public class MemTools {
    
    public static void gotMemoryLeak( AppContext context , Logger logger , 
                                      SeenPath before , SeenPath after ,
                                      long sizeBefore , long sizeAfter
                                      ){
        
        
        logger.error( "mem leak detected.  " + before.size() + "->" + after.size() + " (" + ( after.size() - before.size() ) + ")"  );
        
        IdentitySet newThings = new IdentitySet( after.keySet() );
        newThings.removeAll( before.keySet() );

        for ( Object o : newThings ){

            if ( o == null )
                continue;
            
            System.out.println( "\t" + o.getClass() );
            List path = after.path( context , o );
            
            System.out.println( "\t\t" + path );
        }
        

    }
    
}
