// CollectionEnumeration.java

package ed.util;

import java.util.*;

public class CollectionEnumeration<E> implements Enumeration<E> {

    public CollectionEnumeration( Collection<E> c ){
        _it = c.iterator();
    }

    public boolean hasMoreElements(){
        return _it.hasNext();
    }

    public E nextElement(){
        return _it.next();
    }

    private final Iterator<E> _it;
}
