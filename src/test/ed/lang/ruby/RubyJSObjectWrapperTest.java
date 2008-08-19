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

import org.jruby.*;
import org.jruby.runtime.builtin.IRubyObject;

import ed.lang.ruby.RubyObjectWrapper;
import ed.lang.ruby.RubyJSObjectWrapper;
import ed.js.*;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.*;

@Test(groups = {"ruby", "ruby.jsobj"})
public class RubyJSObjectWrapperTest {

    Scope s;
    org.jruby.Ruby r;
    JSFunction addSevenFunc;

    @BeforeTest
    public void setUp() {
	s = new Scope("test", null);
	r = org.jruby.Ruby.newInstance();
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };

	JSObjectBase jobj = new JSObjectBase();
	jobj.set("count", new Integer(1));
	jobj.set("add_seven", addSevenFunc);

	RubyObject top = (RubyObject)r.getTopSelf();
	RubyClass eigenclass = top.getSingletonClass();
	top.instance_variable_set(RubySymbol.newSymbol(r, "@data"), RubyObjectWrapper.toRuby(s, r, jobj, "data"));
	eigenclass.attr_reader(r.getCurrentContext(), new IRubyObject[] {RubySymbol.newSymbol(r, "data")});
    }

    public void testAccessors() {
	assertEquals(RubyNumeric.num2long(r.evalScriptlet("data.count")), 1L);
	assertEquals(RubyNumeric.num2long(r.evalScriptlet("data.count += 2; data.count")), 3L);
    }

    public void testMethodMissing() {
	IRubyObject answer = r.evalScriptlet("data.hash");
	assertTrue(answer instanceof RubyFixnum);
    }

    public void testMethodMissingNoSuchMethod() {
	try {
	    r.evalScriptlet("data.xyzzy");
	    fail("should have thrown a NoMethodError");
	}
	catch (org.jruby.exceptions.RaiseException re) {
	    String msg = re.getException().toString();
	    assertTrue(msg.startsWith("undefined method `xyzzy'"));
	}
	catch (Exception e) {
	    fail("Expected NoMethodError, got " + e.getClass().getName() + ": " + e.toString());
	}
    }

    public void testInstanceVariables() {
	IRubyObject ro = r.evalScriptlet("data.instance_variables");
	assertNotNull(ro);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertTrue(ra.includes(r.getCurrentContext(), r.fastNewSymbol("@count")), "instance variable \"@count\" is missing");
    }

    public void testPublicMethods() {
	IRubyObject ro = r.evalScriptlet("data.public_methods");
	assertNotNull(ro);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertTrue(ra.includes(r.getCurrentContext(), r.fastNewSymbol("count")), "public method \"count\" is missing");
	assertTrue(ra.includes(r.getCurrentContext(), r.fastNewSymbol("count=")), "public method \"count=\" is missing");
	assertTrue(ra.includes(r.getCurrentContext(), r.fastNewSymbol("add_seven")), "public method \"add_seven\" is missing");
    }
}
