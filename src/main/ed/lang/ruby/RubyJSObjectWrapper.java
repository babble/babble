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

import java.util.*;

import org.jruby.*;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.callback.Callback;
import org.jruby.util.IdUtil;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.js.*;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and JavaScript
 * objects. An instance of RubyJSObjectWrapper is a Ruby object that turns
 * reads and writes of Ruby instance variables into reads and writes of the
 * underlying JavaScript object's instance variables.
 */
public class RubyJSObjectWrapper extends RubyHash {

    static Map<Ruby, RubyClass> klassDefs = new WeakHashMap<Ruby, RubyClass>();
    static RubyClass jsObjectClass = null;

    protected Scope _scope;
    protected final JSObject _jsobj;
    protected int _size;
    protected RubyClass _eigenclass;
    protected Set<String> _jsIvars;
    protected Set<String> _jsFuncs;

    public static synchronized RubyClass getJSObjectClass(Ruby runtime) {
	RubyClass jsObject = klassDefs.get(runtime);
	if (jsObjectClass == null) {
	    jsObjectClass = runtime.defineClass("JSObject", runtime.getHash(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
	    jsObjectClass.kindOf = new RubyModule.KindOf() {
		    public boolean isKindOf(IRubyObject obj, RubyModule type) {
			return obj instanceof RubyJSObjectWrapper;
		    }
		};
	    klassDefs.put(runtime, jsObjectClass);
	}
	return jsObjectClass;
    }

    RubyJSObjectWrapper(Scope s, Ruby runtime, JSObject obj) {
	this(s, runtime, obj, getJSObjectClass(runtime));
    }

    RubyJSObjectWrapper(Scope s, Ruby runtime, JSObject obj, RubyClass klass) {
	super(runtime, klass);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("creating RubyJSObjectWrapper around " + obj.getClass().getName() + "; ruby class = " + klass.name());
	_scope = s;
	_jsobj = obj;
	_eigenclass = getSingletonClass();
	_jsIvars = new HashSet<String>();
	_jsFuncs = new HashSet<String>();
	_createMethods();
    }

    private void _createMethods() {
	final Ruby runtime = getRuntime();
	final ThreadContext context = runtime.getCurrentContext();

	_eigenclass.alias_method(context, RubyString.newString(runtime, "keySet"), RubyString.newString(runtime, "keys"));
	_eigenclass.alias_method(context, RubyString.newString(runtime, "get"), RubyString.newString(runtime, "[]"));
	_eigenclass.alias_method(context, RubyString.newString(runtime, "set"), RubyString.newString(runtime, "[]="));
	_addMethodMissing();

	Set<String> alreadyDefined = new HashSet<String>();
	alreadyDefined.add("keySet");
	alreadyDefined.add("get");
	alreadyDefined.add("set");

	rebuild();

	alreadyDefined.addAll(_jsIvars);
	alreadyDefined.addAll(_jsFuncs);
	RubyObjectWrapper.addJavaPublicMethodWrappers(_scope, _eigenclass, _jsobj, alreadyDefined);
    }

    /**
     * Scan the key set and (re)create or modify ivars and functions.
     * <p>
     * Right now, we do this the dumb way by deleting all information and
     * re-creating it.
     */
    public void rebuild() {
	for (String name : new HashSet<String>(_jsIvars))
	    _removeInstanceVariable(name);
	for (String name : new HashSet<String>(_jsFuncs))
	    _removeFunctionMethod(name);
	for (final Object key : jsKeySet()) {
	    Object val = _jsobj.get(key);
	    if (val != null) {
		if (isCallableJSFunction(val))
		    _addFunctionMethod(key, (JSFunction)val);
		else
		    _addInstanceVariable(key);
	    }
	}
    }

    public JSObject getJSObject() { return _jsobj; }

    public void visitAll(Visitor visitor) {
	for (Object key : jsKeySet())
	    visitor.visit(toRuby(key), toRuby(_jsobj.get(key)));
    }

    public RubyBoolean respond_to_p(IRubyObject mname) {
	return respond_to_p(mname, getRuntime().getFalse());
    }

    public RubyBoolean respond_to_p(IRubyObject mname, IRubyObject includePrivate) {
	String name = mname.asJavaString().substring(1); // strip off leading ":"
	if (name.endsWith("="))
	    name = name.substring(0, name.length() - 1);
	if (_jsobj.get(name) != null)
	    return getRuntime().getTrue();
	return super.respond_to_p(mname, includePrivate);
    }

    // Superclass implementation is OK
//     public IRubyObject initialize(IRubyObject[] args, final Block block) {
//     }

    // Superclass implementation is OK
//     public IRubyObject default_value_get(ThreadContext context) {
//     }

    // Superclass implementation is OK
//     public IRubyObject default_value_get(ThreadContext context, IRubyObject arg) {
//     }

    // Superclass implementation is OK
//     public IRubyObject default_value_set(final IRubyObject defaultValue) {
//     }

    // Superclass implementation is OK
//     public IRubyObject default_proc() {
//     }

    // Superclass implementation is OK
//     public IRubyObject inspect(ThreadContext context) {
//     }

    public RubyFixnum rb_size() {
	return getRuntime().newFixnum(jsKeySet().size());
    }

    public RubyBoolean empty_p() {
	return jsKeySet().size() == 0 ? getRuntime().getTrue() : getRuntime().getFalse();
    }

    // Superclass implementation is OK
//     public RubyArray to_a() {
//     }

    // Superclass implementation is OK
//     public IRubyObject to_s() {
//     }

    public RubyHash rehash() {
	return this;
    }

    // Superclass implementation is OK
//     public RubyHash to_hash() {
//     }

    public IRubyObject op_aset(ThreadContext context, IRubyObject key, IRubyObject value) {
	Object jsKey = toJS(key);
	Object oldVal = _jsobj.get(jsKey);
	Object newVal = toJS(value);

	_jsobj.set(toJS(key), toJS(value));

	if (oldVal != null) {	// update methods
	    boolean oldIsCallableFunc = isCallableJSFunction(oldVal);
	    boolean newIsCallableFunc = isCallableJSFunction(newVal);
	    if (oldIsCallableFunc && !newIsCallableFunc) {
		_removeFunctionMethod(jsKey);
		_addInstanceVariable(jsKey);
	    }
	    else if (!oldIsCallableFunc && newIsCallableFunc) {
		_removeInstanceVariable(jsKey);
		_addFunctionMethod(jsKey, (JSFunction)newVal);
	    }
	}

	return value;
    }

    public IRubyObject op_aref(ThreadContext context, IRubyObject key) {
	return toRuby(_jsobj.get(toJS(key)));
    }

    public IRubyObject fetch(ThreadContext context, IRubyObject[] args, Block block) {
        if (args.length == 2 && block.isGiven())
            getRuntime().getWarnings().warn(ID.BLOCK_BEATS_DEFAULT_VALUE, "block supersedes default value argument");

        Object value = _jsobj.get(toJS(args[0]));
        if (value == null) {
            if (block.isGiven()) return block.yield(context, args[0]);
            if (args.length == 1) throw getRuntime().newIndexError("key not found");
            return args[1];
        }
        return toRuby(value);
    }

    public RubyBoolean has_key_p(IRubyObject key) {
	return _jsobj.get(toJS(key)) == null ? getRuntime().getFalse() : getRuntime().getTrue();
    }

    public RubyBoolean has_value_p(ThreadContext context, IRubyObject expected) {
	Object o = toJS(expected);
	for (Object key : jsKeySet())
	    if (_jsobj.get(key).equals(o))
		return getRuntime().getTrue();
	return getRuntime().getFalse();
    }

    // Superclass implementation is OK
//     public RubyHash each(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash each_pair(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash each_value(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash each_key(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyArray sort(Block block) {
//     }

    public IRubyObject index(ThreadContext context, IRubyObject expected) {
	Object o = toJS(expected);
	for (Object key : jsKeySet())
	    if (_jsobj.get(key).equals(o))
		return toRuby(key);
	return getRuntime().getNil();
    }

    // Superclass implementation is OK
//     public RubyArray indices(ThreadContext context, IRubyObject[] indices) {
//     }

    // Superclass implementation is OK
//     public RubyArray keys() {
//     }

    // Superclass implementation is OK
//     public RubyArray rb_values() {
//     }

    public IRubyObject op_equal(final ThreadContext context, final IRubyObject other) {
	if (other instanceof RubyJSObjectWrapper && ((RubyJSObjectWrapper)other).getJSObject() == _jsobj)
	    return getRuntime().getTrue();
	return super.op_equal(context, other);
    }

    /**
     * Deletes key/value from _jsobj and returns value.
     */
    protected Object internalDelete(Object jsKey) {
	Object val = _jsobj.get(jsKey);
	_jsobj.removeField(jsKey);
	if (isCallableJSFunction(val))
	    _removeFunctionMethod(jsKey);
	else
	    _removeInstanceVariable(jsKey);
	return val;
    }

    public IRubyObject shift(ThreadContext context) {
	Collection<? extends Object> keys = jsKeySet();
	if (keys.size() > 0) {
	    Object key = keys.iterator().next();
	    IRubyObject rk = toRuby(key);
	    IRubyObject rv = toRuby(_jsobj.get(key)); // grab wrapper before interal delete
	    internalDelete(key);
	    return RubyArray.newArray(getRuntime(), rk, rv);
	}
	return default_value_get(getRuntime().getCurrentContext(), getRuntime().getNil());
    }

    public IRubyObject delete(ThreadContext context, IRubyObject key, Block block) {
	Object k = toJS(key);
	Object v = _jsobj.get(k);
	if (v != null) {
	    IRubyObject rval = toRuby(v); // get wrapper before deleting it
	    internalDelete(k);
	    return rval;
	}

	if (block.isGiven()) return block.yield(context, key);
	return getRuntime().getNil();
    }

    // Superclass implementation is OK
//     public IRubyObject select(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash delete_if(final ThreadContext context, final Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash reject(ThreadContext context, Block block) {
//     }

    public IRubyObject reject_bang(ThreadContext context, Block block) {
        int n = jsKeySet().size();
        delete_if(context, block);
        if (n == jsKeySet().size()) return getRuntime().getNil();
        return this;
    }

    public RubyHash rb_clear() {
	Collection<? extends Object> keys = new ArrayList<Object>(jsKeySet());
	for (Object key : keys) {
	    Object val = _jsobj.get(key);
	    _jsobj.removeField(key);
	    if (isCallableJSFunction(val))
		_removeFunctionMethod(key);
	    else
		_removeInstanceVariable(key);
	}
	return this;
    }

    // Superclass implementation is OK
//     public RubyHash invert(final ThreadContext context) {
//     }

    public RubyHash merge_bang(final ThreadContext context, final IRubyObject other, final Block block) {
        final Ruby runtime = getRuntime();
        final RubyHash otherHash = other.convertToHash();
        final RubyHash self = this;
        otherHash.visitAll(new Visitor() {
            public void visit(IRubyObject key, IRubyObject value) {
                if (block.isGiven()) {
		    Object jsExisting = _jsobj.get(toJS(key));
                    if (jsExisting != null)
                        value = block.yield(context, RubyArray.newArrayNoCopy(runtime, new IRubyObject[]{key, toRuby(jsExisting), value}));
                }
                self.op_aset(context, key, value);
            }
        });

        return this;
    }

    // Superclass implementation is OK
//     public RubyHash merge(ThreadContext context, IRubyObject other, Block block) {
//     }

    // Superclass implementation is OK
//     public RubyHash initialize_copy(ThreadContext context, IRubyObject other) {
//     }

    // Superclass implementation is OK
//     public RubyHash replace(final ThreadContext context, IRubyObject other) {
//     }

    // Superclass implementation is OK
//     public RubyArray values_at(ThreadContext context, IRubyObject[] args) {
//     }

    protected Collection<? extends Object> jsKeySet() {
	try {
	    if (_jsobj instanceof JSMap)
		return (Collection<? extends Object>)((JSMap)_jsobj).keys();
	    else
		return _jsobj.keySet();
	}
	catch (Exception e) {
	    return Collections.emptySet();
	}
    }

    protected IRubyObject toRuby(Object o) {
	return RubyObjectWrapper.toRuby(_scope, getRuntime(), o);
    }

    protected Object toJS(IRubyObject o) { return RubyObjectWrapper.toJS(_scope, o); }

    protected void _addFunctionMethod(final Object key, final JSFunction val) {
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("adding function method " + key);
	_jsFuncs.add(key.toString());
	_eigenclass.defineMethod(key.toString(), new Callback() {
		public IRubyObject execute(IRubyObject recv, IRubyObject[] args, Block block) {
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("calling function " + key);
		    return toRuby(((JSFunction)val).callAndSetThis(_scope, _jsobj, RubyObjectWrapper.toJSFunctionArgs(_scope, getRuntime(), args, 0, block)));
		}
		public Arity getArity() { return Arity.OPTIONAL; }
	    });
    }

    protected void _addInstanceVariable(Object key) {
	String skey = key.toString();
	if (!IdUtil.isValidInstanceVariableName("@" + skey))
	    return;
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("adding ivar " + key);
	_jsIvars.add(skey);

	final IRubyObject rkey = toRuby(key);
	final ThreadContext context = getRuntime().getCurrentContext();
	instance_variable_set(RubyString.newString(getRuntime(), "@" + skey), getRuntime().getNil());
	_eigenclass.defineMethod(skey, new Callback() {
		public IRubyObject execute(IRubyObject recv, IRubyObject[] args, Block block) {
		    return op_aref(context, rkey);
		}
		public Arity getArity() { return Arity.NO_ARGUMENTS; }
	    });
	_eigenclass.defineMethod(skey + "=", new Callback() {
		public IRubyObject execute(IRubyObject recv, IRubyObject[] args, Block block) {
		    return op_aset(context, rkey, args[0]);
		}
		public Arity getArity() { return Arity.ONE_ARGUMENT; }
	    });
    }

    protected void _removeFunctionMethod(Object key) {
	String skey = key.toString();
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("removing function method " + skey);
	_eigenclass.undef(getRuntime().getCurrentContext(), skey);
	_jsFuncs.remove(skey);
    }

    protected void _removeInstanceVariable(Object key) {
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("removing ivar " + key);
	String skey = key.toString();
	ThreadContext context = getRuntime().getCurrentContext();
	_eigenclass.undef(context, skey);
	_eigenclass.undef(context, skey + "=");
	remove_instance_variable(context, RubyString.newString(getRuntime(), "@" + skey), null);
	_jsIvars.remove(skey);
    }

    protected void _addMethodMissing() {
	_eigenclass.addMethod("method_missing", new JavaMethod(_eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
		    // args[0] is method name symbol, args[1..-1] are arguments
		    String key = args[0].toString();
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("RubyJSObjectWrapper.method_missing " + key);
		    if (key.endsWith("=")) {
			if (RubyObjectWrapper.DEBUG)
			    System.err.println("method_missing: turning " + key + " into op_aset call");
			key = key.substring(0, key.length() - 1);
			return op_aset(context, toRuby(key), toRuby(args[1]));
		    }

		    // Look for the thing anyway. It's possible that the
		    // JSObject does not respond to keySet but it still has
		    // something named key.
		    Object val = _jsobj.get(key);
		    if (val == null) {
			if (RubyObjectWrapper.DEBUG)
			    System.err.println("method_missing: did not find value for key " + key + "; calling super.method_missing");
			return RuntimeHelpers.invokeAs(context, _eigenclass.getSuperClass(), RubyJSObjectWrapper.this, "method_missing", args, CallType.SUPER, block);
		    }
		    if (val instanceof JSFunction) {
			if (isCallableJSFunction(val)) {
			    if (RubyObjectWrapper.DEBUG)
				System.err.println("method_missing: found a callable function for key " + key + "; calling it");
			    return toRuby(((JSFunction)val).callAndSetThis(_scope, _jsobj, RubyObjectWrapper.toJSFunctionArgs(_scope, getRuntime(), args, 1, block)));
			}
			else {
			    if (RubyObjectWrapper.DEBUG)
				System.err.println("method_missing: found a non-callable function object for key " + key + "; returning it");
			    return toRuby(val);
			}
		    }
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("method_missing: turning " + key + "; returning it");
		    return toRuby(val);
		}
	    });
    }
}
