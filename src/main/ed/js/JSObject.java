// JSObject.java

package ed.js;

import java.util.*;

public interface JSObject {

    // returns v
    public Object set( Object n , Object v );
    public Object get( Object n );

    public Object setInt( int n , Object v );
    public Object getInt( int n );
    
    /**
     * @return old value
     */
    public Object removeField( Object n );

    public boolean containsKey( String s );
    public Collection<String> keySet();

    public JSFunction getConstructor();
    public JSObject getSuper();

}
