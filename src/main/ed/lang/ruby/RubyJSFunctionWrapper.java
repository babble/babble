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
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.js.JSFunction;
import ed.js.engine.Scope;

/**
 * RubyJSFunctionWrapper acts as a bridge between Ruby code and JavaScript
 * functions. An instance of RubyJSFunctionWrapper is a method that is added
 * to the containing object's eigenclass.
 */
public class RubyJSFunctionWrapper extends RubyObjectWrapper {

    private final JSFunction _func;

    RubyJSFunctionWrapper(Scope s, org.jruby.Ruby runtime, JSFunction obj, String name, RubyClass eigenclass) {
	super(s, runtime, obj, false);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyJSFunctionWrapper named " + name);
	_func = (JSFunction)_obj;
	addMethod(name, eigenclass);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  done creating RubyJSFunctionWrapper named " + name);
    }

    /** Adds this method to <var>eigenclass</var>. */
    public void addMethod(String name, RubyClass eigenclass) {
	final String internedName = name.intern();
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("adding method named " + internedName + " to eigenclass " + eigenclass.getName());
	eigenclass.addMethod(internedName, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("calling method " + clazz.getName() + "." + name + " with " + args.length + " args");
		    int n = _func.getNumParameters();
                    if (args.length != n) Arity.raiseArgumentError(_runtime, args.length, n, n);

		    Object result = _func.call(_scope, toJSFunctionArgs(args, block));
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
		    return toRuby(result);
                }
                @Override public Arity getArity() { return Arity.createArity(_func.getNumParameters()); }
            });
	eigenclass.callMethod(_runtime.getCurrentContext(), "method_added", _runtime.fastNewSymbol(internedName));
    }
}
