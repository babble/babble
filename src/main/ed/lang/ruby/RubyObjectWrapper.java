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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.jruby.*;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.JavaUtil;
import org.jruby.parser.ReOptions;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.*;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.appserver.JSFileLibrary;
import ed.db.DBCursor;
import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;
import ed.js.engine.NativeBridge;

/**
 * RubyObjectWrapper acts as a bridge between Ruby objects and Java objects.
 */
public abstract class RubyObjectWrapper extends RubyObject {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB.WRAP");
    static final boolean DEBUG_SEE_EXCEPTIONS = DEBUG || Boolean.getBoolean("DEBUG.RB.EXCEPTIONS");
    static final boolean DEBUG_CREATE = DEBUG || Boolean.getBoolean("DEBUG.RB.CREATE");
    static final boolean DEBUG_FCALL = DEBUG || Boolean.getBoolean("DEBUG.RB.FCALL");
  
    static final Map<Ruby, Map<Object, WeakReference<IRubyObject>>> _wrappers = new WeakHashMap<Ruby, Map<Object, WeakReference<IRubyObject>>>();

    protected final Scope _scope;
    protected final Object _obj;

    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj) {
        return toRuby(s, runtime, obj, null, null, null);
    }

    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj, String name) {
        return toRuby(s, runtime, obj, name, null, null);
    }

    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj, String name, IRubyObject container) {
        return toRuby(s, runtime, obj, name, container, null);
    }

    /** Given a Java object (JSObject, Number, etc.), return a Ruby object. */
    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj, String name, IRubyObject container, JSObject jsThis) {
        if (obj == null)
            return runtime.getNil();
        if (obj instanceof IRubyObject)
            return (IRubyObject)obj;
        if (obj instanceof JSString)
            return runtime.newString(obj.toString());

        IRubyObject wrapper = cachedWrapperFor(runtime, obj);
        if (wrapper != null) {
            if (wrapper instanceof RubyJSObjectWrapper)
                ((RubyJSObjectWrapper)wrapper).rebuild(runtime);
            return wrapper;
        }

        if (obj instanceof JSFunctionWrapper && ((JSFunctionWrapper)obj).getProc().getRuntime() == runtime)
            return ((JSFunctionWrapper)obj).getProc();
        if (obj instanceof JSObjectWrapper && ((JSObjectWrapper)obj).getRubyObject().getRuntime() == runtime)
            return ((JSObjectWrapper)obj).getRubyObject();

        if (obj instanceof JSRegex) {
            JSRegex regex = (JSRegex)obj;
            String flags = regex.getFlags();
            int intFlags = 0;
            if (flags.indexOf('m') >= 0) intFlags |= ReOptions.RE_OPTION_MULTILINE;
            if (flags.indexOf('i') >= 0) intFlags |= ReOptions.RE_OPTION_IGNORECASE;
            return RubyRegexp.newRegexp(runtime, regex.getPattern(), intFlags);
        }

        else if (obj instanceof JSFunction) {
            IRubyObject methodOwner = container == null ? runtime.getTopSelf() : container;
            wrapper = createRubyMethod(s, runtime, (JSFunction)obj, name, methodOwner.getSingletonClass(), jsThis);
        }
        else if (obj instanceof JSArray)
            wrapper = new RubyJSArrayWrapper(s, runtime, (JSArray)obj);
        else if (obj instanceof DBCursor)
            wrapper = new RubyDBCursorWrapper(s, runtime, (DBCursor)obj);
        else if (obj instanceof BigDecimal)
            wrapper = new RubyBigDecimal(runtime, (BigDecimal)obj);
        else if (obj instanceof BigInteger)
            wrapper = new RubyBignum(runtime, (BigInteger)obj);
        else if (obj instanceof Scope)
            wrapper = new RubyScopeWrapper(s, runtime, (Scope)obj);
        else if (obj instanceof ObjectId)
            wrapper = new RubyObjectIdWrapper(runtime, (ObjectId)obj);
        else if (obj instanceof JSObject)
            wrapper = new RubyJSObjectWrapper(s, runtime, (JSObject)obj);
        else
            wrapper = JavaUtil.convertJavaToUsableRubyObject(runtime, obj);

        cacheWrapper(runtime, obj, wrapper);
        return wrapper;
    }

    /**
     * Note: does not return cached wrapper or cache the returned wrapper.
     * Caller is responsible for doing so if desired. Sometimes it's not,
     * which is why this method is separate from (and is called from)
     * toRuby().
     */
    public static IRubyObject createRubyMethod(Scope s, Ruby runtime, JSFunction func, String name, RubyModule attachTo, JSObject jsThis) {
        if (func instanceof JSFileLibrary)
            return new RubyJSFileLibraryWrapper(s, runtime, (JSFileLibrary)func, name, attachTo, jsThis);
        else
            return new RubyJSFunctionWrapper(s, runtime, func, name, attachTo, jsThis);
    }

    protected static synchronized IRubyObject cachedWrapperFor(Ruby runtime, Object obj) {
        Map<Object, WeakReference<IRubyObject>> runtimeWrappers = _wrappers.get(runtime);
        if (runtimeWrappers == null)
            return null;
        WeakReference<IRubyObject> ref = runtimeWrappers.get(obj);
        return ref == null ? null : ref.get();
    }

    protected static synchronized void cacheWrapper(Ruby runtime, Object obj, IRubyObject wrapper) {
        Map<Object, WeakReference<IRubyObject>> runtimeWrappers = _wrappers.get(runtime);
        if (runtimeWrappers == null) {
            runtimeWrappers = new WeakHashMap<Object, WeakReference<IRubyObject>>();
            _wrappers.put(runtime, runtimeWrappers);
        }
        runtimeWrappers.put(obj, new WeakReference<IRubyObject>(wrapper));
    }

    /** Given a Ruby block, returns a JavaScript object. */
    public static JSFunctionWrapper toJS(Scope scope, Ruby runtime, Block block) {
        return new JSFunctionWrapper(scope, runtime, block);
    }

    /** Given a Ruby object, returns a JavaScript object. */
    public static Object toJS(final Scope scope, IRubyObject r) {
        if (r == null || r.isNil())
            return null;
        if (r instanceof JSObject)
            return r;
        if (r instanceof RubyString)
            return new JSString(((RubyString)r).toString());
        if (r instanceof RubyObjectWrapper)
            return ((RubyObjectWrapper)r)._obj;
        if (r instanceof RubyBignum)
            return JavaUtil.convertRubyToJava(r, BigInteger.class);
        if (r instanceof RubyBigDecimal)
            return ((RubyBigDecimal)r).getValue();
        if (r instanceof RubyNumeric)
            return JavaUtil.convertRubyToJava(r);
        if (r instanceof RubyJSObjectWrapper)
            return ((RubyJSObjectWrapper)r).getJSObject();
        if (r instanceof RubyJSArrayWrapper)
            return ((RubyJSArrayWrapper)r).getJSArray();
        if (r instanceof RubyObjectIdWrapper)
            return ((RubyObjectIdWrapper)r).getObjectId();
        if (r instanceof RubyArray) {
            RubyArray ra = (RubyArray)r;
            int len = ra.getLength();
            JSArray ja = new JSArray(len);
            for (int i = 0; i < len; ++i)
                ja.setInt(i, toJS(scope, ra.entry(i)));
            return ja;
        }
        if (r instanceof RubyHash) {
            RubyHash rh = (RubyHash)r;
            final JSObjectBase jobj = new JSObjectBase();
            rh.visitAll(new RubyHash.Visitor() {
                    public void visit(final IRubyObject key, final IRubyObject value) {
                        jobj.set(key.toString(), toJS(scope, value));
                    }
                });
            return jobj;
        }
        if (r instanceof RubyStruct) {
            RubyStruct rs = (RubyStruct)r;
            final JSObjectBase jobj = new JSObjectBase();
            IRubyObject[] ja = rs.members().toJavaArray();
            for (int i = 0; i < ja.length; ++i)
                jobj.set(ja[i].toString(), toJS(scope, rs.get(i)));
            return jobj;
        }
        if (r instanceof RubyProc || r instanceof RubyMethod) {
            RubyProc p = (r instanceof RubyProc) ? (RubyProc)r : (RubyProc)((RubyMethod)r).to_proc(r.getRuntime().getCurrentContext(), Block.NULL_BLOCK);
            Object o = new JSFunctionWrapper(scope, r.getRuntime(), p.getBlock());
            cacheWrapper(r.getRuntime(), o, r);
            return o;
        }
        if (r instanceof RubyRegexp) {
            RubyRegexp regex = (RubyRegexp)r;
            // Ruby regex.to_s returns "(?i-mx:foobar)", where the first part
            // contains the flags. Everything after the minus is a flag that
            // is off.
            String options = regex.to_s().toString().substring(2);
            options = options.substring(0, options.indexOf(':'));
            if (options.indexOf('-') >= 0)
                options = options.substring(0, options.indexOf('-'));
            return new JSRegex(regex.source().toString(), options);
        }
        if (r instanceof RubyClass) {
            Object o = new JSRubyClassWrapper(scope, (RubyClass)r);
            cacheWrapper(r.getRuntime(), o, r);
            return o;
        }
        if (r instanceof RubyObject) {
            Object o = new ed.lang.ruby.JSObjectWrapper(scope, (RubyObject)r);
            cacheWrapper(r.getRuntime(), o, r);
            return o;
        }
        else
            return JavaUtil.convertRubyToJava(r); // punt
    }

    public static Object[] toJSFunctionArgs(Scope s, Ruby r, IRubyObject[] args, int offset, Block block) {
        boolean haveBlock = block != null && block.isGiven();
        Object[] jargs = new Object[args.length - offset + (haveBlock ? 1 : 0)];
        for (int i = offset; i < args.length; ++i)
            jargs[i-offset] = RubyObjectWrapper.toJS(s, args[i]);
        if (haveBlock)
            jargs[args.length-offset] = RubyObjectWrapper.toJS(s, r, block);
        return jargs;
    }

    public static void addJavaPublicMethodWrappers(final Scope scope, RubyModule module, final JSObject jsobj, Set<String> namesToIgnore) {
        for (final String name : NativeBridge.getPublicMethodNames(jsobj.getClass())) {
            if (namesToIgnore.contains(name))
                continue;
            final JSFunction func = NativeBridge.getNativeFunc(jsobj, name);
            module.addMethod(name, new JavaMethod(module, PUBLIC) {
                    public IRubyObject call(ThreadContext context, IRubyObject recv, RubyModule module, String name, IRubyObject[] args, Block block) {
                        Ruby runtime = context.getRuntime();
                        try {
                            return toRuby(scope, runtime, func.callAndSetThis(scope, jsobj, RubyObjectWrapper.toJSFunctionArgs(scope, runtime, args, 0, block)));
                        }
                        catch (Exception e) {
                            if (DEBUG_SEE_EXCEPTIONS) {
                                System.err.println("saw exception; going to raise Ruby error after printing the stack trace here");
                                e.printStackTrace();
                            }
                            recv.callMethod(context, "raise", new IRubyObject[] {runtime.newString(e.toString())}, Block.NULL_BLOCK);
                            return runtime.getNil(); // will never reach
                        }
                    }
                });
        }
    }

    public static boolean isCallableJSFunction(Object o) {
        return (o instanceof JSFunction) && ((JSFunction)o).isCallable();
    }

    RubyObjectWrapper(Scope s, Ruby runtime, Object obj) {
        super(runtime, runtime.getObject());
        _scope = s;
        _obj = obj;
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("creating RubyObjectWrapper around " + (obj == null ? "null" : ("instance of " + obj.getClass().getName())));
    }

    public Object getObject() { return _obj; }

    public IRubyObject toRuby(Object obj)                                           { return toRuby(_scope, getRuntime(), obj); }
    public IRubyObject toRuby(Object obj, String name)                              { return toRuby(_scope, getRuntime(), obj, name); }
    public IRubyObject toRuby(Object obj, String name, RubyObjectWrapper container) { return toRuby(_scope, getRuntime(), obj, name, container); }

    public JSFunctionWrapper toJS(Block block) { return toJS(_scope, getRuntime(), block); }

    public Object toJS(IRubyObject r) { return toJS(_scope, r); }
}
