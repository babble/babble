// JSPyObjectWrapper.java

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

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

public class JSPyObjectWrapper extends JSFunctionCalls0 {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.JSPYOBJECTWRAPPER" );

    private JSPyObjectWrapper( ){
        _p = null;
    }

    public JSPyObjectWrapper( PyObject o ){
        _p = o;
        if ( _p == null )
            throw new NullPointerException( "don't think you should create a JSPyObjectWrapper for null" );
    }

    public static JSPyObjectWrapper newShell( ){
        return new JSPyObjectWrapper();
    }

    public void setContained( PyObject p ){
        // Be careful with this
        _p = p;
    }

    public PyObject getContained( ){
        return _p;
    }
    
    public boolean isCallable(){
        // All python objects are *potentially* callable, so we subclass
        // JSFunction.
        // This function checks whether the wrapped object is callable.
        return __builtin__.callable( _p );
    }

    public Object set( Object n , Object v ){
        if( _p == null && n.equals( "prototype" ) ){
            if( DEBUG )
                System.err.println("I'm not set up yet! Ignoring set to " + n);
            return v;
        }
        _p.__setitem__( toPython( n ) , toPython( v ) );
        return v;
    }

    public Object get( Object n ){
        PyObject p = toPython( n );

        Object o = null;
        try {
            o = _p.__finditem__( p );
        } catch(PyException e){
            if ( D ) System.out.println("JSPyObjectWrapper.get FIXME: " + e.type);
        }

        if ( o == null )
            o = _p.__findattr__( n.toString() );

        if ( o == null )
            return super.get( n );

        return toJS( o );
    }
    
    public Object setInt( int n , Object v ){
        _p.__setitem__( toPython( n ) , toPython( v ) );
        return v;
    }

    public Object getInt( int n ){
        return toJS( _p.__getitem__( toPython ( n ) ) );
    }
    
    public Object removeField( Object n ){
        super.removeField( n );
        _p.__delattr__( n.toString() );
        return null; // FIXME: we removed both of them, who cares
    }
    
    public boolean containsKey( String s ){
        // TODO: make less awful
        return keySet().contains( s );
    }
    
    public Object call( Scope s , Object [] params ){
        return toJS( callPython( params ) );
    }

    public PyObject callPython( Object [] params ){
        PyObject [] pParams = new PyObject[ params == null ? 0 : params.length];
        for(int i = 0; i < pParams.length; ++i){
            pParams[i] = toPython(params[i]);
        }
        return _p.__call__( pParams , new String[0] );
    }

    public Collection<String> keySet( boolean includePrototype ){
        
        List<String> keys = new ArrayList<String>();
    
        if ( _p instanceof PyDictionary ){
            for ( Object o : ((PyDictionary)_p).keySet() )
                keys.add( o.toString() );
        }
        else try {
                for ( PyObject o : _p.asIterable() ){
                    keys.add( o.toString() );
                }
                return keys;
            }
            catch( PyException e ){
                PyObject dict = _p.getDict();
                
                if( dict == null ){
                    // try dir()??
                    return keys;
                }

                if( dict instanceof PyStringMap ){
                    for ( Object o : ((PyStringMap)dict).keys() ){
                        keys.add( o.toString() );
                    }
                }
                else {
                    PyObject pykeys;
                    pykeys = dict.invoke( "keys" );
                    if( pykeys instanceof PySequenceList ){
                        for( Object o : ((PySequenceList)pykeys) )
                            keys.add( o.toString() );
                    }
                    else
                        throw new RuntimeException("can't figure out how to get keyset for " + dict.getClass() );
                }
            }
        
        return keys;
    }
    
    public JSFunction getConstructor(){
        throw new RuntimeException( "not implemented" );
    }

    public JSObject getSuper(){
        // FIXME: This is probably wrong; since we treat all Python objects as
        // functions, why shouldn't we return JSFunction._prototype?
        return JSObjectBase._objectLowFunctions;
    }
    
    public String toString(){
        return _p.toString();
    }
    
    protected PyObject _p;
}
    
