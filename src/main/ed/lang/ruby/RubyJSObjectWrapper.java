/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.ruby;

import java.util.*;

import org.jruby.*;
import org.jruby.anno.JRubyMethod;
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
@SuppressWarnings("serial")
public class RubyJSObjectWrapper extends RubyHash {

    static RubyClass jsObjectClass = null;

    protected Scope _scope;
    protected final JSObject _jsobj;
    protected int _size;
    protected RubyClass _eigenclass;
    protected Set<String> _jsIvars;
    protected Map<String, RubyJSFunctionWrapper> _jsFuncs;

    public static synchronized RubyClass getJSObjectClass(Ruby runtime) {
        RubyClass klazz = runtime.getClass("JSObject");
        if (klazz == null) {
            klazz = runtime.defineClass("JSObject", runtime.getHash(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
            klazz.kindOf = new RubyModule.KindOf() {
                    public boolean isKindOf(IRubyObject obj, RubyModule type) {
                        return obj instanceof RubyJSObjectWrapper;
                    }
                };
            klazz.defineAnnotatedMethods(RubyJSObjectWrapper.class);
        }
        return klazz;
    }

    @SuppressWarnings("unchecked")
    static Collection<? extends Object> jsKeySet(JSObject jsobj) {
        try {
            if (jsobj instanceof JSMap)
                return ((JSMap)jsobj).keys();
            else
                return jsobj.keySet(true);
        }
        catch (Exception e) {
            return Collections.emptySet();
        }
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
        _jsFuncs = new HashMap<String, RubyJSFunctionWrapper>();
        _createMethods();
    }

    private void _createMethods() {
        final Ruby runtime = getRuntime();
        final ThreadContext context = runtime.getCurrentContext();

        _eigenclass.alias_method(context, runtime.newString("keySet"), runtime.newString("keys"));
        _eigenclass.alias_method(context, runtime.newString("get"), runtime.newString("[]"));
        _eigenclass.alias_method(context, runtime.newString("set"), runtime.newString("[]="));

        Set<String> alreadyDefined = new HashSet<String>();
        alreadyDefined.add("keySet");
        alreadyDefined.add("get");
        alreadyDefined.add("set");

        rebuild();

        alreadyDefined.addAll(_jsIvars);
        alreadyDefined.addAll(_jsFuncs.keySet());
        RubyObjectWrapper.addJavaPublicMethodWrappers(_scope, _eigenclass, _jsobj, alreadyDefined);
    }

    /**
     * Scan the key set and (re)create or modify ivars and functions.
     */
    public void rebuild() {
        Set<String> oldIvars = new HashSet<String>(_jsIvars);
        Set<String> oldFuncs = new HashSet<String>(_jsFuncs.keySet());
        Set<String> newIvars = new HashSet<String>();
        Set<String> newFuncs = new HashSet<String>();
        for (Object key : jsKeySet()) {
            String skey = key.toString();
            Object val = peek(key);
            if (val != null) {
                if (isCallableJSFunction(val)) {
                    if (oldIvars.contains(skey))
                        _removeInstanceVariable(skey);
                    if (oldFuncs.contains(skey))
                        _replaceFunctionMethod(key, (JSFunction)val);
                    else
                        _addFunctionMethod(key, (JSFunction)val);
                    newFuncs.add(skey);
                }
                else {
                    if (oldFuncs.contains(skey))
                        _removeFunctionMethod(skey);
                    if (!oldIvars.contains(skey))
                        _addInstanceVariable(key);
                    newIvars.add(skey);
                }
            }
        }
        oldIvars.removeAll(newIvars);
        oldFuncs.removeAll(newFuncs);
        for (String key : oldIvars)
            _removeInstanceVariable(key);
        for (String key : oldFuncs)
            _removeFunctionMethod(key);
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

    public RubyFixnum rb_size() {
        return getRuntime().newFixnum(jsKeySet().size());
    }

    public RubyBoolean empty_p() {
        return jsKeySet().size() == 0 ? getRuntime().getTrue() : getRuntime().getFalse();
    }

    public RubyHash rehash() {
        return this;
    }

    public RubyHash to_hash() {
        final RubyHash rh = new RubyHash(getRuntime());
        final ThreadContext context = getRuntime().getCurrentContext();
        visitAll(new Visitor() {
                public void visit(IRubyObject key, IRubyObject value) { rh.op_aset(context, key, value); }
            });
        return rh;
    }

    public RubyHash convertToHash() { return to_hash(); }

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

    public IRubyObject index(ThreadContext context, IRubyObject expected) {
        Ruby runtime = context.getRuntime();
        Object o = toJS(_scope, expected);
        for (Object key : jsKeySet())
            if (peek(key).equals(o))
                return toRuby(_scope, runtime, key);
        return runtime.getNil();
    }

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

    protected Collection<? extends Object> jsKeySet() {
        return RubyJSObjectWrapper.jsKeySet(_jsobj);
    }

    protected void _addFunctionMethod(Object key, final JSFunction val) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("adding function method " + key);
        String skey = key.toString();
        RubyJSFunctionWrapper wrapper = (RubyJSFunctionWrapper)RubyObjectWrapper.createRubyMethod(_scope, getRuntime(), val, skey, _eigenclass, _jsobj);
        _jsFuncs.put(skey, wrapper);
    }

    protected void _replaceFunctionMethod(Object key, JSFunction val) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("replacing function method " + key);
        String skey = key.toString();
        RubyJSFunctionWrapper wrapper = _jsFuncs.get(skey);
        wrapper.setFunction(val);
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

    @JRubyMethod(name = "method_missing", rest = true, frame = true, module = true, visibility = PUBLIC)
    public static IRubyObject method_missing(ThreadContext context, IRubyObject self, IRubyObject[] args, Block block) {
        if (!(self instanceof RubyJSObjectWrapper))
            return RuntimeHelpers.invokeSuper(context, self, args, block);
        RubyJSObjectWrapper wrapper = (RubyJSObjectWrapper)self;

        Ruby runtime = context.getRuntime();
        /* args[0] is method name symbol, args[1..-1] are arguments. */
        String key = args[0].toString();
        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("RubyJSObjectWrapper.method_missing " + key);
        if (key.endsWith("=")) {
            if (RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("method_missing: turning " + key + " into op_aset call");
            key = key.substring(0, key.length() - 1);
            return wrapper.op_aset(context, toRuby(wrapper._scope, runtime, key), toRuby(wrapper._scope, runtime, args[1], key.toString()));
        }

        /* Look for the thing anyway. It's possible that the
         * JSObject does not respond to keySet but it still has
         * something named key. */
        Object val = wrapper.peek(key);
        if (val == null) {
            if (RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("method_missing: did not find value for key " + key + "; calling super.method_missing");
            return RuntimeHelpers.invokeSuper(context, self, args, block);
        }
        if (val instanceof JSFunction) {
            if (isCallableJSFunction(val)) {
                if (RubyObjectWrapper.DEBUG_FCALL)
                    System.err.println("method_missing: found a callable function for key " + key + "; calling it");
                try {
                    IRubyObject retval = toRuby(wrapper._scope, runtime, ((JSFunction)val).callAndSetThis(wrapper._scope, wrapper._jsobj, RubyObjectWrapper.toJSFunctionArgs(wrapper._scope, runtime, args, 1, block)));
                    if (val instanceof JSFileLibrary)
                        RuntimeEnvironment.createNewClassesAndXGenMethods();
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
                return toRuby(wrapper._scope, runtime, val);
            }
        }
        if (args.length > 1) { // we have function arguments but this is not a function
            if (RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("method_missing: the non-function " + key + " was called with arguments; calling super.method_missing");
            return RuntimeHelpers.invokeSuper(context, self, args, block);
        }
        else {
            if (RubyObjectWrapper.DEBUG_FCALL)
                System.err.println("method_missing: found " + key + "; it is a " + val.getClass().getName() + "; returning it");
            return toRuby(wrapper._scope, runtime, val);
        }
    }
}
