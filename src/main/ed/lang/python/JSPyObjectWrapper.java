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
            System.out.println("FIXME: " + e.type);
        }

        if ( o == null )
            o = _p.__findattr__( n.toString() );

        if ( o == null )
            return super.get( n );

        return toJS( o );
    }
    
    public Object setInt( int n , Object v ){
        throw new RuntimeException( "not implemented" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "not implemented" );
    }
    
    public Object removeField( Object n ){
        throw new RuntimeException( "not implemented" );
    }
    
    public boolean containsKey( String s ){
        // TODO: make less awful
        return keySet().contains( s );
    }
    
    public Object call( Scope s , Object [] params ){
        return toJS( callPython( params ) );
    }

    public PyObject callPython( Object [] params ){
        PyObject [] pParams = new PyObject[params.length];
        for(int i = 0; i < params.length; ++i){
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
        else {
            for ( PyObject o : _p.asIterable() ){
                keys.add( o.toString() );
            }
        }
        
        return keys;
    }
    
    public JSFunction getConstructor(){
        throw new RuntimeException( "not implemented" );
    }

    public JSObject getSuper(){
        throw new RuntimeException( "not implemented" );
    }
    
    public String toString(){
        return _p.toString();
    }
    
    private PyObject _p;
}
    
