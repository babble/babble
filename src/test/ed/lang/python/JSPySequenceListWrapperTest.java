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

package ed.js;

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
    }

    public static void main( String args[] ){
        (new JSONTest()).runConsole();
    }
}
