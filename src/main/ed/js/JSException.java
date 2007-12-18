// JSException.java

package ed.js;

public class JSException extends RuntimeException {

    public JSException( Object o ){
        this( o , null );
    }
    
    public JSException( Object o , Throwable t ){
        super( o.toString() , t );
        _object = o;
    }

    public Object getObject(){
        return _object;
    }
    
    final Object _object;
}
