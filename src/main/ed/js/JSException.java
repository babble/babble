// JSException.java

package ed.js;

public class JSException extends RuntimeException {

    public JSException( Object o ){
        this( o , null );
    }
    
    public JSException( Object o , Throwable t ){
        super( o.toString() , _fix( t ) );
        _object = o;
    }

    public Object getObject(){
        return _object;
    }
    
    final Object _object;

    static Throwable _fix( Throwable t ){
        if ( t instanceof java.lang.reflect.InvocationTargetException )
            return ((java.lang.reflect.InvocationTargetException)t).getTargetException();
        return t;
    }
}
