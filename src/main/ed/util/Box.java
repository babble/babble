// Box.java

package ed.util;

/**
 *  Simple wrapper class to make arguments mutable
 * @expose
 */
public class Box<T> {
    /** Initializes a new wrapper for an object.
     * @param t Object to wrap
     */
    public Box( T t ){
        _t = t;
    }

    /** Sets this object to a new value.
     * @param t New value for the object.
     */
    public void set( T t ){
        _t = t;
    }

    /** Returns the object.
     * @return The object
     */
    public T get(){
        return _t;
    }

    private T _t;
}
