// PyJSArrayWrapper.java

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
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "jsarraywrapper")
public class PyJSArrayWrapper extends PySequence {

    static {
        try {
            ExposedTypeProcessor etp = new ExposedTypeProcessor(PyJSArrayWrapper.class.getClassLoader()
                                                                .getResourceAsStream("ed/lang/python/PyJSArrayWrapper.class"));
            TypeBuilder t = etp.getTypeExposer().makeBuilder();
            PyType.addBuilder(PyJSArrayWrapper.class, t);
            PyType js = PyType.fromClass(PyJSArrayWrapper.class);
            PyObject dict = t.getDict(js);
        }
        catch(java.io.IOException e){
            throw new RuntimeException("Couldn't expose PyJSArrayWrapper as Python type");
        }
    }

    public PyJSArrayWrapper( JSArray jsA ){
        super( );
        _js = jsA;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PyJSObjectWrapper for null" );
    }

    protected PyObject pyget(int index){
        return toPython(_js.getInt(index));
    }

    protected PyObject getslice(int start, int stop, int step){
        throw new RuntimeException("not implemented yet");
    }

    protected PyObject repeat(int count){
        throw new RuntimeException("not implemented yet");
    }

    protected void set(int index, PyObject value){
        _js.setInt(index, toJS(value));
    }

    protected void setslice(int start, int stop, int step, PyObject value){
        throw new RuntimeException("not implemented yet");
    }

    protected void del(int i){
        throw new RuntimeException("not implemented yet");
    }

    protected void delRange(int start, int stop, int step){
        throw new RuntimeException("not implemented yet");
    }

    public int __len__(){
        Object foo = _js.get("length");
        if(foo instanceof Integer){
            return ((Integer)foo).intValue();
        }
        return 0; //??
    }

    @ExposedMethod(names={"__str__", "__repr__"})
    public PyObject jsarraywrapper___repr__(){
        return new PyString(toString());
    }

    public String toString(){
        StringBuilder foo = new StringBuilder();
        int n = __len__();
        int count = 0;
        foo.append("[");
        for(int i = 0; i < n-1; ++i){
            PyObject p = toPython(_js.getInt(i));
            PyObject repr = p.__repr__();
            foo.append(repr.toString());
            foo.append(", ");
        }
        PyObject p = toPython(_js.getInt(n-1));
        PyObject repr = p.__repr__();
        foo.append(repr.toString());
        foo.append("]");
        return foo.toString();
    }

    @ExposedMethod()
    public PyObject jsarraywrapper_append(PyObject value){
        _js.add(toJS(value));
        return Py.None;
    }

    @ExposedMethod
    public PyObject jsarraywrapper_count(PyObject value){
        Object jval = toJS(value);
        int n = __len__();
        int count = 0;
        for(int i = 0; i < n; ++i){
            if(_js.getInt(i).equals(jval))
                count++;
        }
        return Py.newInteger(count);
    }

    // eq, ne, lt, le, gt, ge, cmp
    // finditem, setitem, getslice, delslice -- handled for us?
    final JSArray _js;
}
