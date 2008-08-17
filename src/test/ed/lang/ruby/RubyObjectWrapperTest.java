// RubyObjectWrapperTest.java

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
import ed.lang.ruby.RubyObjectId;
import ed.lang.ruby.RubyObjectWrapper;
import static ed.lang.ruby.RubyObjectWrapper.*;

public class RubyObjectWrapperTest extends ed.TestCase {

    Scope s = new Scope("test", null);
    org.jruby.Ruby r = org.jruby.Ruby.newInstance();

    @Test(groups = {"convert", "r2js"})
    public void testToJSNull() {
	assertNull(toJS(r, null));
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	RubyObjectId rid = new RubyObjectId(r, oid);
	assertEquals(oid, toJS(r, rid));
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
	assertEquals(42L, ((Number)o).longValue());
    }

    @Test(groups = {"convert", "r2js"})
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

    @Test(groups = {"convert", "r2js"})
    public void testToJSString() {
	Object o = toJS(r, RubyString.newUnicodeString(r, "test string"));
	assertTrue(o instanceof JSString);
	assertEquals("test string", o.toString());
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
	RubyArray ra = RubyArray.newArray(r, 3);
	for (int i = 0; i < 3; ++i)
	    ra.store(i, new RubyFixnum(r, i + 4));
	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	for (int i = 0; i < 3; ++i)
	    assertEquals(i + 4, ((Number)ja.getInt(i)).intValue());
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSArrayDeep() {
	// Create ruby array [4, "test string", [3]]
	RubyArray ra = RubyArray.newArray(r, 3);
	ra.store(0, new RubyFixnum(r, 4));
	ra.store(1, RubyString.newUnicodeString(r, "test string"));
	RubyArray subarray = RubyArray.newArray(r, 1);
	subarray.store(0, new RubyFixnum(r, 3));
	ra.store(2, subarray);

	Object o = toJS(r, ra);
	assertTrue(o instanceof JSArray);
	JSArray ja = (JSArray)o;
	assertEquals(4, ((Number)ja.getInt(0)).intValue());
	assertEquals("test string", ja.getInt(1).toString());
	o = ja.getInt(2);
	assertTrue(o instanceof JSArray);
	assertEquals(3, ((Number)((JSArray)o).getInt(0)).intValue());
    }

    @Test(groups = {"convert", "r2js"})
    public void testToJSMap() {
	// Create ruby hash {"a" => 1, :b => "test string"}
	RubyHash rh = new RubyHash(r);
	rh.op_aset(r.getCurrentContext(), RubyString.newUnicodeString(r, "a"), new RubyFixnum(r, 1));
	rh.op_aset(r.getCurrentContext(), RubySymbol.newSymbol(r, "b"), RubyString.newUnicodeString(r, "test string"));

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

    @Test(groups = {"convert", "js2r"})
    public void testCreateNil() {
	assertEquals(r.getNil(), create(s, r, null));
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	IRubyObject ro = create(s, r, oid);
	assertTrue(ro instanceof RubyObjectId);
	assertEquals(oid, ((RubyObjectId)ro)._id);
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateNumber() {
	createNumberTest(42, new Byte((byte)42));
	createNumberTest(42, new Short((byte)42));
	createNumberTest(42, new Integer((byte)42));
	createNumberTest(42, new Long((byte)42));
	createNumberTest(42.0, new Float((byte)42));
	createNumberTest(42.0, new Double((byte)42));

	BigInteger bi = new BigInteger("42");
	IRubyObject ro = create(s, r, bi);
	assertTrue(ro instanceof RubyBignum);
	assertEquals(bi, ((RubyBignum)ro).getValue());

	BigDecimal bd = new BigDecimal("42");
	ro = create(s, r, bd);
	assertTrue(ro instanceof RubyBigDecimal);
	assertEquals(bd, ((RubyBigDecimal)ro).getValue());
    }

    void createNumberTest(long val, Object jobj) {
	IRubyObject ro = create(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(val, RubyNumeric.num2long(ro));
    }

    void createNumberTest(double val, Object jobj) {
	IRubyObject ro = create(s, r, jobj);
	assertTrue(ro instanceof RubyNumeric);
	assertEquals(val, RubyNumeric.num2dbl(ro));
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateFunction() {
	// TODO
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateJSObject() {
	// TODO
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateJSArray() {
	JSArray a = new JSArray(new Long(1), new Float(2.3), new JSString("test string"));
	IRubyObject ro = create(s, r, a);
	assertTrue(ro instanceof RubyArray);
	RubyArray ra = (RubyArray)ro;
	assertEquals(3, ra.size());
	assertEquals(1L, RubyNumeric.num2long(ra.entry(0)));
	assertEquals(2.3, RubyNumeric.num2dbl(ra.entry(1)), 0.0001);
	assertEquals("test string", ra.entry(2).toString());
    }

    @Test(groups = {"convert", "js2r"})
    public void testCreateJSMap() {
	// Create JSMap {"a" => 1, 42 => "test string"}
	JSMap map = new JSMap();
	map.set(new JSString("a"), new Integer(1));
	map.set(new Long(42), new JSString("test string"));

	IRubyObject ro = create(s, r, map);
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
