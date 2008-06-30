// JSObjectSize.java

package ed.js;

import java.util.*;

import ed.db.*;

public class JSObjectSize {

    public static final long OBJ_OVERHEAD = 8;
    
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
        
        String blah = o.getClass().toString();
        if ( ! _seenClasses.contains( blah ) ){
            System.err.println( "can't size : " + blah );
            _seenClasses.add( blah );
        }
        
        return OBJ_OVERHEAD * 4;
    }

    private static Set<String> _seenClasses = Collections.synchronizedSet( new HashSet<String>() );
    
}
