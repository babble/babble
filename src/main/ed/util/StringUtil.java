// StringUtil.java

package ed.util;

public final class StringUtil{

    public static int indexOf( String big , String small , boolean ignoreCase , int start ){
        for ( int i=start; i< ( big.length() - small.length() ) ; i++ )
            if ( big.regionMatches( ignoreCase , i , small , 0 , small.length() ) )
                return i;
        return -1;
    }

    public static int count( String big , String small ){
        int c = 0;
        
        int idx = 0;
        while ( ( idx = big.indexOf( small , idx ) ) >= 0 ){
            c++;
            idx += small.length();
        }
        return c;
    }

    public static final String replace( String str , String from , String to ){
        if ( from == null || from.length() == 0 )
            return str;
        
	StringBuffer buf = null;
	int idx;
	int start = 0;
	while ( ( idx = str.indexOf( from , start ) ) >= 0 ){
	    if ( buf == null )
		buf = new StringBuffer();
	    buf.append( str.substring( start , idx ) );
	    buf.append( to );
	    start = idx + from.length();
	}
	if ( buf == null )
	    return str;
	
	buf.append( str.substring( start ) );

	return buf.toString();
    }
}
