// PyDBCursor.java

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
