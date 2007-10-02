// Scope.java

package ed.js.engine;

import java.util.*;

import ed.js.*;

public class Scope {
    
    public static Scope GLOBAL = new Scope( "GLOBAL" , JSBuiltInFunctions._myScope  );
    static {
        GLOBAL._locked = true;
        GLOBAL._global = true;
    }

    static class _NULL {
        
    }
    static _NULL NULL = new _NULL();
    
    public Scope( String name , Scope parent ){
        _name = name;
        _parent = parent;
    }

    public Scope child(){
        return new Scope( _name + ".child" , this );
    }

    public void put( String name , Object o , boolean local ){
        
        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( local
             || _parent == null
             || _parent._locked 
             || _objects.containsKey( name ) 
             || _global
             ){
            
            if ( o == null )
                o = NULL;
            _objects.put( name , o );
            return;
        }
        
        
        _parent.put( name , o , false );
    }
    
    public Object get( String name ){
        Object foo = _objects.get( name );
        if ( foo != null ){
            if ( foo == NULL )
                return null;
            return foo;
        }
        
        if ( _parent == null )
            return null;
        
        return _parent.get( name );
    }

    public JSFunction getFunction( String name ){
        Object o = get( name );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
        return (JSFunction)o;
    }
    
    final String _name;
    final Scope _parent;

    boolean _locked = false;
    boolean _global = false;

    final Map<String,Object> _objects = new HashMap<String,Object>();
}
