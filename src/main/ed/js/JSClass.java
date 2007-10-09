// JSClass.java

package ed.js;

public class JSClass extends JSObject {
    
    public JSClass( JSFunction constructor ){
        _constructor = constructor;
    }

    public Object get( Object n ){
        Object foo = super.get( n );
        if ( foo != null )
            return foo;
        return _constructor._prototype.get( n );
    }

    final JSFunction _constructor;
}
