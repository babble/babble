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

import java.math.BigInteger;
import java.math.BigDecimal;

import org.jruby.*;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.ruby.RubyObjectWrapper;
import static ed.lang.ruby.RubyObjectWrapper.*;

@Test(groups = {"ruby"})
public class RubyObjectWrapperTest {

    static JSFunction addSevenFunc;
    Scope s;
    org.jruby.Ruby r;

    @BeforeClass(groups = {"convert", "r2js", "js2r"})
    public void classSetUp() {
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };
    }

    @BeforeTest(groups = {"convert", "r2js", "js2r"})
    public void setUp() {
	s = new Scope("test", null);
	r = org.jruby.Ruby.newInstance();
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSNull() {
	assertNull(toJS(r, null));
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSNumber() {
	jsNumberTest(42L, new Byte((byte)42));
	jsNumberTest(42L, new Short((short)42));
	jsNumberTest(42L, new Integer(42));
	jsNumberTest(42L, new Long(42));
	jsNumberTest(42.0, new Float(42.0));
	jsNumberTest(42.0, new Double(42.0));
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSBignum() {
	IRubyObject ro = new RubyBignum(r, new BigInteger("42"));
	Object o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(((Number)o).longValue(), 42L);
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSBigDecimal() {
	IRubyObject ro = new RubyBigDecimal(r, new BigDecimal((double)42.4));
	Object o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(((Number)o).doubleValue(), 42.4);
    }

    void jsNumberTest(long val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(((Number)o).longValue(), val);
    }

    void jsNumberTest(double val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(((Number)o).doubleValue(), val);
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSString() {
	Object o = toJS(r, RubyString.newUnicodeString(r, "test string"));
	assertTrue(o instanceof JSString);
	assertEquals(o.toString(), "test string");
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSFunction() {
	// TODO
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSObject() {
	// TODO
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSArray() {
	RubyArray ra = (RubyArray)r.evalScriptlet("[4, 5, 6]");
	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	for (int i = 0; i < 3; ++i)
	    assertEquals(((Number)ja.getInt(i)).intValue(), i + 4);
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSArrayDeep() {
	RubyArray ra = (RubyArray)r.evalScriptlet("[4, \"test string\", [3]]");
	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	assertEquals(((Number)ja.getInt(0)).intValue(), 4);
	assertEquals(ja.getInt(1).toString(), "test string");
	o = ja.getInt(2);
	assertTrue(o instanceof JSArray);
	assertEquals(((Number)((JSArray)o).getInt(0)).intValue(), 3);
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSMap() {
	RubyHash rh = (RubyHash)r.evalScriptlet("{\"a\" => 1, :b => \"test string\"}");

	Object o = toJS(r, rh);
	assertTrue(o instanceof JSMap);
	JSMap map = (JSMap)(o);

	Object val = map.get("a");
	assertNotNull(val);
	assertTrue(val instanceof Number);
	assertEquals(((Number)val).intValue(), 1);

	val = map.get("b");
	assertNotNull(val);
	assertTrue(val instanceof JSString);
	assertEquals(val.toString(), "test string");
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyNil() {
	assertEquals(toRuby(s, r, null), r.getNil());
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	IRubyObject ro = toRuby(s, r, oid);
	assertTrue(ro instanceof RubyString);
	assertEquals(ro.toString(), oid.toString());
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyNumber() {
	createNumberTest(42, new Byte((byte)42));
	createNumberTest(42, new Short((byte)42));
	createNumberTest(42, new Integer((byte)42));
	createNumberTest(42, new Long((byte)42));
	createNumberTest(42.0, new Float((byte)42));
	createNumberTest(42.0, new Double((byte)42));

	BigInteger bi = new BigInteger("42");
	IRubyObject ro = toRuby(s, r, bi);
	assertTrue(ro instanceof RubyBignum);
	assertEquals(((RubyBignum)ro).getValue(), bi);

	BigDecimal bd = new BigDecimal("42");
	ro = toRuby(s, r, bd);
	assertTrue(ro instanceof RubyBigDecimal);
	assertEquals(((RubyBigDecimal)ro).getValue(), bd);
    }

    void createNumberTest(long val, Object jobj) {
	IRubyObject ro = toRuby(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(RubyNumeric.num2long(ro), val);
    }

    void createNumberTest(double val, Object jobj) {
	IRubyObject ro = toRuby(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(RubyNumeric.num2dbl(ro), val);
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyFunction() {
	IRubyObject ro = toRuby(s, r, addSevenFunc, "add_seven"); // null container: add to top-level object
	assertTrue(ro instanceof RubyJSFunctionWrapper);

	assertEquals(r.evalScriptlet("respond_to?(:add_seven).to_s").toString(), "true");

	IRubyObject result = r.evalScriptlet("add_seven(35)");
	assertTrue(result instanceof RubyNumeric);
	assertEquals(RubyNumeric.num2long(result), 42L);
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyFunctionNameInObject() {
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

    @Test(groups = {"convert", "js2r"})
    public void testToRubyFunctionInArray() {
	JSArray a = new JSArray(new Long(1), new Float(2.3), addSevenFunc);
	RubyArray ra = (RubyArray)toRuby(s, r, a);
	assertTrue(ra.entry(2) instanceof RubyJSFunctionWrapper);
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyJSObject() {
	// TODO
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyJSArray() {
	JSArray a = new JSArray(new Long(1), new Float(2.3), new JSString("test string"));
	IRubyObject ro = toRuby(s, r, a);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertEquals(ra.size(), 3);
	assertEquals(RubyNumeric.num2long(ra.entry(0)), 1L);
	assertEquals(RubyNumeric.num2dbl(ra.entry(1)), 2.3, 0.0001);
	assertEquals(ra.entry(2).toString(), "test string");
    }

    @Test(groups = {"convert", "js2r"})
    public void testToRubyJSMap() {
	// Create JSMap {"a" => 1, 42 => "test string"}
	JSMap map = new JSMap();
	map.set(new JSString("a"), new Integer(1));
	map.set(new Long(42), new JSString("test string"));

	IRubyObject ro = toRuby(s, r, map);
	assertTrue(ro instanceof RubyHash);
	RubyHash rh = (RubyHash)ro;
	assertEquals(rh.keys().size(), 2);

	IRubyObject o = rh.op_aref(r.getCurrentContext(), RubyString.newUnicodeString(r, "a"));
	assertTrue(!r.getNil().equals(o));
	assertTrue(o instanceof RubyNumeric);
	assertEquals(((RubyNumeric)o).getLongValue(), 1L);

	o = rh.op_aref(r.getCurrentContext(), JavaUtil.convertJavaToUsableRubyObject(r, new Long(42)));
	assertTrue(!r.getNil().equals(o));
	assertTrue(o instanceof RubyString);
	assertEquals(((RubyString)o).toString(), "test string");
    }
}
