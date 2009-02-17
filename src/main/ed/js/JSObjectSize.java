// JSObjectSize.java

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

package ed.js;

import java.lang.ref.*;
import java.util.*;

import ed.db.*;
import ed.util.*;
import ed.js.engine.*;

import ed.lang.python.Python;
import org.python.core.PyObject;

/** @expose
 */
public class JSObjectSize {

    /** The extra space every object takes up: 8 */
    public static final long OBJ_OVERHEAD = 8;
    public static final long STRING_CONS_OVERHEAD = 127552;

    /** Finds the size of a given object.
     * @param o The object
     * @return The object's size, in bytes
     */
    public static long size( Object o ){
        return size( o , null , null );
    }
    
    public static long size( Object o , SeenPath seen , Object from ){
        if ( o == null )
            return 0;

        final long size = _size( o , seen , from );
        return size;
    }

    private static long _size( Object o , SeenPath seen , Object from ){
        if ( o == null ||
             o instanceof Boolean ||
             o instanceof Short ||
             o instanceof Character || 
             o instanceof String ) {
            return 0;
        }

        if ( o instanceof DBRef
             || o instanceof JSDate
             || o instanceof Number
             || o instanceof JSDate
             || o instanceof java.io.File
             || o instanceof ed.log.Level
             || o instanceof ObjectId )
            return OBJ_OVERHEAD + 8;
        
        // -------- this is the end of the "primitive" types ------

        if ( seen == null )
            seen = new SeenPath();
        else if ( ! seen.shouldVisit( o , from ) )
            return 0;

        if ( o instanceof JSString ) {
            return 10 * o.toString().length() + 
                ( seen.isFirstString() ? STRING_CONS_OVERHEAD : 0 );
        }

        // --------- special section for WeakReferences and other special thigns
        
        if ( o instanceof WeakReference )
            return OBJ_OVERHEAD;

        if ( o instanceof WeakHashMap )
            return OBJ_OVERHEAD + ( ((WeakHashMap)o).size() * OBJ_OVERHEAD );
        

        // --------  objects we know about --------
        
        if ( o instanceof JSFileChunk ){
            JSFileChunk chunk = (JSFileChunk)o;
            Object data = chunk.get( "data" );
            if ( data == null )
                return 32;
            
            return OBJ_OVERHEAD + ((JSBinaryData)data).length();
        }
        
        if ( o instanceof PyObject ){
            return Python.size( (PyObject)o , seen );
        }

        if ( o instanceof Scope ){
            return ((Scope)o).approxSize( seen );
        }

        if ( o instanceof JSObjectBase ) 
            return ((JSObjectBase)o).approxSize( seen );

        if ( o instanceof Sizable )
            return ((Sizable)o).approxSize( seen );

        if ( o instanceof Collection ){
	    Collection c = (Collection)o;
	    Iterator i = c.iterator();
            long temp = 0;
            temp += o instanceof HashSet ? 184 : 80;

	    if ( i == null )
		return temp;
	    
	    while ( i.hasNext() ){
                Object foo = i.next();
                if ( foo == null )
                    continue;
                
                if ( seen.contains( foo ) )
                    continue;
                
                temp += _size( foo , seen , o );
            }
            return temp;
        }

        if ( o instanceof Map ){
            Map m = (Map)o;
            long temp = 32;
            
            for ( Map.Entry e : (Set<Map.Entry>)m.entrySet() )
                temp += 32 + _size( e.getKey() , seen , o ) + _size( e.getValue() , seen , o );

            return temp;
        }

        String blah = o.getClass().toString();
        if ( ! _seenClasses.contains( blah ) ){
            System.err.println( "can't size : " + blah );
            _seenClasses.add( blah );
        }

        return OBJ_OVERHEAD * 4;
    }

    private static Set<String> _seenClasses = Collections.synchronizedSet( new HashSet<String>() );

}
