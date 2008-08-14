// RubyObjectWrapper.java

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
import ed.js.JSObject;
import ed.js.JSObjectLame;
import ed.js.engine.Scope;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and Java
 * objects.
 */
public class RubyObjectWrapper extends RubyObject {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RBWRAPPER");
  
    protected final Scope _scope;
    protected final org.jruby.Ruby _runtime;
    protected final Object _obj;

    public static IRubyObject create(Scope s, org.jruby.Ruby runtime, Object obj, String name) {
	return create(s, runtime, obj, name, null);
    }

    public static IRubyObject create(Scope s, org.jruby.Ruby runtime, Object obj, String name, RubyObjectWrapper container) {
	if (obj == null)
	    return runtime.getNil();
	if (obj instanceof JSFunction) {
	    IRubyObject methodOwner = container == null ? runtime.getTopSelf() : container;
	    return new RubyJSFunctionWrapper(s, runtime, (JSFunction)obj, name, methodOwner.getSingletonClass());
	}
	if (obj instanceof JSObject)
	    return new RubyJSObjectWrapper(s, runtime, (JSObject)obj);
	return JavaUtil.convertJavaToUsableRubyObject(runtime, obj);
    }

    RubyObjectWrapper(Scope s, org.jruby.Ruby runtime, Object obj) {
	this(s, runtime, obj, true);
    }

    RubyObjectWrapper(Scope s, org.jruby.Ruby runtime, Object obj, boolean createToStringMethod) {
	super(runtime, runtime.getObject());
	_scope = s;
	_runtime = runtime;
	_obj = obj;
	if (createToStringMethod) _addToStringMethod();
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("creating RubyObjectWrapper around " + (obj == null ? "null" : ("instance of " + obj.getClass().getName())));
    }

    private void _addToStringMethod() {
	RubyClass eigenclass = getSingletonClass();
	final ThreadContext context = _runtime.getCurrentContext();
	final String internedName = "to_s".intern();
	eigenclass.addMethod(internedName, new JavaMethod(eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
                    if (args.length != 1) Arity.raiseArgumentError(_runtime, args.length, 0, 0);
                    return JavaUtil.convertJavaToUsableRubyObject(_runtime, _obj.toString());
                }
                @Override public Arity getArity() { return Arity.noArguments(); }
            });
	eigenclass.callMethod(context, "method_added", _runtime.fastNewSymbol(internedName));
    }
}
