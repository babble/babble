// StringParseUtil.java

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

package ed.util;

/** @expose */
public final class StringParseUtil {

    /** Turns a string into a boolean value and returns a default value if unsuccessful.
     * @param s the string to convert
     * @param d the default value
     * @return equivalent boolean value
     */
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

    /** Turns a string into an int and returns a default value if unsuccessful.
     * @param s the string to convert
     * @param d the default value
     * @return the int value
     */
    public static int parseInt( String s , int def ){
        return parseInt( s , def , null , true );
    }

    /** Turns a string into an int using a given radix.
     * @param s the string to convert
     * @param radix radix to use
     * @return the int value
     */
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
            // Remember: all numbers in JS are 64-bit
            return Long.parseLong( s.substring( 0, i ) , radix );
        }
        catch (Exception e) {
            return Double.NaN;
        }
    }

    /** Turns a string into an int and returns a default value if unsuccessful.
     * @param s the string to convert
     * @param d the default value
     * @param lastIdx sets lastIdx[0] to the index of the last digit
     * @param allowNegative if negative numbers are valid
     * @return the int value
     */
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

    /** Turns a string into a Number and returns a default value if unsuccessful.
     * @param s the string to convert
     * @param d the default value
     * @return the numeric value
     */
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
        
        boolean isDouble = false;

        if ( firstDigit > 0 && s.charAt( firstDigit - 1 ) == '.' ){
            firstDigit--;        
            isDouble = true;
        }
        
        if ( firstDigit > 0 && s.charAt( firstDigit - 1 ) == '-' )
            firstDigit--;

        if ( lastDigit < s.length() && s.charAt( lastDigit ) == '.' ){
            lastDigit++;
            while ( lastDigit < s.length() && Character.isDigit( s.charAt( lastDigit ) ) )
                lastDigit++;
            
            isDouble = true;
        }

        if ( lastDigit < s.length() && s.charAt( lastDigit ) == 'E' ){
            lastDigit++;
            while ( lastDigit < s.length() && Character.isDigit( s.charAt( lastDigit ) ) )
                lastDigit++;
            
            isDouble = true;
        }
	

	final String actual = s.substring( firstDigit , lastDigit );

        if ( isDouble || actual.length() > 17  )
            return Double.parseDouble( actual );


	if ( actual.length() > 10 )
	    return Long.parseLong(  actual );

        return Integer.parseInt( actual );
    }

    /** Use Java's "strict parsing" methods Integer.parseInt and  Double.parseDouble to parse s "strictly". i.e. if it's neither a double or an integer, fail.
     * @param s the string to convert
     * @return the numeric value
     */
    public static Number parseStrict( String s ){
        if( s.matches( "-?Infinity" ) ) {
            if( s.startsWith( "-" ) ) {
                return Double.NEGATIVE_INFINITY;
            }
            else {
                return Double.POSITIVE_INFINITY;
            }
        }
        else if( s.indexOf('.') != -1 )
            return Double.parseDouble(s);
        else if( s.length() > 2 && s.charAt( 0 ) == '0' && s.charAt( 1 ) == 'x')
            return Integer.parseInt( s.substring( 2, s.length() ) , 16 );

        int e = s.indexOf( 'e' );
        if( e > 0 ) {
            double num = Double.parseDouble( s.substring( 0, e ) );
            int exp = Integer.parseInt( s.substring( e + 1 ) );
            return num * Math.pow( 10 , exp );
        }
        return Integer.parseInt(s);
    }

}
