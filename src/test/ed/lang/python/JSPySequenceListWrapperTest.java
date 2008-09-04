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

import org.testng.annotations.Test;

import ed.js.*;
import ed.js.engine.*;
import org.python.core.*;
import ed.lang.python.*;

public class JSPySequenceListWrapperTest extends ed.TestCase {
    @Test(groups = {"basic"})
    public void test1(){
        PySequenceList p = new PyList();
        JSPySequenceListWrapper wrapper = new JSPySequenceListWrapper( p );
        JSObject o = new JSObjectBase();
        assert wrapper.add( o );
        assert wrapper.get( 0 ) == o;

        JSObject p1 = new JSObjectBase();
        p1.set( "attr" , new Integer( 34 ) );
        wrapper.add( p1 );

        wrapper.add( p1 );

        wrapper.add( o );

        wrapper.add( p1 );

        // [o, p1, p1, o, p1]
        //  0  1   2   3  4
        java.util.List foo = wrapper.subList( 1, 4 );
        assert foo.get( 0 ) == p1;
        assert foo.get( 1 ) == p1;
        assert foo.get( 2 ) == o;
        assert foo.size() == 3;
        // foo.get( 3 ) should be an exception
    }

    public static void main( String args[] ){
        (new JSONTest()).runConsole();
    }
}
