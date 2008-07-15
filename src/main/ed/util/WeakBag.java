// WeakBag.java

package ed.util;

import java.lang.ref.*;
import java.util.*;

/**
 * if its not obvious what a weak bag should do, then, well...
 * very very not thead safe
 * @expose
 */
public class WeakBag<T> {

    /** Initializes a new weak bag. */
    public WeakBag(){
    }

    /** Adds an element to the bag.
     * @param t Element to add
     */
    public void add( T t ){
        _set.add( new MyRef( t ) );
    }

    /** Returns the size of the bag.
     * @return the size of the bag
     */
    public int size(){
        clean();
        return _set.size();
    }

    /** Removes all object from the bag. */
    public void clear(){
        _set.clear();
    }

    /** Removes any null objects from the bag. */
    public void clean(){
        for ( Iterator<MyRef> i = _set.iterator(); i.hasNext(); ){
            MyRef ref = i.next();
            if ( ref.get() == null )
                i.remove();
        }
    }

    class MyRef extends WeakReference<T> {
        MyRef( T t ){
            super( t );
        }
    }

    /** @unexpose */
    private final List<MyRef> _set = new ArrayList<MyRef>();
}

