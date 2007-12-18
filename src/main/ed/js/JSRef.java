// JSRef.java

package ed.js;

import ed.js.engine.*;

public class JSRef {

    public JSRef( Scope scope , JSObject object , Object key ){
        _scope = scope;
        _object = object;
        _key = key;
    }

    public Object get(){
        if ( _object != null )
            return _object.get( _key );
        return _scope.get( _key.toString() );
    }

    public void set( Object o ){
        if ( _object != null )
            _object.set( _key , o );
        else
            _scope.put( _key.toString() , o , false );
    }

    public String toString(){
        return "[[JSRef Scope: " + ( _scope != null ) + " Object : " + ( _object != null ) + " Key : " + _key + " ]]";
    }

    private final Scope _scope;
    private final JSObject _object;
    private final Object _key;
    
}
