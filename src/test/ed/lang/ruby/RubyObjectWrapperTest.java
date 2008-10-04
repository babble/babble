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

@Test(groups = {"ruby", "ruby.convert"})
public class RubyObjectWrapperTest {

    static JSFunction addSevenFunc;
    Scope s;
    org.jruby.Ruby r;

    @BeforeClass(groups = {"r2js", "js2r"})
    public void classSetUp() {
        addSevenFunc = new JSFunctionCalls1() {
                public Object call(Scope scope, Object arg, Object extras[]) {
                    return new Integer(((Number)arg).intValue() + 7);
                }
            };
    }

    @BeforeMethod(groups = {"r2js", "js2r"})
    public void setUp() {
        s = new Scope("test", null);
        r = org.jruby.Ruby.newInstance();
    }

    @Test(groups = {"r2js"})
    public void testToJSNull() {
        assertNull(toJS(s, (IRubyObject)null));
    }

    @Test(groups = {"r2js"})
    public void testRubyNilToJSNull() {
        assertNull(toJS(s, r.getNil()));
    }

    @Test(groups = {"r2js"})
    public void testRubyNumberToJSNumber() {
        jsNumberTest(42L, new Byte((byte)42));
        jsNumberTest(42L, new Short((short)42));
        jsNumberTest(42L, new Integer(42));
        jsNumberTest(42L, new Long(42));
        jsNumberTest(42.0, new Float(42.0));
        jsNumberTest(42.0, new Double(42.0));
    }

    @Test(groups = {"r2js"})
    public void testRubyBignumToJSBignum() {
        IRubyObject ro = new RubyBignum(r, new BigInteger("42"));
        Object o = toJS(s, ro);
        assertTrue(o instanceof Number, "expected Number, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(((Number)o).longValue(), 42L);
    }

    @Test(groups = {"r2js"})
    public void testRubyBigDecimalToJSBigDecimal() {
        IRubyObject ro = new RubyBigDecimal(r, new BigDecimal((double)42.4));
        Object o = toJS(s, ro);
        assertTrue(o instanceof Number, "expected Number, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(((Number)o).doubleValue(), 42.4);
    }

    void jsNumberTest(long val, Object jobj) {
        IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
        Object o = toJS(s, ro);
        assertTrue(o instanceof Number, "expected Number, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(((Number)o).longValue(), val);
    }

    void jsNumberTest(double val, Object jobj) {
        IRubyObject ro = JavaUtil.convertJavaToUsableRubyObject(r, jobj);
        Object o = toJS(s, ro);
        assertTrue(o instanceof Number, "expected Number, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(((Number)o).doubleValue(), val);
    }

    @Test(groups = {"r2js"})
    public void testRubyStringToJSString() {
        Object o = toJS(s, RubyString.newString(r, "test string"));
        assertTrue(o instanceof JSString, "expected JSString, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(o.toString(), "test string");
    }

    // Conversion from Ruby block to JSFunction is simple; it's the call that
    // needs to be tested. For that, see RubyJxpSourceTest.
//     @Test(groups = {"r2js"})
//     public void testRubyBlockToJSFunction() {
//     }

    @Test(groups = {"r2js"})
    public void testRubyObjectToJSObject() {
        JSObject jso = new JSObjectBase();
        IRubyObject ro = toRuby(s, r, jso);
        assertTrue(ro instanceof RubyJSObjectWrapper, "expected RubyJSObjectWrapper, saw " + (ro == null ? "null" : ro.getClass().getName()));

        Object o = toJS(s, ro);
        assertSame(o, jso);
    }

    @Test(groups = {"r2js"})
    public void testRubyArrayToJSArray() {
        RubyArray ra = (RubyArray)r.evalScriptlet("[4, 5, 6]");
        Object o = toJS(s, ra);
        assertTrue(o instanceof JSArray, "expected JSArray, saw " + (o == null ? "null" : o.getClass().getName()));
        JSArray ja = (JSArray)o;
        for (int i = 0; i < 3; ++i)
            assertEquals(((Number)ja.getInt(i)).intValue(), i + 4);
    }

    @Test(groups = {"r2js"})
    public void testRubyArrayToJSArrayDeep() {
        RubyArray ra = (RubyArray)r.evalScriptlet("[4, \"test string\", [3]]");
        Object o = toJS(s, ra);
        assertTrue(o instanceof JSArray, "expected JSArray, saw " + (o == null ? "null" : o.getClass().getName()));
        JSArray ja = (JSArray)o;
        assertEquals(((Number)ja.getInt(0)).intValue(), 4);
        assertEquals(ja.getInt(1).toString(), "test string");
        o = ja.getInt(2);
        assertTrue(o instanceof JSArray, "expected JSArray, saw " + (o == null ? "null" : o.getClass().getName()));
        assertEquals(((Number)((JSArray)o).getInt(0)).intValue(), 3);
    }

    @Test(groups = {"r2js"})
    public void testRubyHashToJS() {
        RubyHash rh = (RubyHash)r.evalScriptlet("{\"a\" => 1, :b => \"test string\"}");

        Object o = toJS(s, rh);
        assertTrue(o instanceof JSObject, "expected JSObject, saw " + (o == null ? "null" : o.getClass().getName()));
        JSObject jo = (JSObject)(o);

        Object val = jo.get("a");
        assertNotNull(val);
        assertTrue(val instanceof Number, "expected Number, saw " + (val == null ? "null" : val.getClass().getName()));
        assertEquals(((Number)val).intValue(), 1);

        val = jo.get("b");
        assertNotNull(val);
        assertTrue(val instanceof JSString, "expected JSString, saw " + (val == null ? "null" : val.getClass().getName()));
        assertEquals(val.toString(), "test string");
    }

    @Test(groups = {"js2r"})
    public void testNilToRuby() {
        assertEquals(toRuby(s, r, null), r.getNil());
    }

    @Test(groups = {"js2r"})
    public void testOidToRuby() {
        ObjectId oid = new ObjectId("0123456789abcdef01");
        IRubyObject ro = toRuby(s, r, oid);
        assertTrue(ro instanceof RubyString, "expected RubyString, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertEquals(ro.toString(), oid.toString());
    }

    @Test(groups = {"js2r"})
    public void testNumberToRuby() {
        createNumberTest(42, new Byte((byte)42));
        createNumberTest(42, new Short((byte)42));
        createNumberTest(42, new Integer((byte)42));
        createNumberTest(42, new Long((byte)42));
        createNumberTest(42.0, new Float((byte)42));
        createNumberTest(42.0, new Double((byte)42));

        BigInteger bi = new BigInteger("42");
        IRubyObject ro = toRuby(s, r, bi);
        assertTrue(ro instanceof RubyBignum, "expected RubyBignum, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertEquals(((RubyBignum)ro).getValue(), bi);

        BigDecimal bd = new BigDecimal("42");
        ro = toRuby(s, r, bd);
        assertTrue(ro instanceof RubyBigDecimal, "expected RubyBigDecimal, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertEquals(((RubyBigDecimal)ro).getValue(), bd);
    }

    void createNumberTest(long val, Object jobj) {
        IRubyObject ro = toRuby(s, r, jobj);
        assertTrue(ro instanceof RubyNumeric, "expected RubyNumeric, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertEquals(RubyNumeric.num2long(ro), val);
    }

    void createNumberTest(double val, Object jobj) {
        IRubyObject ro = toRuby(s, r, jobj);
        assertTrue(ro instanceof RubyNumeric, "expected RubyNumeric, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertEquals(RubyNumeric.num2dbl(ro), val);
    }

    @Test(groups = {"js2r"})
    public void testJSFunctionToRuby() {
        IRubyObject ro = toRuby(s, r, addSevenFunc, "add_seven"); // null container: add to top-level object
        assertTrue(ro instanceof RubyJSFunctionWrapper, "expected RubyJSFunctionWrapper, saw " + (ro == null ? "null" : ro.getClass().getName()));

        assertEquals(r.evalScriptlet("respond_to?(:add_seven).to_s").toString(), "true");

        IRubyObject result = r.evalScriptlet("add_seven(35)");
        assertTrue(result instanceof RubyNumeric, "expected RubyNumeric, saw " + (result == null ? "null" : result.getClass().getName()));
        assertEquals(RubyNumeric.num2long(result), 42L);
    }

    @Test(groups = {"js2r"})
    public void testJSFunctionInArrayToRuby() {
        JSArray a = new JSArray(new Long(1), new Float(2.3), addSevenFunc);
        RubyArray ra = (RubyArray)toRuby(s, r, a);
        Object o = ra.entry(2);
        assertTrue(o instanceof RubyJSFunctionWrapper, "expected RubyJSFunctionWrapper, saw " + (o == null ? "null" : o.getClass().getName()));
    }

    @Test(groups = {"js2r"})
    public void testToJSObjectRuby() {
        JSObject jso = new JSObjectBase();
        IRubyObject ro = toRuby(s, r, jso);
        assertTrue(ro instanceof RubyJSObjectWrapper, "expected RubyJSObjectWrapper, saw " + (ro == null ? "null" : ro.getClass().getName()));
        assertSame(((RubyJSObjectWrapper)ro).getJSObject(), jso);
    }

    @Test(groups = {"js2r"})
    public void testToJSArrayToRuby() {
        JSArray a = new JSArray(new Long(1), new Float(2.3), new JSString("test string"));
        IRubyObject ro = toRuby(s, r, a);
        assertTrue(ro instanceof RubyArray, "expected RubyArray, saw " + (ro == null ? "null" : ro.getClass().getName()));
        RubyArray ra = (RubyArray)ro;
        assertEquals(ra.size(), 3);
        assertEquals(RubyNumeric.num2long(ra.entry(0)), 1L);
        assertEquals(RubyNumeric.num2dbl(ra.entry(1)), 2.3, 0.0001);
        assertEquals(ra.entry(2).toString(), "test string");
    }

    @Test(groups = {"js2r"})
    public void testJSMapToRuby() {
        // Create JSMap {"a" => 1, 42 => "test string"}
        JSMap map = new JSMap();
        map.set(new JSString("a"), new Integer(1));
        map.set(new Long(42), new JSString("test string"));

        IRubyObject ro = toRuby(s, r, map);
        RubyJSObjectWrapper w = (RubyJSObjectWrapper)ro;
        assertSame(w.getJSObject(), map);
    }

    @Test(groups = {"js2r"})
    public void testJSFunctionWrapperToRuby() {
        // TODO test return of existing proc
        // TODO test return of new proc
    }

    public void testPublicMethodsWrapped() {
        JSObject jso = new JSObjectBase();
        IRubyObject ro = toRuby(s, r, jso);
        assertTrue(ro.respondsTo("getConstructor")); // public method in JSObject
        assertFalse(ro.respondsTo("xyzzy"));
    }
}
