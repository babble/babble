// JSFunction.java

package ed.js;

import ed.js.engine.Scope;

public abstract class JSFunction extends JSFunctionBase {

    public JSFunction( int num ){
        this( null , num );
    }

    public JSFunction( String name , int num ){
        super( num );
        _name = name;
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

    public Scope getScope(){
        if ( _scope == null )
            _scope = new Scope( "temp score for : " + _name , Scope.GLOBAL );
        return _scope;
    }

    private Scope _scope = null;

    String _name = "NO NAME SET";
}
