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

import java.util.Collection;

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
	_overrideInstanceVariables();
	_overridePublicMethods();
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

		    // Write
		    if (key.endsWith("=")) {
			key = key.substring(0, key.length() - 1);
			if (DEBUG)
			    System.err.println("assigning new value to instance var named " + key);
			_jsobj.set(key, RubyObjectWrapper.toJS(_runtime, args[1]));
			return RubyObjectWrapper.toRuby(_scope, _runtime, JavaUtil.convertRubyToJava(args[1]));
		    }

		    Object obj = _jsobj.get(key);

		    // Call function
		    if (obj instanceof JSFunction) {
			if (DEBUG)
			    System.err.println("calling function " + key);
			Object[] jargs = new Object[args.length - 1];
			for (int i = 1; i < args.length; ++i)
			    jargs[i-1] = toJS(_runtime, args[i]);
			return RubyObjectWrapper.toRuby(_scope, _runtime, ((JSFunction)obj).call(_scope, jargs));
		    }

		    // Read ivar
		    if (DEBUG)
			System.err.println("returning value of instance var named " + key);
		    return (obj == null) ? _runtime.getNil() : RubyObjectWrapper.toRuby(_scope, _runtime, obj);
                }

                @Override public Arity getArity() { return Arity.ONE_REQUIRED; }
            });
	eigenclass.callMethod(_runtime.getCurrentContext(), "method_added", _runtime.fastNewSymbol(name));
    }

    private void _overrideInstanceVariables() {
	final RubyClass eigenclass = getSingletonClass();
	final String name = "instance_variables".intern();
	eigenclass.addMethod(name, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    Collection<String> keys = _jsobj.keySet();
		    int size = keys.size();
		    RubyArray ra = RubyArray.newArray(_runtime, size);
		    int i = 0;
		    for (String key : keys)
			if (!(_jsobj.get(key) instanceof JSFunction))
			    ra.store(i++, _runtime.fastNewSymbol(("@" + key).intern()));
		    return ra;
		}
	    });
	eigenclass.callMethod(_runtime.getCurrentContext(), "method_added", _runtime.fastNewSymbol(name));
    }

    private void _overridePublicMethods() {
	final RubyClass eigenclass = getSingletonClass();
	final String name = "public_methods".intern();
	eigenclass.addMethod(name, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    RubyArray superMethods = (RubyArray)((RubyObject)self).public_methods(context, new IRubyObject[0]);
		    Collection<String> keys = _jsobj.keySet();
		    int size = keys.size();
		    RubyArray ra = RubyArray.newArray(_runtime, size + superMethods.size());
		    int i = 0;
		    for (String key : keys) {
			ra.store(i++, _runtime.fastNewSymbol((key).intern()));
			if (!(_jsobj.get(key) instanceof JSFunction))
			    ra.store(i++, _runtime.fastNewSymbol((key + "=").intern()));
		    }
		    for (Object o : superMethods)
			ra.store(i++, _runtime.fastNewSymbol(o.toString().intern()));
		    return ra;
		}
	    });
	eigenclass.callMethod(_runtime.getCurrentContext(), "method_added", _runtime.fastNewSymbol(name));
    }
}
