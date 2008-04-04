// JSObjectLame.java

package ed.js;

import java.util.*;

public class JSObjectLame implements JSObject {

    public Object get( Object n ){
        throw new UnsupportedOperationException();
    }

    public Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }

    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }

    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }

    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }

    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }

    public void removeField( Object n ){
        throw new UnsupportedOperationException();
    }

    public JSFunction getConstructor(){
        return null;
    }

}

