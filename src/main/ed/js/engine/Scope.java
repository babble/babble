// Scope.java

package ed.js.engine;

import java.util.*;

class Scope {
    Scope( String name , Scope parent ){
        _name = name;
        _parent = parent;
    }

    void put( String name , Object o ){
        _objects.put( name , o );
    }

    Object get( String name ){
        Object foo = _objects.get( name );
        if ( foo != null )
            return foo;
        
        if ( _parent == null )
            return null;
        
        return _parent.get( name );
    }

    final String _name;
    final Scope _parent;

    final Map<String,Object> _objects = new HashMap<String,Object>();
}
