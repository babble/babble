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

import ed.lang.ruby.RubyJSArrayWrapper;
import ed.js.*;
import ed.js.func.JSFunctionCalls1;
import ed.js.engine.Scope;

@Test(groups = {"ruby", "ruby.jsarray"})
public class RubyJSArrayWrapperTest {

    Scope s;
    org.jruby.Ruby r;
    JSArray array;
    JSFunction addSevenFunc;
    RubyJSArrayWrapper w;

    @BeforeTest
    public void setUp() {
	s = new Scope("test", null);
	r = org.jruby.Ruby.newInstance();
	addSevenFunc = new JSFunctionCalls1() {
		public Object call(Scope scope, Object arg, Object extras[]) {
		    return new Integer(((Number)arg).intValue() + 7);
		}
	    };

	// FIXME use addSevenFunc when I decide what to do with funcs in arrays
// 	array = new JSArray(new Integer(1), new JSString("test string"), addSevenFunc);
	array = new JSArray(new Integer(1), new JSString("test string"));
	w = new RubyJSArrayWrapper(s, r, array);
	r.getGlobalVariables().set("$a", w);
    }

    public void testBasics() {
	assertSame(w.getJSArray(), array);
    }

    public void testAppend() {
	int len = array.size();
	r.evalScriptlet("$a << 42");
	assertEquals(array.size(), len + 1);
	assertEquals(array.get(2).toString(), "42");
    }

    public void testReplace() {
	int len = array.size();
	r.evalScriptlet("$a[0] = 42");
	assertEquals(array.size(), len);
	assertEquals(array.get(0).toString(), "42");
    }
}
