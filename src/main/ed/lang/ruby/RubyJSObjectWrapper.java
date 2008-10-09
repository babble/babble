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

import java.lang.ref.WeakReference;
import java.util.*;

import org.jruby.*;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.IdUtil;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.JSFileLibrary;
import ed.js.*;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and JavaScript
 * objects. An instance of RubyJSObjectWrapper is a Ruby object that turns
 * reads and writes of Ruby instance variables into reads and writes of the
 * underlying JavaScript object's instance variables.
 */
public class RubyJSObjectWrapper extends RubyHash {

    static Map<Ruby, WeakReference<RubyClass>> klassDefs = new WeakHashMap<Ruby, WeakReference<RubyClass>>();
    static RubyClass jsObjectClass = null;

    protected Scope _scope;
    protected final JSObject _jsobj;
    protected int _size;
    protected RubyClass _eigenclass;
    protected Set<String> _jsIvars;
    protected Set<String> _jsFuncs;

    public static synchronized RubyClass getJSObjectClass(Ruby runtime) {
        WeakReference<RubyClass> ref = klassDefs.get(runtime);
        if (ref == null) {
            RubyClass klazz = runtime.defineClass("JSObject", runtime.getHash(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyJSObjectWrapper;
                    }
                };
            klassDefs.put(runtime, ref = new WeakReference<RubyClass>(klazz));
        }
        return ref.get();
    }

    RubyJSObjectWrapper(Scope s, Ruby runtime, JSObject obj) {
        this(s, runtime, obj, getJSObjectClass(runtime));
    }

    RubyJSObjectWrapper(Scope s, Ruby runtime, JSObject obj, RubyClass klass) {
        super(runtime, klass);
        if (RubyObjectWrapper.DEBUG_CREATE)
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

        _eigenclass.alias_method(context, runtime.newString("keySet"), runtime.newString("keys"));
        _eigenclass.alias_method(context, runtime.newString("get"), runtime.newString("[]"));
        _eigenclass.alias_method(context, runtime.newString("set"), runtime.newString("[]="));
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
            Object val = peek(key);
            if (val != null) {
                if (isCallableJSFunction(val))
                    _addFunctionMethod(key, (JSFunction)val);
                else
                    _addInstanceVariable(key);
            }
        }
    }

    public JSObject getJSObject() { return _jsobj; }

    /**
     * Returns _jsobject.get(key).
     * <p>
     * Subclasses may override this for different reasons. For example, {@link
     * RubyJSFileLibraryWrapper} needs to pass an additional argument to get()
     * to make sure the library object is not initialized.
     */
    public Object peek(Object key) { return _jsobj.get(key); }

    public void visitAll(Visitor visitor) {
        Ruby runtime = getRuntime();
        for (Object key : jsKeySet())
            visitor.visit(toRuby(_scope, runtime, key), toRuby(_scope, runtime, _jsobj.get(key), key.toString())); // I think this should be get(), not peek()
    }

    public RubyBoolean respond_to_p(IRubyObject mname) {
        return respond_to_p(mname, getRuntime().getFalse());
    }

    public RubyBoolean respond_to_p(IRubyObject mname, IRubyObject includePrivate) {
        String name = mname.asJavaString().substring(1); // strip off leading ":"
        if (name.endsWith("="))
            name = name.substring(0, name.length() - 1);
        if (peek(name) != null)
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
        Object jsKey = toJS(_scope, key);
        Object oldVal = peek(jsKey);
        Object newVal = toJS(_scope, value);

        _jsobj.set(toJS(_scope, key), toJS(_scope, value));

        if (oldVal != null) {   // update methods
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
        return toRuby(_scope, context.getRuntime(), _jsobj.get(toJS(_scope, key)), key.toString());
    }

    public IRubyObject fetch(ThreadContext context, IRubyObject[] args, Block block) {
        Ruby runtime = context.getRuntime();
        if (args.length == 2 && block.isGiven())
            runtime.getWarnings().warn(ID.BLOCK_BEATS_DEFAULT_VALUE, "block supersedes default value argument");

        Object value = _jsobj.get(toJS(_scope, args[0]));
        if (value == null) {
            if (block.isGiven()) return block.yield(context, args[0]);
            if (args.length == 1) throw runtime.newIndexError("key not found");
            return args[1];
        }
        return toRuby(_scope, runtime, value);
    }

    public RubyBoolean has_key_p(IRubyObject key) {
        return peek(toJS(_scope, key)) == null ? getRuntime().getFalse() : getRuntime().getTrue();
    }

    public RubyBoolean has_value_p(ThreadContext context, IRubyObject expected) {
        Object o = toJS(_scope, expected);
        for (Object key : jsKeySet())
            if (peek(key).equals(o))
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
        Ruby runtime = context.getRuntime();
        Object o = toJS(_scope, expected);
        for (Object key : jsKeySet())
            if (peek(key).equals(o))
                return toRuby(_scope, runtime, key);
        return runtime.getNil();
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
            return context.getRuntime().getTrue();
        return super.op_equal(context, other);
    }

    /**
     * Deletes key/value from _jsobj and returns value.
     */
    protected Object internalDelete(Object jsKey) {
        Object val = peek(jsKey);
        _jsobj.removeField(jsKey);
        if (isCallableJSFunction(val))
            _removeFunctionMethod(jsKey);
        else
            _removeInstanceVariable(jsKey);
        return val;
    }

    public IRubyObject shift(ThreadContext context) {
        Ruby runtime = context.getRuntime();
        Collection<? extends Object> keys = jsKeySet();
        if (keys.size() > 0) {
            Object key = keys.iterator().next();
            IRubyObject rk = toRuby(_scope, runtime, key);
            IRubyObject rv = toRuby(_scope, runtime, _jsobj.get(key), key.toString()); // grab wrapper before interal delete
            internalDelete(key);
            return RubyArray.newArray(runtime, rk, rv);
        }
        return default_value_get(context, runtime.getNil());
    }

    public IRubyObject delete(ThreadContext context, IRubyObject key, Block block) {
        Object k = toJS(_scope, key);
        Object v = peek(k);
        if (v != null) {
            IRubyObject rval = toRuby(_scope, context.getRuntime(), v); // get wrapper before deleting it
            internalDelete(k);
            return rval;
        }

        if (block.isGiven()) return block.yield(context, key);
        return context.getRuntime().getNil();
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
        if (n == jsKeySet().size()) return context.getRuntime().getNil();
        return this;
    }

    public RubyHash rb_clear() {
        Collection<? extends Object> keys = new ArrayList<Object>(jsKeySet());
        for (Object key : keys) {
            Object val = peek(key);
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
                    Object jsExisting = peek(toJS(_scope, key));
                    if (jsExisting != null)
                        value = block.yield(context, RubyArray.newArrayNoCopy(runtime, new IRubyObject[]{key, toRuby(_scope, runtime, jsExisting), value}));
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

    protected void _addFunctionMethod(Object key, final JSFunction val) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("adding function method " + key);
        String skey = key.toString();
        _jsFuncs.add(skey);
        RubyObjectWrapper.createRubyMethod(_scope, getRuntime(), val, skey, _eigenclass, _jsobj);
    }

    protected void _addInstanceVariable(Object key) {
        Ruby runtime = getRuntime();
        String skey = key.toString();
        if (!IdUtil.isValidInstanceVariableName("@" + skey))
            return;
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("adding ivar " + key);
        _jsIvars.add(skey);

        final IRubyObject rkey = toRuby(_scope, runtime, key);
        instance_variable_set(runtime.newString("@" + skey), runtime.getNil());
        _eigenclass.addMethod(skey, new JavaMethod(_eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject recv, RubyModule module, String name, IRubyObject[] args, Block block) {
                    return op_aref(context, rkey);
                }
            });
        _eigenclass.addMethod(skey + "=", new JavaMethod(_eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject recv, RubyModule module, String name, IRubyObject[] args, Block block) {
                    return op_aset(context, rkey, args[0]);
                }
            });
    }

    protected void _removeFunctionMethod(Object key) {
        String skey = key.toString();
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("removing function method " + skey);
        Ruby runtime = getRuntime();
        _eigenclass.undef(runtime.getCurrentContext(), skey);
        _eigenclass.callMethod(runtime.getCurrentContext(), "method_removed", runtime.newSymbol(skey));
        _jsFuncs.remove(skey);
    }

    protected void _removeInstanceVariable(Object key) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("removing ivar " + key);
        String skey = key.toString();
        Ruby runtime = getRuntime();
        ThreadContext context = runtime.getCurrentContext();
        _eigenclass.undef(context, skey);
        _eigenclass.undef(context, skey + "=");
        remove_instance_variable(context, runtime.newString("@" + skey), null);
        _jsIvars.remove(skey);
    }

    protected void _addMethodMissing() {
        _eigenclass.addMethod("method_missing", new JavaMethod(_eigenclass, PUBLIC) {
                public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule module, String name, IRubyObject[] args, Block block) {
                    Ruby runtime = context.getRuntime();
                    // args[0] is method name symbol, args[1..-1] are arguments
                    String key = args[0].toString();
                    if (RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("RubyJSObjectWrapper.method_missing " + key);
                    if (key.endsWith("=")) {
                        if (RubyObjectWrapper.DEBUG_FCALL)
                            System.err.println("method_missing: turning " + key + " into op_aset call");
                        key = key.substring(0, key.length() - 1);
                        return op_aset(context, toRuby(_scope, runtime, key), toRuby(_scope, runtime, args[1], key.toString()));
                    }

                    // Look for the thing anyway. It's possible that the
                    // JSObject does not respond to keySet but it still has
                    // something named key.
                    Object val = peek(key);
                    if (val == null) {
                        if (RubyObjectWrapper.DEBUG_FCALL)
                            System.err.println("method_missing: did not find value for key " + key + "; calling super.method_missing");
                        return RuntimeHelpers.invokeAs(context, _eigenclass.getSuperClass(), RubyJSObjectWrapper.this, "method_missing", args, CallType.SUPER, block);
                    }
                    if (val instanceof JSFunction) {
                        if (isCallableJSFunction(val)) {
                            if (RubyObjectWrapper.DEBUG_FCALL)
                                System.err.println("method_missing: found a callable function for key " + key + "; calling it");
                            try {
                                IRubyObject retval = toRuby(_scope, runtime, ((JSFunction)val).callAndSetThis(_scope, _jsobj, RubyObjectWrapper.toJSFunctionArgs(_scope, runtime, args, 1, block)));
                                if (val instanceof JSFileLibrary)
                                    RubyJxpSource.createNewClasses(_scope, runtime);
                                return retval;
                            }
                            catch (Exception e) {
                                if (RubyObjectWrapper.DEBUG_SEE_EXCEPTIONS) {
                                    System.err.println("saw exception; going to raise Ruby error after printing the stack trace here");
                                    e.printStackTrace();
                                }
                                self.callMethod(context, "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
                            }
                        }
                        else {
                            if (RubyObjectWrapper.DEBUG_FCALL)
                                System.err.println("method_missing: found a non-callable function object for key " + key + "; returning it");
                            return toRuby(_scope, runtime, val);
                        }
                    }
                    if (RubyObjectWrapper.DEBUG_FCALL)
                        System.err.println("method_missing: turning " + key + "; returning it");
                    return toRuby(_scope, runtime, val);
                }
            });
    }
}
