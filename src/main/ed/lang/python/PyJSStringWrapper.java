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
