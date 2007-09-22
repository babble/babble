// Scope.java

package ed.js.engine;

import java.util.*;

import ed.js.*;

public class Scope {
    
    public static Scope GLOBAL = new Scope( "GLOBAL" , JSBuiltInFunctions._myScope  );
    static {
        GLOBAL._parentWritable = false;
    }
    
    public Scope( String name , Scope parent ){
        _name = name;
        _parent = parent;
    }

    public void put( String name , Object o , boolean local ){
        if ( local || _parent == null || ! _parentWritable || _objects.containsKey( name ) ){
            _objects.put( name , o );
            return;
        }
        
        _parent.put( name , o , false );
    }

    public Object get( String name ){
        Object foo = _objects.get( name );
        if ( foo != null )
            return foo;
        
        if ( _parent == null )
            return null;
        
        return _parent.get( name );
    }

    public JSFunction getFunction( String name ){
        Object o = get( name );
        if ( o == null )
            throw new RuntimeException( "no function named : " + name );
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
        return (JSFunction)o;
    }
    
    final String _name;
    final Scope _parent;

    boolean _parentWritable = true;

    final Map<String,Object> _objects = new HashMap<String,Object>();
}
