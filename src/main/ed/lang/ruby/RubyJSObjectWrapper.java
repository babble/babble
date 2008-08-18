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

import org.jruby.*;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.IdUtil;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.js.JSObject;
import ed.js.JSFunction;
import ed.js.engine.Scope;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and JavaScript
 * objects. An instance of RubyJSObjectWrapper is a Ruby object that turns
 * reads and writes of Ruby instance variables into reads and writes of the
 * underlying JavaScript object's instance variables.
 */
public class RubyJSObjectWrapper extends RubyObjectWrapper {

    private final JSObject _jsobj;

    RubyJSObjectWrapper(Scope s, org.jruby.Ruby runtime, JSObject obj) {
	super(s, runtime, obj);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyJSObjectWrapper");
	_jsobj = (JSObject)_obj;
	_addMethodMissing();
    }

    // Add a method_missing method to the eigenclass that handles method calls
    // and accessors.
    private void _addMethodMissing() {
	final RubyClass eigenclass = getSingletonClass();
	final String name = "method_missing".intern();
	eigenclass.addMethod(name, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    // args[0] is method name symbol, args[1..-1] are arguments
		    String key = JavaUtil.convertRubyToJava(args[0]).toString();
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("method_missing called; symbol = " + key);

		    boolean assign = false;
		    if (key.endsWith("=")) {
			assign = true;
			key = key.substring(0, key.length() - 1);
		    }

		    // If this object does now know about key, call superclass method_missing
		    if (!_jsobj.containsKey(key))
			return RubyObjectWrapper.toRuby(_scope, _runtime, callSuper(context, args, block));

		    // Write
		    if (assign) {
			if (DEBUG)
			    System.err.println("assigning new value " + key);
			_jsobj.set(key, RubyObjectWrapper.toJS(_runtime, args[1]));
			return RubyObjectWrapper.toRuby(_scope, _runtime, JavaUtil.convertRubyToJava(args[1]));
		    }

		    // Read ivar or call function
		    Object obj = _jsobj.get(key);
		    if (obj == null) {
			if (DEBUG)
			    System.err.println("returning instance var value");
			return _runtime.getNil();
		    }
		    if (obj instanceof JSFunction) {
			if (DEBUG)
			    System.err.println("calling function " + key);
			Object[] jargs = new Object[args.length - 1];
			for (int i = 1; i < args.length; ++i)
			    jargs[i-1] = JavaUtil.convertRubyToJava(args[i]);
			return RubyObjectWrapper.toRuby(_scope, _runtime, ((JSFunction)obj).call(_scope, jargs));
		    }
		    if (DEBUG)
			System.err.println("returning instance var value");
		    return RubyObjectWrapper.toRuby(_scope, _runtime, obj);
                }
                @Override public Arity getArity() { return Arity.ONE_REQUIRED; }
            });
	eigenclass.callMethod(_runtime.getCurrentContext(), "method_added", _runtime.fastNewSymbol(name));
    }
}
