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

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


/**
 *  PyFile that writes to a thread-local stream.  Used for CGI
 *  since the Python 'state' appears to be a singleton and thus
 *  concurrent CGI requests can stomp on one another.
 */
@ExposedType(name = "_10gen_cgiout")
public class PythonCGIOutFile extends PyFile {

    protected static Charset _charset = Charset.forName("ISO-8859-1");
    protected CharsetEncoder encoder = _charset.newEncoder();

    protected static PyType TYPE = Python.exposeClass(PythonCGIOutFile.class);

    PythonCGIOutFile() {
        super(TYPE);
    }
    
    @ExposedMethod
    public void flush() {
        try {
            PythonCGIAdapter.CGIStreamHolder osw = PythonCGIAdapter.CGIStreamHolder.getThreadLocal();
            osw.getOut().flush();
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

        PythonCGIAdapter.CGIStreamHolder osw = PythonCGIAdapter.CGIStreamHolder.getThreadLocal();

        try {
            // decode to bytes - python seems to do iso-8859-1
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(s));
            osw.getOut().write(bbuf.array());

        } catch (CharacterCodingException e) {
                e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }    
    }

    public void write(String s) {
        _10gen_cgiout_write(s);
    }
}
