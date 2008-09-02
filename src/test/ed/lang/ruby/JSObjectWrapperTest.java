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

import ed.lang.ruby.JSObjectWrapper;
import ed.js.JSObject;
import ed.js.engine.Scope;

@Test(groups = {"ruby", "ruby.jsobj"})
public class JSObjectWrapperTest extends SourceRunner {

    @BeforeTest(groups={"ruby", "ruby.jsobj"})
    public void globalSetUp() {
	super.globalSetUp();
    }

    @BeforeMethod(groups={"ruby", "ruby.jsobj"})
    public void setUp() {
	runRuby("class Super; attr_accessor :s; end; class Foo < Super; attr_accessor :foo; end; $x = Foo.new");
    }

    public void testConversion() {
	Object o = s.get("x");
	assertNotNull(o);
	assertTrue(o instanceof JSObjectWrapper, "oops: wrong type: expected JSObjectWrapper but see " + o.getClass().getName());
    }

    public void testModifyRubyObject() {
	JSObject jsobj = (JSObject)s.get("x");
	jsobj.set("s", 42);
	runRuby("puts $x.s");
	assertEquals(rubyOutput, "42");
    }

    public void testRubyClassUnchanged() {
	JSObject jsobj = (JSObject)s.get("x");
	jsobj.set("s", 42);
	runRuby("puts $x.class.name");
	assertEquals(rubyOutput, "Foo");
    }
}
