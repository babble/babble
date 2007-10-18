// StringUtil.java

package ed.util;

public final class StringUtil{

    public static int count( String big , String small ){
        int c = 0;
        
        int idx = 0;
        while ( ( idx = big.indexOf( small , idx ) ) >= 0 ){
            c++;
            idx += small.length();
        }
        return c;
    }
}
