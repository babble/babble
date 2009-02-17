// PyDBCursor.java

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

import org.python.core.*;
import org.python.expose.*;

import ed.js.*;
import ed.lang.python.*;
import static ed.lang.python.Python.*;
import ed.db.*;

@ExposedType(name = "_10gen_dbcursor")
public class PyDBCursor extends PyJSObjectWrapper {
    static PyType TYPE = Python.exposeClass( PyDBCursor.class );

    PyDBCursor( DBCursor cursor ){
        super( TYPE , cursor );
        _cursor = cursor;
    }

    @ExposedMethod(names = {"next"})
    public PyObject __iternext__(){
        if( _cursor.hasNext() )
            return toPython( _cursor.next() );
        //throw Py.StopIteration("");
        return null; // Really??
    }

    @ExposedMethod
    public PyObject __iter__(){
        return this;
    }

    final DBCursor _cursor;
}
