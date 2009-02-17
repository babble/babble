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

import org.python.expose.ExposedType;
import org.python.expose.ExposedMethod;
import org.python.core.PyFile;
import org.python.core.PyType;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyString;
import org.python.core.Py;

import java.io.IOException;

/**
 *  PyFile that writes to a thread-local stream.  Used for CGI
 *  since the Python 'state' appears to be a singleton and thus
 *  concurrent CGI requests can stomp on one another.
 */
@ExposedType(name = "_10gen_cgiout")
public class PythonCGIOutFile extends PyFile {

    protected static PyType TYPE = Python.exposeClass(PythonCGIOutFile.class);

    PythonCGIOutFile() {
        super(TYPE);
    }
    
    @ExposedMethod
    public void _10gen_cgiout_write(PyObject o) {

        if (o instanceof PyUnicode) {
            _10gen_cgiout_write(o.__str__().toString());
        } else if (o instanceof PyString) {
            _10gen_cgiout_write(o.toString());
        } else {
            throw Py.TypeError("write requires a string as its argument");
        }
    }

    final public void _10gen_cgiout_write(String s) {

        try {
            PythonCGIAdapter.CGITLSData.getThreadLocal().getOutputStream().write(s.getBytes("ISO-8859-1"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ExposedMethod
    public void flush() {

        try {
            PythonCGIAdapter.CGITLSData.getThreadLocal().getOutputStream().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }    

    public void write(String s) {
        _10gen_cgiout_write(s);
    }

    @ExposedMethod(names = {"__str__", "__repr__"})
    public String toString() {
        return "<open file '_10gen.apprequest', mode 'w'>";
    }

    public Object __tojava__(Class cls) {
        return this;
    }

}
