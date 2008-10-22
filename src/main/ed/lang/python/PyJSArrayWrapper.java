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

import ed.js.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "jsarraywrapper")
public class PyJSArrayWrapper extends PyList {

    public static final PyType TYPE = Python.exposeClass(PyJSArrayWrapper.class);

    public PyJSArrayWrapper( JSArray jsA ){
        super( TYPE );
        _js = jsA;
        if ( _js == null )
            throw new NullPointerException( "don't think you should create a PyJSObjectWrapper for null" );
    }

    public PyObject pyget(int index){
        return toPython(_js.getInt(index));
    }

    protected PyObject getslice(int start, int stop, int step){
        PyList pyl = new PyList();
        int i = start;
        while( i < stop ){
            pyl.append( toPython( _js.getInt( i ) ) );
            i += step;
        }
        return pyl;
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
        _js.remove( i );
    }

    protected void delRange(int start, int stop, int step){
        throw new RuntimeException("not implemented yet");
    }

    public int __len__(){
        return _js.size();
    }

    @ExposedMethod
    public PyObject jsarraywrapper___iter__(){
        return new PySequenceIter( this );
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
        if( n >= 1 ){
            PyObject p = toPython(_js.getInt(n-1));
            PyObject repr = p.__repr__();
            foo.append(repr.toString());
        }
        foo.append("]");
        return foo.toString();
    }

    @ExposedMethod()
    public PyObject jsarraywrapper_append(PyObject value){
        _js.add(toJS(value));
        return Py.None;
    }

    public int count(PyObject o){
        return jsarraywrapper_count(o);
    }

    @ExposedMethod()
    final public int jsarraywrapper_count(PyObject value){
        Object jval = toJS(value);
        int n = __len__();
        int count = 0;
        for(int i = 0; i < n; ++i){
            if(_js.getInt(i).equals(jval))
                count++;
        }
        return count;
    }

    @ExposedMethod()
    public PyObject jsarraywrapper_insert(PyObject index, PyObject value){
        if( ! index.isIndex() ){
            throw Py.TypeError("an integer is required");
        }
        int i = fixindex(index.asIndex(Py.IndexError));
        _js.add(i, toJS(value));
        return Py.None;
    }

    @ExposedMethod(defaults="null")
    public PyObject jsarraywrapper_pop(PyObject index){
        if( index == null ){
            return toPython( _js.remove( _js.size() - 1 ) );
        }

        if( ! index.isIndex() ){
            throw Py.TypeError("an integer is required");
        }
        int i = fixindex(index.asIndex(Py.IndexError));
        return toPython( _js.remove( i ) );
    }

    @ExposedMethod
    public PyObject jsarraywrapper_extend(PyObject extra){
        throw new RuntimeException("not implemented yet");
    }

    @ExposedMethod(defaults={"null", "null"})
    public PyObject jsarraywrapper_index(PyObject x, PyObject start, PyObject end){
        Object jsX = toJS( x );
        int i = 0;
        if(start != null) i = calculateIndex(PySlice.calculateSliceIndex(start));
        int j = _js.size();
        if(end != null) j = calculateIndex(PySlice.calculateSliceIndex(end));

        for(int n = i; n < j; ++n){
            if(_js.getInt( n ).equals( jsX ))
                return Py.newInteger( n );
        }

        throw Py.ValueError("list.index(x): x not in list");
    }

    @ExposedMethod
    public PyObject jsarraywrapper_remove(PyObject x){
        throw new RuntimeException("not implemented yet");
    }

    @ExposedMethod
    public PyObject jsarraywrapper_reverse(){
        throw new RuntimeException("not implemented yet");
    }

    @ExposedMethod
    public PyObject jsarraywrapper_sort(PyObject[] args, String[] keywords){
        // Rather than reimplement sorting with Python semantics, I just copy
        // the JSArray into a PyList and call sort on that, and extract
        // the result.
        PyList list = new PyList();
        int n = _js.size();
        for(int i = 0; i < n; ++i){
            list.append( toPython( _js.getInt( i ) ) );
        }

        list.invoke("sort", args, keywords);
        //int n = list.__len__();
        for(int i = 0; i < n; ++i){
            _js.setInt( i , toJS( list.__finditem__(i) ) );
        }
        return Py.None;
    }

    // eq, ne, lt, le, gt, ge, cmp
    // finditem, setitem, getslice, delslice -- handled for us?
    final JSArray _js;
}
