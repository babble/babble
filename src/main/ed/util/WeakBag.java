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
        _set.add( new WeakReference<T>( t ) );
    }

    public int size(){
        clean();
        return _set.size();
    }

    public void clean(){
        for ( Iterator<WeakReference<T>> i = _set.iterator(); i.hasNext(); ){
            WeakReference<T> ref = i.next();
            if ( ref.get() == null )
                i.remove();
        }
    }

    private final List<WeakReference<T>> _set = new ArrayList<WeakReference<T>>();
}

