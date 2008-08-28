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

    static {
        try {
            ExposedTypeProcessor etp = new ExposedTypeProcessor(PyJSObjectWrapper.class.getClassLoader()
                                                                .getResourceAsStream("ed/lang/python/PyJSObjectWrapper.class"));
            TypeBuilder t = etp.getTypeExposer().makeBuilder();
            PyType.addBuilder(PyJSObjectWrapper.class, t);
            PyType js = PyType.fromClass(PyJSObjectWrapper.class);
            PyObject dict = t.getDict(js);
        }
        catch(java.io.IOException e){
            throw new RuntimeException("Couldn't expose PyJSObjectWrapper as Python type");
        }
    }
    
    public PyJSObjectWrapper( JSObject jsObject ){
        this( jsObject , true );
    }

    public PyJSObjectWrapper( JSObject jsObject , boolean returnPyNone ){
        super( );
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
        return jswrapper_keys();
    }
    
    public PyObject iteritems(){
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
        Object o = _js.get( toJS( key ) );
        // Explicitly return null here rather than converting to None
        // This isn't findattr, after all; this is the check used to 
        // see if a dict contains a value.
        if( o == null )
            return null;

        return _fixReturn( o );
    }

    private PyObject _fixReturn( Object o ){
        if ( o == null && ! _returnPyNone )
            return null;
        
        return toPython( o , _js );
    }

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

    public void __delattr__( String key ){
        try {
            super.__delitem__( key );
        }
        catch( PyException e ){
            // meh
        }
        _js.removeField( key );
    }

    @ExposedMethod(names = {"__repr__", "__str__"})
    public String toString(){
        return _js.toString();
    }

    final JSObject _js;
    final boolean _returnPyNone;

}
