// JSObjectLame.java

package ed.js;

import java.util.*;

/** @expose */
public class JSObjectLame implements JSObject {

    /** @unexpose */
    public Object get( Object n ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object removeField( Object n ){
        throw new UnsupportedOperationException();
    }

    /** Finds the constructor for this object.
     * @return null
     */
    public JSFunction getConstructor(){
        return null;
    }

    /** Gets this object's superclass.
     * @return null
     */
    public JSObject getSuper(){
        return null;
    }
}

