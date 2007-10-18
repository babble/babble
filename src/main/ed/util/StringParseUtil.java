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

    public static int parseInt( String s , int def ){
        if ( s == null )
            return def;
        
        s = s.trim();
        if ( s.length() == 0 )
            return def;

        int firstDigit = -1;
        for ( int i=0; i<s.length(); i++ ){
            if ( Character.isDigit( s.charAt( i ) ) ){
                firstDigit = i;
                break;
            }
        }

        if ( firstDigit < 0 )
            return def;
        
        int lastDigit = firstDigit + 1;
        while ( lastDigit < s.length() && Character.isDigit( s.charAt( lastDigit ) ) )
            lastDigit++;
        
        if ( firstDigit > 0 && s.charAt( firstDigit - 1 ) == '-' )
            firstDigit--;
        
        return Integer.parseInt( s.substring( firstDigit , lastDigit ) );
    }
}
   
