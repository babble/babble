// StringParseUtil.java

package ed.util;

public final class StringParseUtil {
    public static boolean parseBoolean( String s , boolean d ){

        if ( s == null )
            return d;
        
        s = s.trim();
        if ( s.length() == 0 )
            return d;
        
        char c = s.charAt( 0 );

        if ( c == 't' || c == 'T' ||
             c == 'y' || c == 'Y' )
            return true;

        if ( c == 'f' || c == 'F' ||
             c == 'n' || c == 'N' )
            return false;
        
        return d;
             
    }
}
   
