// JSObjectWrapper.java

package ed.js;

/** @expose */
public class JSObjectWrapper implements JSObject {
    /** Initializes a new wrapper for an object
     * @param wrap The object to wrap
     */
    public JSObjectWrapper( JSObject wrap ){
        _wrap = wrap;
    }

    /** Sets a name/value pair in this object.
     * @param n Name to set
     * @param v Corresponding value
     * @return <tt>v</tt>
     */
    public Object set( Object n , Object v ){
        return _wrap.set( n , v );
    }

    /** Gets a field from this object by a given name.
     * @param n The name of the field fetch
     * @return The field, if found
     */
    public Object get( Object n ){
        return _wrap.get( n );
    }

    /** Add a key/value pair to this object, using a numeric key.
     * @param n Key to use.
     * @param v Value to use.
     * @param v
     */
    public Object setInt( int n , Object v ){
        return _wrap.setInt( n , v );
    }

    /** Get a value from this object whose key is numeric.
     * @param n Key for which to search.
     * @return The corresponding value.
     */
    public Object getInt( int n ){
        return _wrap.getInt( n );
    }

    /** Remove a field with a given name from this object.
     * @param n The name of the field to remove
     * @return The value removed from this object
     */
    public Object removeField( Object n ){
        return _wrap.removeField( n );
    }

    /** Checks if this object contains a field with the given name.
     * @param s Field name for which to check
     * @return if this object contains a field with the given name
     */
    public boolean containsKey( String s ){
        return _wrap.containsKey( s );
    }

    /** Returns this object's fields' names
     * @return The names of the fields in this object
     */
    public java.util.Collection<String> keySet(){
        return _wrap.keySet();
    }

    /** Finds the constructor for this object.
     * @return The constructor
     */
    public JSFunction getConstructor(){
        return null;
    }

    /** Gets this object's superclass.
     * @return The superclass
     */
    public JSObject getSuper(){
        return _wrap.getSuper();
    }

    /** @unexpose */
    final JSObject _wrap;
}
