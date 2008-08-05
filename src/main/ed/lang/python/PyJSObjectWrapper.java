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

import org.python.core.*;
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "jswrapper")
public class PyJSObjectWrapper extends PyDictionary {

    @ExposedMethod
    public final PyList jswrapper_keys(){
        PyList l = new PyList();
        for( String s : _js.keySet() ){
            l.append( Py.newString( s ) );
        }
        return l;
    }

    public int __len__(){
        return _js.keySet().size();
    }

    @ExposedMethod
    public final boolean jswrapper___nonzero__(){
        return _js.keySet().size() > 0;
    }

    public boolean __nonzero__(){
        return jswrapper___nonzero__();
    }

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
    
    public PyObject __findattr__(String name) {

        if ( D ) System.out.println( "__findattr__ on [" + name + "]" );

        // FIXME: more graceful fail-through etc
        try{
            PyObject p = super.__findattr__( name );
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

    public PyObject __dir__(){
        PyList list = new PyList();
        for( String s : _js.keySet() ){
            list.append( Py.newString( s ) );
        }
        return list;
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
