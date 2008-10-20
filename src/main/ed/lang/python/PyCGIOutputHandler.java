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

import org.python.expose.ExposedType;
import org.python.expose.ExposedMethod;
import org.python.core.PyFile;
import org.python.core.PyType;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyString;
import org.python.core.Py;
import ed.appserver.AppRequest;
import ed.log.Logger;


/**
 *  Handler for CGI output in python.  This is a copy
 *  and modification of the similar class in
 *  SiteSystemState - Ethan muttered something about
 *  inheritance problems.
 */
@ExposedType(name="_10gen_pycgi_stdout")
public class PyCGIOutputHandler extends PyFile {

    int _count = 0;
    boolean _inHeaders = true;
    final Logger _log;

    static PyType TYPE = Python.exposeClass(PyCGIOutputHandler.class);

    PyCGIOutputHandler(Logger l){
        super( TYPE );
        _log = l;
    }

    @ExposedMethod
    public void _10gen_pycgi_stdout_write( PyObject o ){
        if ( o instanceof PyUnicode){
            _10gen_pycgi_stdout_write(o.__str__().toString());
        }
        else if ( o instanceof PyString){
            _10gen_pycgi_stdout_write(o.toString());
        }
        else {
            throw Py.TypeError("write requires a string as its argument");
        }
    }

    final public void _10gen_pycgi_stdout_write( String s ){

        AppRequest request = AppRequest.getThreadLocal();

        if (_inHeaders) {
            boolean newline = "\n".equals(s);

            if (newline) {
                _inHeaders = (++_count != 2);
            }
            else {
                _count = 0;
                int i = s.indexOf(":");
                request.getResponse().setHeader(s.substring(0, i), s.substring(i+1));
            }
        }
        else {
            if( request == null )
                // Log
                _log.info( s );
            else{
                request.print( s );
            }
        }
    }

    public void write( String s ){
        _10gen_pycgi_stdout_write( s );
    }

    @ExposedMethod
    public void flush(){
    }    
}