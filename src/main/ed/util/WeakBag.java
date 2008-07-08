// WeakBag.java

package ed.util;

import java.lang.ref.*;
import java.util.*;

/**
   if its not obvious what a weak bag should do, then, well...
   
   very very not thead safe
 */
public class WeakBag<T> {

    public WeakBag(){
    }
    
    public void add( T t ){
        _set.add( new MyRef( t ) );
    }

    public int size(){
        clean();
        return _set.size();
    }

    public void clear(){
        _set.clear();
    }
    
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
    
    private final List<MyRef> _set = new ArrayList<MyRef>();
}

