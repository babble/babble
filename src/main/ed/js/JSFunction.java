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
        
        _prototype = new JSObjectBase();
        set( "prototype" , _prototype );

        init();
    }

    public JSObject newOne(){
        return new JSObjectBase( this );
    }

    protected void init(){}

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

    public Scope getScope(){
        return _scope;
    }
    
    public String toString(){
        return "JSFunction : " + _name;
    }

    protected final Scope _scope;
    protected final JSObject _prototype;

    protected String _name = "NO NAME SET";
}
