// JSFunction.java

package ed.js;

import ed.js.engine.Scope;

public abstract class JSFunction extends JSFunctionBase {

    public JSFunction( int num ){
        this( null , null , num );
    }
    
    public JSFunction( Scope scope , String name , int num ){
        super( num );
        _scope = scope;
        _name = name;
        
        _prototype = new JSObject();
        set( "prototype" , _prototype );
    }

    public Object get( Object n ){
        Object foo = super.get( n );
        if ( foo != null ) 
            return foo;
        return _prototype.get( n );
    }

    public void setName( String name ){
        _name = name;
    }

    public String getName(){
        return _name;
    }
    
    public String toString(){
        return "JSFunction : " + _name;
    }

    protected final Scope _scope;
    protected final JSObject _prototype;

    String _name = "NO NAME SET";
}
