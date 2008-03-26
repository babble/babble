// Box.java

package ed.util;

/**
 *  Simple wrapper class to make arguments mutable
 */
public class Box<T> {
    public Box( T t ){
        _t = t;
    }

    public void set( T t ){
        _t = t;
    }

    public T get(){
        return _t;
    }

    private T _t;
}
