// JSObject.java

package ed.js;

import java.util.*;

public interface JSObject {

    // returns v
    public Object set( Object n , Object v );
    public Object get( Object n );

    public Object setInt( int n , Object v );
    public Object getInt( int n );
    
    public void removeField( Object n );

    public boolean containsKey( String s );
    public Collection<String> keySet();
    
}
