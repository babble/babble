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

@Test(groups = {"ruby", "ruby.jxpsource"})
public class RubyJxpSourceTest extends SourceRunner {

    @BeforeTest(groups={"ruby", "ruby.jxpsource"})
    public void globalSetUp() {
	super.globalSetUp();
    }

    @BeforeMethod(groups={"ruby", "ruby.jxpsource"})
    public void setUp() {
	runJS("add_seven = function(i) { return i + 7; };" +
	      "two_args = function(a, b) { return a + b; };" +
	      "data = {};" +
	      "data.count = 1;" +
	      "data.subobj = {};" +
	      "data.subobj.subvar = 99;" +
	      "data.subobj.add_seven = add_seven;" +
	      "data.add_seven = add_seven;" +
	      "array = [100, \"test string\", null, add_seven];");
    }

    @Test
    public void testContent() {
	assertRubyEquals("puts 1 + 1", "2");
    }

    @Test
    public void testScopeToVar() {
	assertRubyEquals("puts data.count;", "1");
    }

    @Test
    public void testCallFuncInVar() {
	assertRubyEquals("puts data.add_seven(35)", "42");
    }

    @Test
    public void testCallTopLevelFunc() {
	assertRubyEquals("puts add_seven(35)", "42");
    }

    @Test
    public void testGet() {
	assertRubyEquals("puts data.get('count')", "1");
    }

    @Test
    public void testGetUsingHashSyntax() {
	assertRubyEquals("puts data['count']", "1");
    }

    @Test
    public void testRubyModifiesJS() {
	runRuby("data.count = 42");
	assertEquals(s.eval("data.count").toString(), "42");
    }

    @Test
    public void testModifyUsingSet() {
	runRuby("data.set('count', 42)");
	assertEquals(s.eval("data.count").toString(), "42");
    }

    @Test
    public void testModifyUsingHash() {
	runRuby("data['count'] = 42");
	assertEquals(s.eval("data.count").toString(), "42");
    }

    @Test
    public void testCreateNew() {
	runRuby("data['vint'] = 3; data.vfloat = 4.2; data.varray = [1, 2, 'three']; data.vhash = {'a' => 1, 'b' => 2}");
	assertEquals(s.eval("data.vint").toString(), "3");
	assertEquals(s.eval("data.vfloat").toString(), "4.2");
	assertEquals(s.eval("data.varray").toString(), "1,2,three");
	assertEquals(s.eval("data.vhash['a']").toString(), "1");
	assertEquals(s.eval("data.vhash['b']").toString(), "2");
	assertEquals(s.eval("data.vhash.a").toString(), "1");
	assertEquals(s.eval("data.vhash.b").toString(), "2");
    }

    @Test
    public void testBuiltIn() {
	assertRubyEquals("puts data.keySet.sort", "[add_seven, count, subobj]");
    }

    @Test
    public void testSubObject() {
	assertRubyEquals("puts data.subobj.subvar", "99");
	assertRubyEquals("puts data.subobj.add_seven(35)", "42");
    }

    // FIXME
//     @Test
//     public void testRubyTopLevelVarIntoScope() {
// 	runRuby("data.count = 33; x = 42");
// 	assertNotNull(s.get("x"), "top-level 'x' should not be null; it should be in scope");
// 	assertEquals(s.get("x").toString(), "42");
//     }

    @Test
    public void testReadScope() {
	assertRubyEquals("puts $scope.get('array')[1]", "test string");
    }

    @Test
    public void testWriteScope() {
	runRuby("$scope.set('new_thing', 57)");
	assertNotNull(s.get("new_thing"), "'new_thing' not defined in scope");
	assertEquals(s.get("new_thing").toString(), "57");
    }

    @Test
    public void testWriteScopeUsingHashSyntax() {
	runRuby("$scope['new_thing'] = 57");
	assertNotNull(s.get("new_thing"), "'new_thing' not defined in scope");
	assertEquals(s.get("new_thing").toString(), "57");
    }
}
