// JSPyClassWrapper.java

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
import ed.js.engine.*;
import static ed.lang.python.Python.*;

public class JSPyClassWrapper extends JSPyObjectWrapper {
    public JSPyClassWrapper( PyObject o ){
        super( o );
    }

    public JSObject newOne(){
        JSPyObjectWrapper empty = JSPyObjectWrapper.newShell();
        return empty;
    }

    public Object call( Scope s , Object [] params ){
        Object t = s.getThis();
        if( t == null || ! ( t instanceof JSPyObjectWrapper ) ){
            throw new UnsupportedOperationException( "please call Python classes using new" );
        }

        JSPyObjectWrapper shell = (JSPyObjectWrapper)t;
        PyObject p = callPython(s, params, null);
        shell.setContained(p);
        return this;
    }

    public JSFunction getFunction( String name , boolean tryLower ){
        PyObject foo = _p.__findattr__( name );
        if ( foo == null ) return null;
        return new JSPyObjectWrapper( foo , true );
    }
}

