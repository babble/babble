// PyJSObjectWrapper.java

package ed.lang.python;

import org.python.core.*;

import ed.js.*;
import static ed.lang.python.Python.*;


public class PyJSObjectWrapper extends PyObject {
    
    public PyJSObjectWrapper( JSObject jsObject ){
        this( jsObject , true );
    }

    public PyJSObjectWrapper( JSObject jsObject , boolean returnPyNone ){
        _js = jsObject;
        _returnPyNone = returnPyNone;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PyJSObjectWrapper for null" );
    }
    
    public PyObject __findattr__(String name) {
        return _fixReturn( _js.get( name ) );
    }    

    public PyObject __finditem__(PyObject key){
        return _fixReturn( _js.get( toJS( key ) ) );
    }
    
    private PyObject _fixReturn( Object o ){
        if ( o == null && ! _returnPyNone )
            return null;
        
        return toPython( o );
    }

    public void __setitem__(PyObject key, PyObject value) {
        _js.set( toJS( key ) , toJS( value ) );
    }

    public String toString(){
        return _js.toString();
    }

    final JSObject _js;
    final boolean _returnPyNone;
}
