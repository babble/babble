// Python.java

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

import java.io.*;

import org.python.core.*;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

import static ed.lang.python.PythonSmallWrappers.*;

public class Python {

    static {
        PySystemState.initialize();
    }
    
    public static PyCode compile( File f )
        throws IOException {
        return (PyCode)(Py.compile( new FileInputStream( f ) , f.toString() , "exec" ));
    }


    static Object toJS( Object p ){

        if ( p == null || p instanceof PyNone )
            return null;
 
        if ( p instanceof JSObject ||
             p instanceof String || 
             p instanceof Number )
            return p;

        if ( p instanceof PyJSObjectWrapper )
            return ((PyJSObjectWrapper)p)._js;

        if ( p instanceof PyInteger )
            return ((PyInteger)p).getValue();
        
        if ( p instanceof PyFloat )
            return ((PyFloat)p).getValue();
        
        if ( p instanceof PyString )
            return p.toString();

        if ( p instanceof PyObjectId )
            return ((PyObjectId)p)._id;

        if ( p instanceof PyClass || p instanceof PyType ){
            return new JSPyClassWrapper( (PyObject)p );
        }

        // this needs to be last
        if ( p instanceof PyObject )
            return new JSPyObjectWrapper( (PyObject)p );

        throw new RuntimeException( "can't convert [" + p.getClass().getName() + "] from py to js" );       
    }
    
    static PyObject toPython( Object o ){
        
        if ( o == null )
            return Py.None;
        
        if ( o instanceof JSPyObjectWrapper )
            return ((JSPyObjectWrapper)o).getContained();

        if ( o instanceof PyObject )
            return (PyObject)o;
        
        if ( o instanceof Integer )
            return new PyInteger( ((Integer)o).intValue() );
        
        if ( o instanceof Number )
            return new PyFloat( ((Number)o).floatValue() );
        
        if ( o instanceof String ||
             o instanceof JSString )
            return new PyString( o.toString() );
        
        if ( o instanceof ed.db.ObjectId )
            return new PyObjectId( (ed.db.ObjectId)o );

        // FILL IN MORE HERE

        // these should be at the bottom
        if ( o instanceof JSFunction ){
            Object p = ((JSFunction)o).getPrototype();
            if( p instanceof JSObject ){
                JSObject jsp = (JSObject)p;
                if( ! jsp.keySet().isEmpty() )
                    return new PyJSClassWrapper( (JSFunction)o );
            }

            return new PyJSFunctionWrapper( (JSFunction)o );
        }
        if ( o instanceof JSObject )
            return new PyJSObjectWrapper( (JSObject)o );
        
        throw new RuntimeException( "can't convert [" + o.getClass().getName() + "] from js to py" );
    }
}
