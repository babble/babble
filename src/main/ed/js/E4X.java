// E4X.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class E4X extends JSObjectBase {
    
    public static final JSFunctionCalls1 CONS = new JSFunctionCalls1(){
	    public JSObject newOne(){
		return new E4X();
	    }
	    
	    public Object call( Scope scope , Object str , Object [] args){	
		E4X e = (E4X)scope.getThis();
		e.init( str.toString() );
		return e;
	    }
	};
    
    public E4X(){}

    void init( String s ){
	_raw = s;
    }
    
    public Object get( Object n ){
	return new JSString( "asdasdasdas [" + n + "]" );
    }

    public String toString(){
	return _raw;
    }

    private String _raw;
}
