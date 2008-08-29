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

import org.jruby.runtime.builtin.IRubyObject;

import ed.lang.ruby.JSFunctionWrapper;
import ed.js.*;
import ed.js.engine.Scope;

@Test(groups = {"ruby", "ruby.block"})
public class JSFunctionWrapperTest extends SourceRunner {

    @BeforeTest(groups={"ruby", "ruby.block"})
    public void globalSetUp() {
	super.globalSetUp();
    }

    @BeforeMethod(groups={"ruby", "ruby.block"})
    public void setUp() {
	runRuby("$rfunc = Proc.new {|i| i + 7}");
    }

    public void testConversion() {
	Object o = s.get("rfunc");
	assertNotNull(o);
	assertTrue(o instanceof JSFunctionWrapper, "oops: wrong type: expected JSFunctionWrapper but see " + o.getClass().getName());
    }

    public void testCallBlockAsJSFunction() {
	Object o = RubyObjectWrapper.toJS(s, (IRubyObject)runRuby("Proc.new {|i| i + 7}"));
	assertTrue(o instanceof JSFunctionWrapper);
	JSFunctionWrapper fw = (JSFunctionWrapper)o;
	Object answer = fw.call(s, new Integer(35), new Object[0]);
	assertNotNull(answer);
	assertTrue(answer instanceof Number);
	assertEquals(((Number)answer).intValue(), 42);
    }
}
