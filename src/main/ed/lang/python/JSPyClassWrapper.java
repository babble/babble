// JSPyClassWrapper.java

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
        return shell;
    }

    public JSFunction getFunction( String name , boolean tryLower ){
        PyObject foo = _p.__findattr__( name );
        if ( foo == null ) return null;
        return new JSPyObjectWrapper( foo , true );
    }
}

