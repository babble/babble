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
    
    public Object removeField( Object n ){
        return _wrap.removeField( n );
    }

    public boolean containsKey( String s ){
        return _wrap.containsKey( s );
    }

    public java.util.Collection<String> keySet(){
        return _wrap.keySet();
    }

    public JSFunction getConstructor(){
        return null;
    }

    public JSObject getSuper(){
        return _wrap.getSuper();
    }

    final JSObject _wrap;
}
