// PyJSStringWrapper.java

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
import ed.util.*;
import static ed.lang.python.Python.*;


public class PyJSStringWrapper extends PyUnicode {

    final static public PyType TYPE = PyType.fromClass( PyJSStringWrapper.class );

    public PyJSStringWrapper( JSString s ){
        super( TYPE , s.toString() );
        _js = s;
        _p = new PyJSObjectWrapper( s );
    }

    public PyObject iterkeys(){ return keys().__iter__(); }
    public PyObject iteritems(){ return _p.items().__iter__(); }

    public PyList keys(){
        PyList l = new PyList();
        for( String s : _js.keySet() ){
            if( ! StringUtil.isDigits( s ) ){
                l.append( Py.newString( s ) );
            }
        }
        return l;
    }

    public PyList values(){
        PyList l = new PyList();
        for( String s : _js.keySet() ){
            if( ! StringUtil.isDigits( s ) ){
                l.append( toPython( _js.get( s ) ) );
            }
        }
        return l;
    }

    public PyObject __reduce_ex__(int version){
        return new PyTuple( PyUnicode.TYPE, new PyTuple( Py.newUnicode(toString()) ) );
    }

    public boolean has_key(PyObject key){ return _p.has_key(key); }

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
        if( key instanceof PyInteger || key instanceof PyLong )
            super.__setitem__( key , value ); // throws exception
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
        // Python string attributes are still read-only
        _p.__delattr__(key);
    }

    public boolean equals( Object other ){
        // FIXME: ????
        if( other instanceof String ){
            return super.equals( new PyString( (String)other ) );
        }
        return super.equals( other );
    }

    PyJSObjectWrapper _p;
    JSString _js;
}
