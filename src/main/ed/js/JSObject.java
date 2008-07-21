// JSObject.java

package ed.js;

import java.util.*;

/** @expose */
public interface JSObject {

    /** Sets a name/value pair in this object.
     * @jsset
     * @param n Name to set
     * @param v Corresponding value
     * @return <tt>v</tt>
     */
    public Object set( Object n , Object v );
    /** Gets a field from this object by a given name.
     * @jsget
     * @param n The name of the field fetch
     * @return The field, if found
     */
    public Object get( Object n );

    /** Add a key/value pair to this object, using a numeric key.
     * @param n Key to use.
     * @param v Value to use.
     * @param v
     */
    public Object setInt( int n , Object v );

    /** Get a value from this object whose key is numeric.
     * @param n Key for which to search.
     * @return The corresponding value.
     */
    public Object getInt( int n );

    /** Remove a field with a given name from this object.
     * @param n The name of the field to remove
     * @return The value removed from this object
     */
    public Object removeField( Object n );

    /** Checks if this object contains a field with the given name.
     * @param s Field name for which to check
     * @return if this object contains a field with the given name
     */
    public boolean containsKey( String s );

    /** Returns this object's fields' names
     * @jskeyset
     * @return The names of the fields in this object
     */
    public Collection<String> keySet();

    /** Finds the constructor for this object.
     * @return The constructor
     */
    public JSFunction getConstructor();

    /** Gets this object's superclass.
     * @return The superclass
     */
    public JSObject getSuper();

}
