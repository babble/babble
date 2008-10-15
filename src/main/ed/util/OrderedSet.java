// OrderedSet.java

package ed.util;

import java.util.*;

public class OrderedSet<T> extends AbstractSet<T> {
    public OrderedSet(){
        _list = new ArrayList<T>();
    }

    public boolean add(T t) {
        if ( _list.contains( t ) )
            return false;
        _list.add( t );
        return true;
    }

    public int size(){
        return _list.size();
    }

    public Iterator<T> iterator(){
        return _list.iterator();
    }
    
    final List<T> _list;
    
}
