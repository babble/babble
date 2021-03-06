// PyJSObjectWrapper.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.python;

import java.util.*;

import org.python.core.*;
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

public class PyJSObjectWrapper extends PyDictionary {

    public PyJSObjectWrapper( JSObject jsObject ){
        this( jsObject , true );
    }

    public PyJSObjectWrapper( PyType type , JSObject jsObject ){
        this( jsObject , false );
    }

    public PyJSObjectWrapper( JSObject jsObject , boolean returnPyNone ){
        super( );
        _js = jsObject;
        _returnPyNone = returnPyNone;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PyJSObjectWrapper for null" );
    }

    public PyObject __iter__(){
        return keys().__iter__();
    }

    public PyObject iterkeys(){
        // FIXME: return an actual iterator
        return keys().__iter__();
    }

    public PyObject iteritems(){
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

    /**
     * Note: our pickle support is very limited! We just pickle
     * wrappers into appropriate "plain" types --
     * PyJSObjectWrapper->dict, PyJSStringWrapper->str, etc. If you actually
     * need this to be smarter, let us know!
     */
    public PyObject __reduce_ex__(int version){
        PyObject arguments = new PyTuple();
        PyObject state = Py.None;
        PyObject iterator = iteritems();
        return new PyTuple( PyDictionary.TYPE , arguments , state , Py.None , iterator );
    }

    public boolean has_key(PyObject key){
        return __contains__( key );
    }

    public PyList keys(){
        PyList l = new PyList();
        for( String s : _js.keySet() ){
            l.append( Py.newString( s ) );
        }
        return l;
    }

    public PyList values(){
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

    public boolean __nonzero__(){
        return _js.keySet().size() > 0;
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
        // PyDictionary.__setitem__ doesn't call invoke("__setitem__")
        super.__setitem__(key, value);
        this.handleSet( toJS( key ) , toJS( value ) );
    }

    public void __setattr__( String key , PyObject value ){
        super.__setitem__(key, value);
        this.handleSet( toJS( key ) , toJS( value ) );
    }

    public boolean __contains__( PyObject key ){
        if( key instanceof PyString )
            return _js.containsKey( key.toString() );
        throw new RuntimeException( "js wrappers cannot contain objects of class " + key.getClass() );
    }

    public void handleSet( Object key , Object value ){
        _js.set( key , value );
    }

    private void _remove( String key ){
        // FIXME: check if the key exists and throw an error if not --
        // this is Python after all.
        try {
            remove( key );
        }
        catch( PyException e ){
            // Didn't have a cached Python value, no big deal.
        }
        _js.removeField( key.toString() );
    }

    public void __delitem__( PyObject key ){
        // Although we expose this, we don't use the customary
        // jswrapper___delitem__ method name, because Jython evidently calls
        // the PyObject.__delitem__ method which is intercepted by PyDictionary.
        // In order to intercept it from there, we intercept the __delitem__
        // method.

        // FIXME: Maybe be more rigorous about casting to String here?
        _remove( key.toString() );
    }

    public void __delattr__( String key ){
        _remove( key );
    }

    public String toString(){
        return _js.toString();
    }

    public void clear(){
        throw new RuntimeException("not implemented yet");
    }


    public PyDictionary copy(){
        PyDictionary d = new PyDictionary();
        for( String key : _js.keySet() ){
            d.__setitem__( key.intern() , toPython( _js.get( key ) ) );
        }
        return d;
    }

    public PyList items(){
        throw new RuntimeException("not implemented yet");
    }

    public PyList itervalues(){
        throw new RuntimeException("not implemented yet");
    }

    public void update(PyObject [] args , String [] keywords){
        // Seasoned copy of PyDictionary.updateCommon
        int nargs = args.length - keywords.length;
        if (nargs > 1) {
            throw PyBuiltinFunction.DefaultInfo.unexpectedCall(nargs, false, "update", 0, 1);
        }
        if (nargs == 1) {
            PyObject arg = args[0];
            if (arg.__findattr__("keys") != null) {
                merge(arg);
            } else {
                mergeFromSeq(arg);
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            _js.set( keywords[i] , toJS ( args[nargs + i] ) );
        }
    }

    // Following methods were taken from PyDictionary to support easier
    // implementation of update().
    // The only changes are s/dict___setitem__/__setitem__/g .

    /**
     * Merge another PyObject that supports keys() with this
     * dict.
     *
     * @param other a PyObject with a keys() method
     */
    private void merge(PyObject other) {
        // Seasoned copy of PyDictionary.merge
        if (other instanceof PyDictionary) {
            mergeFromKeys(other, ((PyDictionary)other).keys());
        } else if (other instanceof PyStringMap) {
            mergeFromKeys(other, ((PyStringMap)other).keys());
        } else {
            mergeFromKeys(other, other.invoke("keys"));
        }
    }

    /**
     * Merge another PyObject via its keys() method
     *
     * @param other a PyObject with a keys() method
     * @param keys the result of other's keys() method
     */
    private void mergeFromKeys(PyObject other, PyObject keys) {
        // Seasoned copy of PyDictionary.mergeFromKeys
        for (PyObject key : keys.asIterable()) {
            __setitem__(key, other.__getitem__(key));
        }
    }

    /**
     * Merge any iterable object producing iterable objects of length
     * 2 into this dict.
     *
     * @param other another PyObject
     */
    private void mergeFromSeq(PyObject other) {
        // Seasoned copy of PyDictionary.mergeFromSeq
        PyObject pairs = other.__iter__();
        PyObject pair;

        for (int i = 0; (pair = pairs.__iternext__()) != null; i++) {
            // FIXME: took out call to fastSequence because it's not accessible
            // from outside the Jython org.python.core package. Dumb!
            int n;
            if ((n = pair.__len__()) != 2) {
                throw Py.ValueError(String.format("dictionary update sequence element #%d "
                                                  + "has length %d; 2 is required", i, n));
            }
            // This was the only change (dict___setitem__ -> __setitem__)
            __setitem__(pair.__getitem__(0), pair.__getitem__(1));
        }
    }

    public PyObject get( PyObject key ){
        return get( key , Py.None );
    }

    public PyObject get( PyObject key , PyObject default_object ){
        if( key instanceof PyString ){
            String jkey = key.toString();
            if( _js.containsKey( jkey , true ) )
                return toPython( _js.get( jkey ) );
        }
        return default_object;
    }

    public PyObject setdefault( PyObject key ){
        return setdefault( key , Py.None );
    }

    public PyObject setdefault( PyObject key , PyObject default_object ){
        throw new RuntimeException("not implemented");
    }

    public PyObject pop( PyObject key ){
        return pop( key , Py.None );
    }

    public PyObject pop( PyObject key , PyObject default_object ){
        throw new RuntimeException("not implemented");
    }

    public PyObject popitem(){
        throw new RuntimeException("not implemented");
    }

    final JSObject _js;
    final boolean _returnPyNone;

}
