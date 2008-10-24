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

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;


@ExposedType(name = "_10gen_cgiout")
public class PythonCGIOutFile extends PyFile {

    protected static PyType TYPE = Python.exposeClass(PythonCGIOutFile.class);
    protected OutputStreamWriter _osWriter;

    PythonCGIOutFile(OutputStream outStream) {
        super(TYPE);
        _osWriter = new OutputStreamWriter(outStream);
    }

    @ExposedMethod
    public void flush() {
        try {
            _osWriter.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

    @ExposedMethod(names = {"__str__", "__repr__"})
    public String toString() {
        return "<open file '_10gen.apprequest', mode 'w'>";
    }

    public Object __tojava__(Class cls) {
        return this;
    }

    final public void _10gen_cgiout_write(String s) {
        try {
            _osWriter.write(s);
            _osWriter.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String s) {
        _10gen_cgiout_write(s);
    }
}