// PYJSObjectWrapper.java

package ed.lang.python;

import org.python.core.*;

import ed.js.*;
import static ed.lang.python.Python.*;


public class PYJSObjectWrapper extends PyObject {
    
    public PYJSObjectWrapper( JSObject jsObject ){
        _js = jsObject;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PYJSObjectWrapper for null" );
    }
    
    public PyObject __findattr__(String name) {
        return toPython( _js.get( name ) );
    }    

    public PyObject __finditem__(PyObject key){
        return toPython( _js.get( toJS( key ) ) );
    }

    public void __setitem__(PyObject key, PyObject value) {
        _js.set( toJS( key ) , toJS( value ) );
    }

    public String toString(){
        return _js.toString();
    }

    final JSObject _js;
}
