// JSRegex.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class JSRegex extends JSObjectBase {

    static JSFunction _cons = new JSFunctionCalls0(){

            public Object call( Scope s , Object[] args ){
                throw new RuntimeException( "can't be here" );
            }

            protected void init(){

            }
        };
    
    public JSRegex( String p , String f ){
        super( _cons );
        _p = p;
        _f = f;
    }
    
    public String toString(){
        return "/" + _p + "/" + _f;
    }
    
    public int hashCode(){
        return _p.hashCode() + _f.hashCode();
    }

    public boolean equals( Object o ){
        return toString().equals( o.toString() );
    }

    final String _p;
    final String _f;
}
