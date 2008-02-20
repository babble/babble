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
        return parseInt( s , def , null , true );
    }

    public static Number parseIntRadix( String s , int radix ){
        if ( s == null )
            return Double.NaN;
        
        s = s.trim();
        if ( s.length() == 0 )
            return Double.NaN;

        int firstDigit = -1;
        int i = 0;
        if ( s.charAt( 0 ) == '-' ) 
            i = 1;
        // Find first non-digit.
        for ( ; i<s.length(); i++ ){
            if ( Character.digit( s.charAt( i ) , radix ) == -1 )
                break;
        }
        
        try {
            return Integer.parseInt( s.substring( 0, i ) , radix );
        }
        catch (Exception e) {
            return Double.NaN;
        }
    }

    public static int parseInt( String s , int def , final int[] lastIdx , final boolean allowNegative ){
        final boolean useLastIdx = lastIdx != null && lastIdx.length > 0;
        if ( useLastIdx )
            lastIdx[0] = -1;
        
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
        
        if ( allowNegative && firstDigit > 0 && s.charAt( firstDigit - 1 ) == '-' )
            firstDigit--;
        
        if ( useLastIdx )
            lastIdx[0] = lastDigit;
        return Integer.parseInt( s.substring( firstDigit , lastDigit ) );
    }

    public static Number parseNumber( String s , Number def ){
        if ( s == null )
            return def;
        
        s = s.trim();
        if ( s.length() == 0)
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
        
        if ( lastDigit < s.length() && s.charAt( lastDigit ) == '.' ){
            lastDigit++;
            while ( lastDigit < s.length() && Character.isDigit( s.charAt( lastDigit ) ) )
                lastDigit++;    
            return Double.parseDouble( s.substring( firstDigit , lastDigit ) );
        }

        return Integer.parseInt( s.substring( firstDigit , lastDigit ) );


    }


}
   
