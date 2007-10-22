// JSObject.java

package ed.js;

import java.util.*;

public interface JSObject {

    // returns v
    public Object set( Object n , Object v );
    public Object get( Object n );

    public void setInt( int n , Object v );
    public Object getInt( int n );

    public Collection<String> keySet();

}
