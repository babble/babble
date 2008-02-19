// JSObjectWrapper.java

package ed.js;

public class JSObjectWrapper implements JSObject {
    
    public JSObjectWrapper( JSObject wrap ){
        _wrap = wrap;
    }
    
    public Object set( Object n , Object v ){
        return _wrap.set( n , v );
    }
    public Object get( Object n ){
        return _wrap.get( n );
    }

    public Object setInt( int n , Object v ){
        return _wrap.setInt( n , v );
    }
    public Object getInt( int n ){
        return _wrap.getInt( n );
    }
    
    public void removeField( Object n ){
        _wrap.removeField( n );
    }

    public java.util.Collection<String> keySet(){
        return _wrap.keySet();
    }

    final JSObject _wrap;
}
