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

import org.testng.annotations.Test;

import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.ruby.RubyObjectId;
import ed.lang.ruby.RubyObjectWrapper;
import static ed.lang.ruby.RubyObjectWrapper.*;

public class RubyObjectWrapperTest extends ed.TestCase {

    Scope s = new Scope("test", null);
    org.jruby.Ruby r = org.jruby.Ruby.newInstance();
    JSFunction addSevenFunc = new JSFunctionCalls1() {
	    public Object call(Scope scope, Object arg, Object extras[]) {
		return new Integer(((Number)arg).intValue() + 7);
	    }
	};

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSNull() {
	assertNull(toJS(r, null));
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	RubyObjectId rid = new RubyObjectId(r, oid);
	assertEquals(oid, toJS(r, rid));
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSNumber() {
	jsNumberTest(42L, new Byte((byte)42));
	jsNumberTest(42L, new Short((short)42));
	jsNumberTest(42L, new Integer(42));
	jsNumberTest(42L, new Long(42));
	jsNumberTest(42.0, new Float(42.0));
	jsNumberTest(42.0, new Double(42.0));
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSBignum() {
	IRubyObject ro = new RubyBignum(r, new BigInteger("42"));
	Object o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(42L, ((Number)o).longValue());
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSBigDecimal() {
	IRubyObject ro = new RubyBigDecimal(r, new BigDecimal((double)42.4));
	Object o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(42.4, ((Number)o).doubleValue());
    }

    void jsNumberTest(long val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(val, ((Number)o).longValue());
    }

    void jsNumberTest(double val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(val, ((Number)o).doubleValue());
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSString() {
	Object o = toJS(r, RubyString.newUnicodeString(r, "test string"));
	assertTrue(o instanceof JSString);
	assertEquals("test string", o.toString());
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSFunction() {
	// TODO
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSObject() {
	// TODO
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSArray() {
	RubyArray ra = (RubyArray)r.evalScriptlet("[4, 5, 6]");
	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	for (int i = 0; i < 3; ++i)
	    assertEquals(i + 4, ((Number)ja.getInt(i)).intValue());
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSArrayDeep() {
	RubyArray ra = (RubyArray)r.evalScriptlet("[4, \"test string\", [3]]");
	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	assertEquals(4, ((Number)ja.getInt(0)).intValue());
	assertEquals("test string", ja.getInt(1).toString());
	o = ja.getInt(2);
	assertTrue(o instanceof JSArray);
	assertEquals(3, ((Number)((JSArray)o).getInt(0)).intValue());
    }

    @Test(groups = {"ruby", "convert", "r2js"})
    public void testToJSMap() {
	RubyHash rh = (RubyHash)r.evalScriptlet("{\"a\" => 1, :b => \"test string\"}");

	Object o = toJS(r, rh);
	assertTrue(o instanceof JSMap);
	JSMap map = (JSMap)(o);

	Object val = map.get("a");
	assertNotNull(val);
	assertTrue(val instanceof Number);
	assertEquals(1, ((Number)val).intValue());

	val = map.get("b");
	assertNotNull(val);
	assertTrue(val instanceof JSString);
	assertEquals("test string", val.toString());
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyNil() {
	assertEquals(r.getNil(), toRuby(s, r, null));
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	IRubyObject ro = toRuby(s, r, oid);
	assertTrue(ro instanceof RubyObjectId);
	assertEquals(oid, ((RubyObjectId)ro)._id);
    }

    @Test(groups = {"ruby", "convert", "js2r"})
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
	assertEquals(bi, ((RubyBignum)ro).getValue());

	BigDecimal bd = new BigDecimal("42");
	ro = toRuby(s, r, bd);
	assertTrue(ro instanceof RubyBigDecimal);
	assertEquals(bd, ((RubyBigDecimal)ro).getValue());
    }

    void createNumberTest(long val, Object jobj) {
	IRubyObject ro = toRuby(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(val, RubyNumeric.num2long(ro));
    }

    void createNumberTest(double val, Object jobj) {
	IRubyObject ro = toRuby(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(val, RubyNumeric.num2dbl(ro));
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyFunction() {
	IRubyObject ro = toRuby(s, r, addSevenFunc, "add_seven"); // null container: add to top-level object
	assertTrue(ro instanceof RubyJSFunctionWrapper);

	assertEquals("true", r.evalScriptlet("respond_to?(:add_seven).to_s").toString());

	IRubyObject result = r.evalScriptlet("add_seven(35)");
	assertTrue(result instanceof RubyNumeric);
	assertEquals(42L, RubyNumeric.num2long(result));
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyFunctionNameInObject() {
	JSObjectBase jo = new JSObjectBase();
	jo.set(new JSString("add_seven"), addSevenFunc);
	IRubyObject ro = toRuby(s, r, jo);
	r.getGlobalVariables().set("$obj_with_func", ro);

	try {
	    IRubyObject result = r.evalScriptlet("$obj_with_func.add_seven(35)");
	    assertTrue(result instanceof RubyNumeric);
	    assertEquals(42L, RubyNumeric.num2long(result));
	}
	catch (Exception e) {
	    e.printStackTrace();
	    assertTrue(false);
	}
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyFunctionInArray() {
	JSArray a = new JSArray(new Long(1), new Float(2.3), addSevenFunc);
	RubyArray ra = (RubyArray)toRuby(s, r, a);
	assertTrue(ra.entry(2) instanceof RubyJSFunctionWrapper);
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyJSObject() {
	// TODO
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyJSArray() {
	JSArray a = new JSArray(new Long(1), new Float(2.3), new JSString("test string"));
	IRubyObject ro = toRuby(s, r, a);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertEquals(3, ra.size());
	assertEquals(1L, RubyNumeric.num2long(ra.entry(0)));
	assertEquals(2.3, RubyNumeric.num2dbl(ra.entry(1)), 0.0001);
	assertEquals("test string", ra.entry(2).toString());
    }

    @Test(groups = {"ruby", "convert", "js2r"})
    public void testToRubyJSMap() {
	// Create JSMap {"a" => 1, 42 => "test string"}
	JSMap map = new JSMap();
	map.set(new JSString("a"), new Integer(1));
	map.set(new Long(42), new JSString("test string"));

	IRubyObject ro = toRuby(s, r, map);
	assertTrue(ro instanceof RubyHash);
	RubyHash rh = (RubyHash)ro;
	assertEquals(2, rh.keys().size());

	IRubyObject o = rh.op_aref(r.getCurrentContext(), RubyString.newUnicodeString(r, "a"));
	assertTrue(!r.getNil().equals(o));
	assertTrue(o instanceof RubyNumeric);
	assertEquals(1L, ((RubyNumeric)o).getLongValue());

	o = rh.op_aref(r.getCurrentContext(), JavaUtil.convertJavaToUsableRubyObject(r, new Long(42)));
	assertTrue(!r.getNil().equals(o));
	assertTrue(o instanceof RubyString);
	assertEquals("test string", ((RubyString)o).toString());
    }

    public static void main(String args[]) {
        new RubyObjectWrapperTest().runConsole();
    }
}
