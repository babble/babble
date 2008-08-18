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

import org.testng.annotations.Test;

import org.jruby.RubyNumeric;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.JSFunction;
import ed.js.func.JSFunctionCalls1;
import ed.lang.ruby.RubyJSFunctionWrapper;
import ed.js.engine.Scope;

public class RubyJSFunctionWrapperTest extends ed.TestCase {

    Scope s = new Scope("test", null);
    org.jruby.Ruby r = org.jruby.Ruby.newInstance();
    JSFunction addSevenFunc = new JSFunctionCalls1() {
	    public Object call(Scope scope, Object arg, Object extras[]) {
		return new Integer(((Number)arg).intValue() + 7);
	    }
	};

    @Test(groups = {"basic"})
    public void testCallFromRuby() {
	IRubyObject methodOwner = r.getTopSelf();
	IRubyObject ro = new RubyJSFunctionWrapper(s, r, addSevenFunc, "add_seven", methodOwner.getSingletonClass());

	assertEquals("true", r.evalScriptlet("respond_to?(:add_seven).to_s").toString());

	IRubyObject result = r.evalScriptlet("add_seven(35)");
	assertTrue(result instanceof RubyNumeric);
	assertEquals(42L, RubyNumeric.num2long(result));
    }

    public static void main(String args[]) {
        new RubyJSFunctionWrapperTest().runConsole();
    }
}
