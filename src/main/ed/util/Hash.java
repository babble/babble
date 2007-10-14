// Hash.java

package ed.util;

public final class Hash {

    public static final int hashBackward( String s ) {
        int hash = 0;
        for ( int i = s.length()-1; i >= 0; i-- )
            hash = hash * 31 + s.charAt( i );
        return hash;
    }

    public static final long hashBackwardLong( String s ) {
        long hash = 0;
        for ( int i = s.length()-1; i >= 0; i-- )
            hash = hash * 63 + s.charAt( i );
        return hash;
    }

    static final long _longHashConstant = 4095;

    /**
     * 64-bit hash, using longs, in stead of ints, for less collisions, for when it matters.
     * Calls longHash( s , 0 , s.length() )
     * @param s         The String to hash.
     */
    public static final long longHash( String s ) {
        return longHash( s , 0 , s.length() );
    }

    /**
     * 64-bit hash using longs, starting on index 'start' and including everything before 'end'.
     * @param s         The string to hash.
     * @param start     Where to start the hash.
     * @param end       Where to end the hash.
     */
    public static final long longHash( String s , int start , int end ) {
        long hash = 0;
        for ( ; start < end; start++ )
            hash = _longHashConstant * hash + s.charAt( start );
        return hash;
    }

    /**
     * Same as longHash(String), using only lower-case values of letters.
     * Calls longhash( s , 0 , s.length() ).
     * @param s     The string to Hash.
     */
    public static final long longLowerHash( String s ) {
        return longLowerHash( s , 0 , s.length() );
    }

    /**
     * Long (64-bit) hash, lower-cased, from [start-end)
     * @param s      The string to hash.
     * @param start  where to start hashing.
     * @param end    Where to stop hashing.
     */
    public static final long longLowerHash( String s , int start , int end ) {
        long hash = 0;
        for ( ; start < end; start++ )
            hash = _longHashConstant * hash + Character.toLowerCase( s.charAt( start ) );
        return hash;
    }

    /**
     * Long (64-bit) hash, lower-cased, from [start-end)
     * @param s      The string to hash.
     * @param start  where to start hashing.
     * @param end    Where to stop hashing.
     */
    public static final long longLowerHash( String s , int start , int end , long hash ) {
        for ( ; start < end; start++ )
            hash = _longHashConstant * hash + Character.toLowerCase( s.charAt( start ) );
        return hash;
    }

    public static final long longLowerHashAppend( long hash , char c ) {
        return hash * _longHashConstant + Character.toLowerCase( c );
    }

    public static final long longHashAppend( long hash , char c ) {
        return hash * _longHashConstant + c;
    }
    
    /**
     * This is an exact copy of the String <code>hashCode()</code> function, aside from the lowercasing.
     */
    public static final int lowerCaseHash( String s ) {
	int h = 0;
        final int len = s.length();
        for ( int i = 0; i < len; i++ )
            h = 31*h + Character.toLowerCase( s.charAt( i ) );
        return h;
    }

    public static final int lowerCaseHash( String s , int start , int end ) {
	int h = 0;
        final int len = s.length();
        for ( int i = start; i < len && i < end; i++ )
            h = 31*h + Character.toLowerCase( s.charAt( i ) );
        return h;
    }

    public static final int hashCode( CharSequence s , int start , int end ) {
	int h = 0;
        final int len = s.length();
        for ( int i = start; i < len && i < end; i++ )
            h = 31*h + s.charAt( i );
        return h;
    }

    public static final int nospaceLowerHash( String s , int start , int end ) {
	int h = 0;
        final int len = s.length();
        for ( int i = start; i < len && i < end; i++ ) {
            char c = s.charAt( i );
            if ( Character.isWhitespace( c ) )
                continue;
            h = 31*h + Character.toLowerCase( c );
        }
        return h;
    }

    /**
     * This is an exact copy of the String <code>hashCode()</code> function, aside from the lowercasing.
     * No, it's not.  It also ignores consecutive whitespace.
     */
    public static final int lowerCaseSpaceTrimHash( String s ) {
	int h = 0;
        int len = s.length();
        while ( len > 1 && Character.isWhitespace( s.charAt( len-1 ) ) )
            len--;
        boolean lastWasSpace = true;
        for ( int i = 0; i < len; i++ ) {
            boolean isSpace = Character.isWhitespace( s.charAt( i ) );
            if ( isSpace && lastWasSpace )
                continue;
            lastWasSpace = isSpace;
            h = 31*h + Character.toLowerCase( s.charAt( i ) );
        }
        return h;
    }

    public static final int lowerCaseSpaceTrimHash( String s , int start , int end ) {
	int h = 0;
        int len = s.length();
        while ( len > 1 && Character.isWhitespace( s.charAt( len-1 ) ) )
            len--;
        boolean lastWasSpace = true;
        for ( int i = start; i < len && i < end; i++ ) {
            boolean isSpace = Character.isWhitespace( s.charAt( i ) );
            if ( isSpace && lastWasSpace )
                continue;
            lastWasSpace = isSpace;
            h = 31*h + Character.toLowerCase( s.charAt( i ) );
        }
        return h;
    }

    /**
     * Calculate the hashcode for a series of strings combined as one.
     * @param strings     Varargs array of Strings.
     * @return            A hashcode.
     */
    public static final int hashCode( String ... strings ) {
	int h = 0;
        for ( String s : strings ) {
            int len = s.length();
            for ( int i = 0; i < len; i++ )
                h = 31*h + s.charAt( i );
        }
        return h;
    }

}
