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

import ed.lang.ruby.RubyJSFunctionWrapper;
import ed.js.*;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.Scope;

@Test(groups = {"ruby", "ruby.jsfunc"})
public class RubyJSFunctionWrapperTest extends SourceRunner {

    JSFunction addSevenFunc;

    @BeforeMethod(groups={"ruby", "rjby.jsfunc"})
    public void setUp() {
	super.setUp();
	Object o = runJS("MyJSClass = function(foo) {\n" +
	      "  this.foo = foo;\n" +
	      "}\n" +
	      "MyJSClass.prototype.reverse = function() {\n" +
	      "  return this.foo.reverse();\n" +
	      "}\n" +
	      "return MyJSClass;");
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };
    }

    public void testClass() {
	RubyClass klazz = new RubyJSFunctionWrapper(s, r, addSevenFunc, null, null).type();
	assertEquals(klazz.name().toString(), "JSFunction");
	assertEquals(((RubyClass)klazz.superclass(r.getCurrentContext())).name().toString(), "JSObject");
    }

    public void testClassConstantCreated() {
	try {
	    r.getClassFromPath("MyJSClass");
	    fail("MyJSClass should not already be defined");
	}
	catch (org.jruby.exceptions.RaiseException e) {
	    assertTrue(true);
	}

	runRuby("# gotta run something for the class to be created");

	try {
	    RubyModule mo = r.getClassFromPath("MyJSClass");
	    assertNotNull(mo, "getClassFromPath returned null");
	}
	catch (org.jruby.exceptions.RaiseException e) {
	    fail("MyJSClass should be defined; getClassFromPath threw exception " + e);
	}
    }

    public void testClassHierarchy() {
	runRuby("# gotta run something for the class to be created");
	RubyModule mo = r.getClassFromPath("MyJSClass");
	assertTrue(mo instanceof RubyClass);
	RubyClass c = (RubyClass)mo;
	RubyClass superClass = (RubyClass)c.superclass(r.getCurrentContext());
	assertNotNull(superClass);
	assertEquals(superClass.name().toString(), "JSObject");
    }

    public void testClassHierarchyInRuby() {
	assertRubyEquals("x = MyJSClass.new('bar'); puts x.class.name; puts x.class.superclass.name", "MyJSClass\nJSObject");
    }

    public void testJSClassExists() {
	runRuby("# gotta run something for the class to be created");
	RubyModule m = r.getClassFromPath("MyJSClass");
	assertNotNull(m);
	assertEquals(runRuby("MyJSClass"), m);
    }

    public void testConstruction() {
	Object o = runRuby("MyJSClass.new('bar')");
	assertNotNull(o);
	assertTrue(o instanceof ed.lang.ruby.RubyJSObjectWrapper);
	RubyJSObjectWrapper wrapper = (RubyJSObjectWrapper)o;
	assertEquals(wrapper.type().name().toString(), "MyJSClass");
	assertEquals(wrapper.type().getSuperClass().name().toString(), "JSObject");

	Object foo = wrapper.getJSObject().get("foo");
	assertEquals(foo.toString(), "bar");
	assertTrue(foo instanceof JSString);

	Object reverse = wrapper.getJSObject().get("reverse");
	assertTrue(reverse instanceof JSFunction);

	assertNotNull(r.getModule("MyJSClass"));
    }

    public void testClassNameInRuby() {
        assertRubyEquals("puts MyJSClass.new('bar').class.name", "MyJSClass");
    }

    public void testAccessNewIvar() {
	assertRubyEquals("x = MyJSClass.new('bar'); puts x['foo']", "bar");
	// NOTE: though we can access foo with x['foo'] we can not access it
	// with x.foo because the JavaScript object does not return "foo" as a
	// member of its keySet.
    }

    public void testModifyJSObject() {
	// We can't call x.foo because foo is not in the object's keySet
	Object o = runRuby("x = MyJSClass.new('bar'); x['foo'] = 'new_value'; x");
	RubyJSObjectWrapper wrapper = (RubyJSObjectWrapper)o;
	Object foo = wrapper.getJSObject().get("foo");
	assertEquals(foo.toString(), "new_value");
	assertTrue(foo instanceof JSString);
    }

    public void testCallJSObjectFunction() {
	assertRubyEquals("puts MyJSClass.new('bar').reverse()", "rab");
    }
}
