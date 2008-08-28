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
    JSObject jsobj;
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

	jsobj = new JSObjectBase();
	jsobj.set("count", new Integer(1));
	jsobj.set("add_seven", addSevenFunc);

	RubyObject top = (RubyObject)r.getTopSelf();
	RubyClass eigenclass = top.getSingletonClass();
	r.getGlobalVariables().set("$data", RubyObjectWrapper.toRuby(s, r, jsobj));
    }

    public void testAccessors() {
	assertEquals(RubyNumeric.num2long(r.evalScriptlet("$data.count")), 1L);
	assertEquals(RubyNumeric.num2long(r.evalScriptlet("$data.count += 2; $data.count")), 3L);
    }

    @Test(groups = {"js2r"})
    public void testJSFunctionNameInJSArrayToRuby() {
	JSObjectBase jo = new JSObjectBase();
	jo.set(new JSString("add_seven"), addSevenFunc);
	IRubyObject ro = toRuby(s, r, jo);
	r.getGlobalVariables().set("$obj_with_func", ro);

	try {
	    IRubyObject result = r.evalScriptlet("$obj_with_func.add_seven(35)");
	    assertTrue(result instanceof RubyNumeric);
	    assertEquals(RubyNumeric.num2long(result), 42L);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    fail(e.toString());
	}
    }

    public void testInnerJSObjectHasFunction() {
	JSObjectBase innie = new JSObjectBase();
	innie.set(new JSString("add_seven"), addSevenFunc);
	JSObjectBase jo = new JSObjectBase();
	jo.set(new JSString("innie"), innie);
	IRubyObject ro = toRuby(s, r, jo);
	r.getGlobalVariables().set("$obj_with_innie", ro);

	try {
	    IRubyObject result = r.evalScriptlet("$obj_with_innie.innie.add_seven(35)");
	    assertTrue(result instanceof RubyNumeric);
	    assertEquals(RubyNumeric.num2long(result), 42L);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    fail(e.toString());
	}
    }

    public void testMethodMissing() {
	IRubyObject answer = r.evalScriptlet("$data.hash");
	assertTrue(answer instanceof RubyFixnum);
    }

    public void testMethodMissingNoSuchMethod() {
	try {
	    IRubyObject answer = r.evalScriptlet("$data.xyzzy");
	    fail("expected method_missing exception");
	}
	catch (org.jruby.exceptions.RaiseException re) {
	    assertNotNull(re.getException());
	    assertTrue(re.getException().toString().contains("undefined method `xyzzy'"), "raised exception should be \"undefined method `xyzzy'\"; instead text = " + re.getException().toString());
	}
	catch (Exception e) {
	    fail("expected org.jruby.exceptions.RaiseException, saw " + e.toString());
	}
    }

    public void testInstanceVariables() {
	IRubyObject ro = r.evalScriptlet("$data.instance_variables");
	assertNotNull(ro);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertTrue(ra.includes(r.getCurrentContext(), RubyString.newString(r, "@count")), "instance variable \"@count\" is missing");
    }

    @Test(groups = {"js2r"})
    public void testRespondsTo() {
	IRubyObject ro = toRuby(s, r, jsobj);
	assertTrue(ro.respondsTo("count"));
	assertTrue(ro.respondsTo("count="));
	assertTrue(ro.respondsTo("add_seven"));
    }

    public void testPublicMethods() {
	IRubyObject ro = r.evalScriptlet("$data.public_methods");
	assertNotNull(ro);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertTrue(ra.includes(r.getCurrentContext(), RubyString.newString(r, "count")), "public method \"count\" is missing");
	assertTrue(ra.includes(r.getCurrentContext(), RubyString.newString(r, "count=")), "public method \"count=\" is missing");
	assertTrue(ra.includes(r.getCurrentContext(), RubyString.newString(r, "add_seven")), "public method \"add_seven\" is missing");
    }

    public void testLameWithGetSetValue() {
	JSObjectLame lame_o = new JSObjectLame() {
		Object val = "hi";
		public Object get(Object n) { return val; }
		public Object set(Object n, Object v) { val = v; return v; }
	    };
	r.getGlobalVariables().set("$lame_o", RubyObjectWrapper.toRuby(s, r, lame_o));

	IRubyObject ro = toRuby(s, r, lame_o);
	assertTrue(((RubyObject)ro).respond_to_p(RubySymbol.newSymbol(r, ":foo")).isTrue());

	Object answer = r.evalScriptlet("$lame_o.foo");
	assertEquals(answer.toString(), "hi");

	answer = r.evalScriptlet("$lame_o.foo = 'howdy'; $lame_o.foo");
	assertEquals(answer.toString(), "howdy");
	assertEquals(lame_o.get("foo").toString(), "howdy");
    }

    public void testLameWithFunc() {
	JSObjectLame lame_o = new JSObjectLame() {
		public Object get(Object n) { return addSevenFunc; }
	    };
	r.getGlobalVariables().set("$lame_o", RubyObjectWrapper.toRuby(s, r, lame_o));

	IRubyObject ro = toRuby(s, r, lame_o);
	assertTrue(!ro.respondsTo("add_seven"));

	Object answer = r.evalScriptlet("$lame_o.add_seven(35)");
	assertEquals(answer.toString(), "42");
    }
}
