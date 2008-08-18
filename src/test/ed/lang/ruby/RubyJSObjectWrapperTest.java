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

import ed.lang.ruby.RubyObjectWrapper;
import ed.lang.ruby.RubyJSObjectWrapper;
import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.*;

public class RubyJSObjectWrapperTest extends ed.TestCase {

    Scope s = new Scope("test", null);
    org.jruby.Ruby r = org.jruby.Ruby.newInstance();

    @Test(groups = {"basic", "r2js"})
    public void testAccessors() {
	JSObjectBase jobj = new JSObjectBase();
	jobj.set("count", new Integer(1));

	IRubyObject ro = toRuby(s, r, jobj);
	System.err.println("ro = " + ro.getClass().getName()); // DEBUG
	assertTrue(ro instanceof RubyJSObjectWrapper);

	r.getGlobalVariables().set("$jobj", ro);
	assertEquals(1L, RubyNumeric.num2long(r.evalScriptlet("$jobj.count")));
	assertEquals(3L, RubyNumeric.num2long(r.evalScriptlet("$jobj.count += 2; $jobj.count")));
    }

    public static void main(String args[]) {
        new RubyJSObjectWrapperTest().runConsole();
    }
}
