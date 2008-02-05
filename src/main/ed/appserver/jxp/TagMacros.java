// TagMacros.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.util.*;

public class TagMacros {
    
    /**
     * @return null means no macros.  
               if there is a macro, the first elements is the open, the 2nd is the close
     */
    public static String[] getBlocks( String tag ){
        return _tags.get( tag );
    }
    
    
    private static Map<String,String[]> _tags = Collections.synchronizedMap( new StringMap<String[]>() );
    
    static {
        _tags.put( "if" , new String[]{ 
                " if ( $1 ){ \n " , "\n } \n"
            } );
    }
}
