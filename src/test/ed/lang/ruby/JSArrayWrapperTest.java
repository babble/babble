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

package ed.lang.ruby;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.js.JSArray;

@Test(groups = {"ruby", "ruby.jsobj"})
public class JSArrayWrapperTest extends SourceRunner {

    protected JSArray jsobj;

    @BeforeMethod(groups={"ruby", "ruby.jsobj"})
    public void setUp() {
        super.setUp();
        runRuby("$x = [1, 'a', 3]");
        jsobj = (JSArray)s.get("x");
    }

    public void testConversion() {
        assertNotNull(jsobj);
        assertTrue(jsobj instanceof ed.lang.ruby.JSArrayWrapper, "oops: wrong type: expected ed.lang.ruby.JSArrayWrapper but see " + jsobj.getClass().getName());

        jsobj.setInt(1, 42);
        runRuby("puts $x.class.name");
        assertEquals(rubyOutput, "Array");
    }

    public void testModifyRubyObject() {
        jsobj.setInt(1, 42);
        runRuby("puts $x[1]");
        assertEquals(rubyOutput, "42");
    }

    public void testLength() {
        assertEquals(3, jsobj.get("length"));
        assertEquals(3, jsobj.size()); // Proves that internal _array is set properly
    }

    public void testAccess() {
        assertEquals(1, ((Integer)jsobj.getInt(0)).intValue());
        assertEquals("a", jsobj.getInt(1).toString());
        assertEquals(3, ((Integer)jsobj.getInt(2)).intValue());
    }
}
