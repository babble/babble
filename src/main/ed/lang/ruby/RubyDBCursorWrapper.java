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

// import java.util.Collection;
// import java.util.Collections;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
// import org.jruby.internal.runtime.methods.JavaMethod;
// import org.jruby.javasupport.JavaUtil;
// import org.jruby.runtime.*;
// import org.jruby.runtime.builtin.IRubyObject;
// import org.jruby.util.IdUtil;
// import static org.jruby.runtime.Visibility.PUBLIC;

import ed.db.DBCursor;
// import ed.js.JSObject;
import ed.js.JSFunction;
import ed.js.engine.Scope;

/**
 * RubyDBCursorWrapper is a RubyJSObjectWrapper that implements Enumerable.
 */
public class RubyDBCursorWrapper extends RubyJSObjectWrapper {

    RubyDBCursorWrapper(Scope s, org.jruby.Ruby runtime, DBCursor obj) {
	super(s, runtime, obj);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyDBCursorWrapper");

	RubyClass eigenclass = getSingletonClass();
	eigenclass.includeModule(runtime.getEnumerable());
	eigenclass.defineAnnotatedMethods(RubyDBCursorWrapper.class);
    }

    @JRubyMethod(name = "each", frame = true)
    public IRubyObject each(ThreadContext context, Block block) {
	JSFunction blockFunc = toJS(_scope, _runtime, block);
	JSFunction forEachFunc = (JSFunction)_jsobj.get("forEach");
	_scope.setThis(_jsobj);
	forEachFunc.call(_scope, blockFunc, null);
	_scope.clearThisNormal(null);
        return this;
    }
}
