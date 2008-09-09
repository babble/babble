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

import ed.js.*;
import ed.js.engine.Scope;

/**
 * RubyJSFunctionWrapper acts as a bridge between Ruby code and JavaScript
 * functions. An instance of RubyJSFunctionWrapper is a Ruby object that can
 * call a JSFunction. If we are given a function name and a class, then a
 * method with that name is added to the class.
 *
 * @see JSFunctionWrapper
 */
public class RubyJSFunctionWrapper extends RubyJSObjectWrapper {

    static RubyClass jsFunctionClass = null;

    private final JSFunction _func;
    private RubyClass _klazz;

    public static synchronized RubyClass getJSFunctionClass(Ruby runtime) {
	if (jsFunctionClass == null) {
	    jsFunctionClass = runtime.defineClass("JSFunction", RubyJSObjectWrapper.getJSObjectClass(runtime), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
	    jsFunctionClass.kindOf = new RubyModule.KindOf() {
		    public boolean isKindOf(IRubyObject obj, RubyModule type) {
			return obj instanceof RubyJSFunctionWrapper;
		    }
		};
	}
	return jsFunctionClass;
    }

    RubyJSFunctionWrapper(Scope s, Ruby runtime, JSFunction obj, String name, RubyClass eigenclass) {
	super(s, runtime, obj, getJSFunctionClass(runtime));
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyJSFunctionWrapper named " + name);
	_func = obj;
	if (name != null && name.length() > 0) {
	    _addMethod(name, eigenclass);
	    if (isRubyClassName(name) && runtime.getClass(name) == null)
		_createJSObjectSubclass(s, name);
	}
    }

    boolean isRubyClassName(String name) {
	return name != null && name.length() > 0 && Character.isUpperCase(name.charAt(0));
    }

    /** Adds this method to <var>eigenclass</var>. */
    protected void _addMethod(String name, RubyClass klazz) {
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("adding method named " + name + " to eigenclass " + klazz.getName());
	final String internedName = name.intern();
	klazz.addMethod(internedName, new JavaMethod(klazz, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("calling method " + clazz.getName() + "." + name + " with " + args.length + " args");
		    int n = _func.getNumParameters();
                    if (args.length != n) Arity.raiseArgumentError(getRuntime(), args.length, n, n);

		    Object result = _func.call(_scope, RubyObjectWrapper.toJSFunctionArgs(_scope, getRuntime(), args, 0, block));
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
		    return toRuby(result);
                }
                @Override public Arity getArity() { return Arity.createArity(_func.getNumParameters()); }
            });
	klazz.callMethod(getRuntime().getCurrentContext(), "method_added", getRuntime().fastNewSymbol(internedName));
    }

    protected void _createJSObjectSubclass(final Scope scope, final String name) {
// FIXME doesn't work quite right yet
// 	if (RubyObjectWrapper.DEBUG)
// 	    System.err.println("adding class named " + name);
// 	_klazz = getRuntime().defineClass(name, RubyJSObjectWrapper.getJSObjectClass(getRuntime()), new ObjectAllocator() {
// 		public IRubyObject allocate(Ruby runtime, RubyClass klass) {
// 		    if (RubyObjectWrapper.DEBUG)
// 			System.err.println("allocating an instance of " + klass.name() + " via a JSFunction named " + name);
// 		    JSObject obj = _func.newOne();
// 		    RubyObject r = (RubyObject)RubyObjectWrapper.toRuby(scope, runtime, obj);
// 		    r.makeMetaClass(_klazz);
// 		    return r;
// 		}
// 	    });
// 	_klazz.kindOf = new RubyModule.KindOf() {
// 		    public boolean isKindOf(IRubyObject obj, RubyModule type) {
// 			return obj instanceof RubyJSObjectWrapper;
// 		    }
// 	    };
// 	_klazz.addMethod("initialize", new JavaMethod(_klazz, PUBLIC) {
//                 public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
// 		    if (RubyObjectWrapper.DEBUG) {
// 			System.err.println("calling method " + clazz.getName() + ".initialize with " + args.length + " args:");
// 			for (IRubyObject iro : args) System.err.println("  " + iro.toString());
// 		    }
// 		    // We re-create the object and return the new instance so
// 		    // that we can pick up the ivars and methods that are
// 		    // defined by the JavaScript constructor function call.
// 		    JSObject obj = _func.newOne(); // re-create a new instance
// 		    _func.callAndSetThis(_scope, obj, RubyObjectWrapper.toJSFunctionArgs(scope, getRuntime(), args, 0, block)); // initialize it by calling _func
// 		    RubyObject r = (RubyObject)RubyObjectWrapper.toRuby(scope, context.getRuntime(), obj);
// 		    r.makeMetaClass(_klazz);

// 		    return r;
//                 }
//                 @Override public Arity getArity() { return Arity.OPTIONAL; }
// 	    });
//     }
}
