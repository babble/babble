// RubyJSFunctionWrapper.java

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
	_addMethod(name, eigenclass);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  done creating RubyJSFunctionWrapper named " + name);
    }

    /** Adds this method to <var>eigenclass</var>. */
    private void _addMethod(String name, RubyClass eigenclass) {
	final ThreadContext context = _runtime.getCurrentContext();
	final String internedName = name.intern();
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("adding method named " + internedName + " to eigenclass " + eigenclass.getName());
	eigenclass.addMethod(internedName, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("calling method " + clazz.getName() + "." + name + " with " + args.length + " args");
		    int n = _func.getNumParameters();
                    if (args.length != n) Arity.raiseArgumentError(_runtime, args.length, n, n);

		    Object[] jargs = new Object[args.length];
		    for (int i = 0; i < args.length; ++i)
			jargs[0] = JavaUtil.convertRubyToJava(args[0]);

		    Object result = null;
		    switch (n) {
		    case 0:
			result = _func.call(_scope);
			break;
		    case 1:
			result = _func.call(_scope, jargs[0]);
			break;
		    case 2:
			result = _func.call(_scope, jargs[0], jargs[1]);
			break;
		    case 3:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2]);
			break;
		    case 4:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3]);
			break;
		    case 5:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4]);
			break;
		    case 6:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5]);
			break;
		    case 7:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6]);
			break;
		    case 8:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7]);
			break;
		    case 9:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8]);
			break;
		    case 10:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9]);
			break;
		    case 11:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10]);
			break;
		    case 12:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11]);
			break;
		    case 13:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12]);
			break;
		    case 14:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13]);
			break;
		    case 15:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14]);
			break;
		    case 16:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15]);
			break;
		    case 17:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16]);
			break;
		    case 18:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17]);
			break;
		    case 19:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18]);
			break;
		    case 20:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19]);
			break;
		    case 21:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20]);
			break;
		    case 22:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21]);
			break;
		    case 23:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21], jargs[22]);
			break;
		    case 24:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21], jargs[22], jargs[23]);
			break;
		    case 25:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21], jargs[22], jargs[23], jargs[24]);
			break;
		    case 26:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21], jargs[22], jargs[23], jargs[24], jargs[25]);
			break;
		    case 27:
			result = _func.call(_scope, jargs[0], jargs[1], jargs[2], jargs[3], jargs[4], jargs[5], jargs[6], jargs[7], jargs[8], jargs[9], jargs[10], jargs[11], jargs[12], jargs[13], jargs[14], jargs[15], jargs[16], jargs[17], jargs[18], jargs[19], jargs[20], jargs[21], jargs[22], jargs[23], jargs[24], jargs[25], jargs[26]);
			break;
		    }
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("func " + name + " returned " + result + ", which is " + (result == null ? "null" : ("of class " + result.getClass().getName())));
		    return RubyObjectWrapper.create(_scope, _runtime, result, null, null);
                }
                @Override public Arity getArity() { return Arity.createArity(_func.getNumParameters()); }
            });
	eigenclass.callMethod(context, "method_added", _runtime.fastNewSymbol(internedName));
    }
}
