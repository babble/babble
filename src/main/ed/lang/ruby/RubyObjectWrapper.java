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
  
    static final Map<Object, IRubyObject> _wrappers = new WeakHashMap<Object, IRubyObject>();

    protected final Scope _scope;
    protected final Object _obj;

    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj) {
	return toRuby(s, runtime, obj, null, null);
    }

    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj, String name) {
	return toRuby(s, runtime, obj, name, null);
    }

    /** Given a Java object (JSObject, Number, etc.), return a Ruby object. */
    public static IRubyObject toRuby(Scope s, Ruby runtime, Object obj, String name, RubyObjectWrapper container) {
	if (obj == null)
	    return runtime.getNil();

	IRubyObject wrapper;
	if ((wrapper = _wrappers.get(obj)) != null) {
	    if (wrapper instanceof RubyJSObjectWrapper)
		((RubyJSObjectWrapper)wrapper).rebuild();
	    return wrapper;
	}

	if (obj instanceof JSFunctionWrapper)
	    return ((JSFunctionWrapper)obj).getProc();
	if (obj instanceof JSObjectWrapper)
	    return ((JSObjectWrapper)obj).getRubyObject();

	if (obj instanceof JSRegex) {
	    JSRegex regex = (JSRegex)obj;
	    String flags = regex.getFlags();
	    int intFlags = 0;
	    if (flags.indexOf('m') >= 0) intFlags |= ReOptions.RE_OPTION_MULTILINE;
	    if (flags.indexOf('i') >= 0) intFlags |= ReOptions.RE_OPTION_IGNORECASE;
	    return RubyRegexp.newRegexp(runtime, regex.getPattern(), intFlags);
	}

	if (obj instanceof JSString || obj instanceof ObjectId)
	    wrapper = RubyString.newString(runtime, obj.toString());
	else if (obj instanceof JSFunction) {
	    IRubyObject methodOwner = container == null ? runtime.getTopSelf() : container;
	    wrapper = new RubyJSFunctionWrapper(s, runtime, (JSFunction)obj, name, methodOwner.getSingletonClass());
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
	else if (obj instanceof JSObject)
	    wrapper = new RubyJSObjectWrapper(s, runtime, (JSObject)obj);
	else
	    wrapper = JavaUtil.convertJavaToUsableRubyObject(runtime, obj);

	_wrappers.put(obj, wrapper);
	return wrapper;
    }

    /** Given a Ruby block, returns a JavaScript object. */
    public static JSFunctionWrapper toJS(Scope scope, Ruby runtime, Block block) {
	return new JSFunctionWrapper(scope, runtime, block);
    }

    /** Given a Ruby object, returns a JavaScript object. */
    public static Object toJS(final Scope scope, IRubyObject r) {
	if (r == null || r.isNil())
	    return null;
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
	if (r instanceof RubyProc) {
	    Object o = new JSFunctionWrapper(scope, r.getRuntime(), ((RubyProc)r).getBlock());
	    _wrappers.put(o, r);
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
	    return new JSRubyClassWrapper(scope, (RubyClass)r);
	}
	if (r instanceof RubyObject) {
	    Object o = new ed.lang.ruby.JSObjectWrapper(scope, (RubyObject)r);
	    _wrappers.put(o, r);
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

    public static void addJavaPublicMethodWrappers(final Scope scope, RubyClass klazz, final JSObject jsobj, Set<String> namesToIgnore) {
	for (final String name : NativeBridge.getPublicMethodNames(jsobj.getClass())) {
	    if (namesToIgnore.contains(name))
		continue;
	    final JSFunction func = NativeBridge.getNativeFunc(jsobj, name);
	    klazz.addMethod(name, new JavaMethod(klazz, PUBLIC) {
		    public IRubyObject call(ThreadContext context, IRubyObject recv, RubyModule klazz, String name, IRubyObject[] args, Block block) {
			Ruby runtime = context.getRuntime();
			try {
			    return toRuby(scope, runtime, func.callAndSetThis(scope, jsobj, RubyObjectWrapper.toJSFunctionArgs(scope, runtime, args, 0, block)));
			}
			catch (Exception e) {
			    recv.callMethod(context, "raise", new IRubyObject[] {RubyString.newString(runtime, e.toString())}, Block.NULL_BLOCK);
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
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("creating RubyObjectWrapper around " + (obj == null ? "null" : ("instance of " + obj.getClass().getName())));
    }

    public Object getObject() { return _obj; }

    public IRubyObject toRuby(Object obj)                                           { return toRuby(_scope, getRuntime(), obj); }
    public IRubyObject toRuby(Object obj, String name)                              { return toRuby(_scope, getRuntime(), obj, name); }
    public IRubyObject toRuby(Object obj, String name, RubyObjectWrapper container) { return toRuby(_scope, getRuntime(), obj, name, container); }

    public JSFunctionWrapper toJS(Block block) { return toJS(_scope, getRuntime(), block); }

    public Object toJS(IRubyObject r) { return toJS(_scope, r); }
}
