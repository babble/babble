// Hash.java

package ed.util;

public final class Hash {

    public static final long longHash( String s ) {
        return longHash( s , 0 , s.length() );
    }

    public static final long longHash( String s , int start , int end ) {
        long hash = 0;
        for ( ; start < end; start++ )
            hash = 123 * hash + s.charAt( start );
        return hash;
    }


}
