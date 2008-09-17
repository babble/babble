// JSObjectSize.java

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

package ed.js;

import java.util.*;

import ed.db.*;
import ed.util.*;
import ed.js.engine.*;

/** @expose
 */
public class JSObjectSize {

    /** The extra space every object takes up: 8 */
    public static final long OBJ_OVERHEAD = 8;

    /** Finds the size of a given object.
     * @param o The object
     * @return The object's size, in bytes
     */
    public static long size( Object o ){
        return size( o , null );
    }

    
    public static long size( Object o , IdentitySet seen ){
        if ( o == null )
            return 0;
        
        if ( o instanceof Boolean ||
             o instanceof Short ||
             o instanceof Character )
            return OBJ_OVERHEAD + 4;

        if ( o instanceof DBRef
             || o instanceof JSDate
             || o instanceof Number
             || o instanceof JSDate
             || o instanceof ObjectId )
            return OBJ_OVERHEAD + 16;
        
        if ( o instanceof String ||
             o instanceof JSString )
            return OBJ_OVERHEAD + (long)( o.toString().length() * 2 );
        
        if ( o instanceof DBCollection ){
            // TODO
            return 128;
        }

        if ( o instanceof DBBase ){
            // TODO
            return 128;
        }
        
        if ( o instanceof JSRegex ){
            // this is a total guess
            // TODO: make it a litlte more realistic
            return o.toString().length() * 4;
        }

        if ( o instanceof JSFileChunk ){
            JSFileChunk chunk = (JSFileChunk)o;
            Object data = chunk.get( "data" );
            if ( data == null )
                return 32;
            
            return OBJ_OVERHEAD + ((JSBinaryData)data).length();
        }
        
        // -------- this is the end of the "primitive" types ------

        if ( seen == null )
            seen = new IdentitySet();
        else if ( seen.contains( o ) )
            return 0;
        
        seen.add( o );
        
        if ( o instanceof Scope ){
            return ((Scope)o).approxSize( seen );
        }

        if ( o instanceof Collection ){
            long temp = 0;
            temp += 32;
            for ( Object foo : (Collection)o ){
                if ( seen.contains( foo ) )
                    continue;
                temp += size( foo );
            }
            return temp;
        }

        if ( o instanceof JSObjectBase )
            return ((JSObjectBase)o).approxSize( seen );


        String blah = o.getClass().toString();
        if ( ! _seenClasses.contains( blah ) ){
            System.err.println( "can't size : " + blah );
            _seenClasses.add( blah );
        }

        return OBJ_OVERHEAD * 4;
    }

    private static Set<String> _seenClasses = Collections.synchronizedSet( new HashSet<String>() );

}
