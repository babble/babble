// StringMap.java

package ed.util;

import java.util.*;

public class StringMap<T> extends CustomHashMap<String,T>{

    public StringMap(){
        super();
    }

    public StringMap( int initialCap ) {
        super( initialCap );
    }

    public StringMap( int initialCap , float loadFactor ) {
        super( initialCap , loadFactor );
    }

    public StringMap( Map<String,? extends T> m ) {
        super( m );
    }

    public final int doHash( Object s ) {
        return s instanceof String ? Hash.lowerCaseHash( (String)s ) : 0;
    }

    public final boolean doEquals( Object a , Object b ) {
        return ( ! ( a instanceof String ) || ! ( b instanceof String ) ) ? false : ((String)a).equalsIgnoreCase( (String)b );
    }
}
