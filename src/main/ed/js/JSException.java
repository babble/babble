// JSException.java

package ed.js;

public class JSException extends RuntimeException {
    public JSException( Object o ){
        super( o.toString() );
        _object = o;
    }

    public Object getObject(){
        return _object;
    }
    
    final Object _object;
}
