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

import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;
import ed.lang.ruby.RubyObjectId;
import ed.lang.ruby.RubyObjectWrapper;
import static ed.lang.ruby.RubyObjectWrapper.*;

public class RubyObjectWrapperTest extends ed.TestCase {

    Scope s = new Scope("test", null);
    org.jruby.Ruby r = org.jruby.Ruby.newInstance();

    public void testCreate() {
	assertEquals(r.getNil(), create(s, r, null, null));
    }

    public void testToJSNull() {
	assertNull(toJS(r, null));
    }

    public void testToJSOid() {
	ObjectId oid = new ObjectId("0123456789abcdef01");
	RubyObjectId rid = new RubyObjectId(r, oid);
	assertEquals(oid, toJS(r, rid));
    }

    public void testToJSNumber() {
	numberTest(42L, new Byte((byte)42));
	numberTest(42L, new Short((short)42));
	numberTest(42L, new Integer(42));
	numberTest(42L, new Long(42));
	numberTest(42.0, new Float(42.0));
	numberTest(42.0, new Double(42.0));

	IRubyObject ro = new RubyBignum(r, new BigInteger("42"));
	Object o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(42L, ((Number)o).longValue());

	ro = new RubyBigDecimal(r, new BigDecimal((double)42.4));
	o = toJS(r, ro);
	assertTrue(toJS(r, ro) instanceof Number);
	assertEquals(42.4, ((Number)o).doubleValue());
    }

    void numberTest(long val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(val, ((Number)o).longValue());
    }

    void numberTest(double val, Object jobj) {
	IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
	Object o = toJS(r, ro);
	assertTrue(o instanceof Number);
	assertEquals(val, ((Number)o).doubleValue());
    }

    public void testToJSString() {
	Object o = toJS(r, RubyString.newUnicodeString(r, "test string"));
	assertTrue(o instanceof JSString);
	assertEquals("test string", o.toString());
    }

    public void testToJSFunction() {
	// TODO
    }

    public void testToJSObject() {
	// TODO
    }

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

    public void testToJSMap() {
	// Create ruby hash {"a" => 1, :b => "test string"}
	RubyHash rh = new RubyHash(r);
	rh.op_aset(RubyString.newUnicodeString(r, "a"), new RubyFixnum(r, 1));
	rh.op_aset(RubySymbol.newSymbol(r, "b"), RubyString.newUnicodeString(r, "test string"));

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

    public void testCreateNil() {
	// TODO
    }

    public void testCreateOid() {
	// TODO
    }

    public void testCreateNumber() {
	// TODO
    }

    public void testCreateFunction() {
	// TODO
    }

    public void testCreateJSObject() {
	// TODO
    }

    public void testCreateJSArray() {
	// TODO
    }

    public void testCreateJSMap() {
	// TODO
    }

    public static void main(String args[]) {
        new RubyObjectWrapperTest().runConsole();
    }
}
