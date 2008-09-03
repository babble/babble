// PyJSStringWrapper.java

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

@ExposedType(name = "jsstringwrapper")
public class PyJSStringWrapper extends PyString {

    static {
        try {
            ExposedTypeProcessor etp = new ExposedTypeProcessor(PyJSStringWrapper.class.getClassLoader()
                                                                .getResourceAsStream("ed/lang/python/PyJSStringWrapper.class"));
            TypeBuilder t = etp.getTypeExposer().makeBuilder();
            PyType.addBuilder(PyJSStringWrapper.class, t);
            PyType js = PyType.fromClass(PyJSStringWrapper.class);
            PyObject dict = t.getDict(js);
        }
        catch(java.io.IOException e){
            throw new RuntimeException("Couldn't expose PyJSStringWrapper as Python type");
        }
    }

    public PyJSStringWrapper( JSString s ){
        super( s.toString() );
        _p = new PyJSObjectWrapper( s );
    }

    public PyObject iterkeys(){ return _p.iterkeys(); }
    @ExposedMethod
    public PyObject jsstringwrapper_iterkeys(){ return _p.iterkeys(); }
    public PyObject iteritems(){ return _p.iteritems(); }
    @ExposedMethod
    public PyObject jsstringwrapper_iteritems(){ return _p.iteritems(); }

    public PyList keys(){ return _p.keys(); }
    public PyList values(){ return _p.values(); }

    @ExposedMethod
    public final PyList jsstringwrapper_keys(){ return _p.keys(); }

    @ExposedMethod
    public final PyList jsstringwrapper_values(){ return _p.values(); }

    @ExposedMethod
    public boolean jsstringwrapper_has_key(PyObject key){ return _p.jswrapper_has_key(key); }

    public PyObject __dir__(){ return _p.__dir__(); }

    public PyObject __findattr_ex__(String name){ 
        PyObject result = super.__findattr_ex__( name );
        if( result != null ) return result;
        return _p.__findattr_ex__(name);
    }

    public PyObject __finditem__(PyObject key){
        PyObject result = super.__finditem__( key );
        if( result != null ) return result;
        return _p.__finditem__(key);
    }

    public void __setitem__(PyObject key, PyObject value){
        _p.__setitem__(key, value);
    }

    public void __setattr__( String key, PyObject value){
        // Python string attributes are read-only, so I don't have to worry here
        _p.__setattr__(key, value);
    }

    public boolean __contains__( PyObject key ){
        return super.__contains__( key ) || _p.__contains__( key );
    }

    public void __delattr__( String key ){
        _p.__delattr__(key);
    }

    PyJSObjectWrapper _p;
}
