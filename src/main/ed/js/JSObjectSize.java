// JSObjectSize.java

package ed.js;

import java.util.*;

import ed.db.*;

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
            return OBJ_OVERHEAD + (long)( o.toString().length() * 1.5 );

        if ( o instanceof JSFunction ){
            // TODO
            return 128;
        }

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

        if ( o.getClass() == JSObjectBase.class ){
            return ((JSObjectBase)o).approxSize();
        }

        if ( o instanceof Collection ){
            long temp = 0;
            temp += 32;
            for ( Object foo : (Collection)o )
                temp += size( foo );
            return temp;
        }

        if ( o instanceof JSFileChunk ){
            JSFileChunk chunk = (JSFileChunk)o;
            Object data = chunk.get( "data" );
            if ( data == null )
                return 32;
            
            return OBJ_OVERHEAD + ((JSBinaryData)data).length();
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
