// PyJSObjectWrapper.java

/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.lang.python;

import java.util.*;

import org.python.core.*;
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "jswrapper")
public class PyJSObjectWrapper extends PyDictionary {

    static PyType TYPE = Python.exposeClass(PyJSObjectWrapper.class);

    public PyJSObjectWrapper( JSObject jsObject ){
        this( jsObject , true );
    }

    public PyJSObjectWrapper( JSObject jsObject , boolean returnPyNone ){
        super( TYPE );
        _js = jsObject;
        _returnPyNone = returnPyNone;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PyJSObjectWrapper for null" );
    }

    public PyObject iterkeys(){
        return jswrapper_iterkeys();
    }

    @ExposedMethod
    public PyObject jswrapper_iterkeys(){
        // FIXME: return an actual iterator
        return jswrapper_keys();
    }

    public PyObject iteritems(){
        // FIXME: an actual iterator
        return jswrapper_iteritems();
    }

    @ExposedMethod
    public PyObject jswrapper_iteritems(){
        final Iterator<String> keys = _js.keySet().iterator();
        return new PyIterator(){
            public PyObject __iternext__(){

                if ( ! keys.hasNext() )
                    return null;

                String key = keys.next();

                return new PyTuple( Py.newString( key ) , toPython( _js.get( key ) ) );
            }
        };
    }

    public PyList keys(){
        return jswrapper_keys();
    }

    public PyList values(){
        return jswrapper_values();
    }

    @ExposedMethod
    public boolean jswrapper_has_key(PyObject key){
        return jswrapper___contains__( key );
    }

    @ExposedMethod
    public final PyList jswrapper_keys(){
        PyList l = new PyList();
        for( String s : _js.keySet() ){
            l.append( Py.newString( s ) );
        }
        return l;
    }

    @ExposedMethod
    public PyList jswrapper_values(){
        PyList list = new PyList();
        for( String key : _js.keySet() ){
            list.append( toPython( _js.get( key ) ) );
        }
        return list;
    }

    public int __len__(){
        return _js.keySet().size();
    }

    public PyObject __dir__(){
        PyList list = new PyList();
        for( String s : _js.keySet() ){
            list.append( Py.newString( s ) );
        }
        return list;
    }

    @ExposedMethod
    public final boolean jswrapper___nonzero__(){
        return _js.keySet().size() > 0;
    }

    public boolean __nonzero__(){
        return jswrapper___nonzero__();
    }

    public PyObject __findattr_ex__(String name) {

        if ( D ) System.out.println( "__findattr__ on [" + name + "]" );

        // FIXME: more graceful fail-through etc
        try{
            PyObject p = super.__findattr_ex__( name );
            if( p != null )
                return p;
        }
        catch(PyException e){
        }

        Object res = _js.get( name );
        if ( res == null )
            res = NativeBridge.getNativeFunc( _js , name );

        return _fixReturn( res );
    }

    public PyObject __finditem__(PyObject key){

        if ( D ) System.out.println( "__finditem__ on [" + key + "]" );

        // FIXME: more graceful fail-through etc
        PyObject p = super.__finditem__(key);
        if( p != null )
            return p;

        Object jkey = toJS( key );
        String skey = jkey.toString();

        Object o = _js.get( jkey );
        if( o == null && _js.containsKey( skey , true ) ){
            return Py.None;
        }

        // We tried to find and got a null -- maybe this means it's not
        // contained in the object?
        // (Or maybe it is contained and is set to null and we couldn't
        // find out, because containsKey throws an exception or something.)
        // We better "fail-fast", since we're in Python land.
        // Returning null here means "we don't have it", and may raise
        // an exception.
        if( o == null )
            return null;

        return _fixReturn( o );
    }

    private PyObject _fixReturn( Object o ){
        if ( o == null && ! _returnPyNone )
            return null;

        return toPython( o , _js );
    }

    // FIXME: why is this being unwrapped twice?
    // (i.e. once here, once in handleSet)
    // When I take it out, I get a bunch of test failures. Look into
    public void __setitem__(PyObject key, PyObject value) {
        super.__setitem__(key, value);
        this.handleSet( toJS( key ) , toJS( value ) );
    }

    public void __setattr__( String key , PyObject value ){
        super.__setitem__(key, value);
        this.handleSet( toJS( key ) , toJS( value ) );
    }

    public boolean __contains__( PyObject key ){
        return jswrapper___contains__( key );
    }

    @ExposedMethod
    public boolean jswrapper___contains__( PyObject key ){
        if( key instanceof PyString )
            return _js.containsKey( key.toString() );
        throw new RuntimeException( "js wrappers cannot contain objects of class " + key.getClass() );
    }

    public void handleSet( Object key , Object value ){
        _js.set( toJS( key ) , toJS( value ) );
    }

    public void remove( String key ){
        // FIXME: check if the key exists and throw an error if not --
        // this is Python after all.
        try {
            super.remove( key );
        }
        catch( PyException e ){
            // Didn't have a cached Python value, no big deal.
        }
        _js.removeField( key.toString() );
    }

    @ExposedMethod
    public void __delitem__( PyObject key ){
        // Although we expose this, we don't use the customary
        // jswrapper___delitem__ method name, because Jython evidently calls
        // the PyObject.__delitem__ method which is intercepted by PyDictionary.
        // In order to intercept it from there, we intercept the __delitem__
        // method.

        // FIXME: Maybe be more rigorous about casting to String here?
        remove( key.toString() );
    }

    public void __delattr__( String key ){
        remove( key );
    }

    @ExposedMethod(names = {"__repr__", "__str__"})
    public String toString(){
        return _js.toString();
    }

    final JSObject _js;
    final boolean _returnPyNone;

}
