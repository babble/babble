// JSPySequenceListWrapperTest.java

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

import org.testng.annotations.*;

import ed.js.*;
import ed.js.engine.*;
import org.python.core.*;
import ed.lang.python.*;

public class JSPySequenceListWrapperTest extends ed.TestCase {
    PySequenceList p;
    JSPySequenceListWrapper wrapper;
    JSObject o, p1;
    java.util.List sublist;

    @BeforeClass
    public void setUp(){
        p = new PyList(PyType.fromClass(PyList.class));
        wrapper = new JSPySequenceListWrapper( p );
        o = new JSObjectBase();
        p1 = new JSObjectBase();
        p1.set( "attr" , new Integer( 34 ) );
        wrapper.add( o );

        wrapper.add( p1 );

        wrapper.add( p1 );

        wrapper.add( o );

        wrapper.add( p1 );

        sublist = wrapper.subList( 1, 4 );
    }

    @Test(groups = {"basic"})
    public void test1(){
        assert wrapper.get( 0 ) == o;

        JSObject foo = new JSObjectBase();
        assert wrapper.add( foo );

        // [o, p1, p1, o, p1]
        //  0  1   2   3  4
        assert sublist.get( 0 ) == p1;
        assert sublist.get( 1 ) == p1;
        assert sublist.get( 2 ) == o;
        assert sublist.size() == 3;
        // sublist.get( 3 ) should be an exception
    }

    @Test(groups = {"basic"}, expectedExceptions={IndexOutOfBoundsException.class})
    public void test2(){
        sublist.get( 3 );
    }

    public static void main( String args[] ){
        (new JSPySequenceListWrapperTest()).runConsole();
    }
}
