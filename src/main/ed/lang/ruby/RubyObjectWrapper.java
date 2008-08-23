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

import org.jruby.*;
import org.jruby.internal.runtime.methods.JavaMethod;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import static org.jruby.runtime.Visibility.PUBLIC;

import ed.db.ObjectId;
import ed.js.*;
import ed.js.engine.Scope;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and Java
 * objects.
 */
public class RubyObjectWrapper extends RubyObject {

    static final boolean DEBUG = Boolean.getBoolean("DEBUG.RB.WRAP");
  
    protected final Scope _scope;
    protected final org.jruby.Ruby _runtime;
    protected final Object _obj;

    public static IRubyObject toRuby(Scope s, org.jruby.Ruby runtime, Object obj) {
	return toRuby(s, runtime, obj, null, null);
    }

    public static IRubyObject toRuby(Scope s, org.jruby.Ruby runtime, Object obj, String name) {
	return toRuby(s, runtime, obj, name, null);
    }

    /** Given a Java object (JSObject, Number, etc.), return a Ruby object. */
    public static IRubyObject toRuby(Scope s, org.jruby.Ruby runtime, Object obj, String name, RubyObjectWrapper container) {
	if (obj == null)
	    return runtime.getNil();
	if (obj instanceof JSString || obj instanceof ObjectId)
	    return RubyString.newString(runtime, obj.toString());
	if (obj instanceof JSFunction) {
	    IRubyObject methodOwner = container == null ? runtime.getTopSelf() : container;
	    return new RubyJSFunctionWrapper(s, runtime, (JSFunction)obj, name, methodOwner.getSingletonClass());
	}
	if (obj instanceof JSArray) {
	    JSArray ja = (JSArray)obj;
	    int size = ja.size();
	    RubyArray ra = RubyArray.newArray(runtime, size);
	    for (int i = 0; i < size; ++i)
		ra.store(i, toRuby(s, runtime, ja.getInt(i), "__" + i));
	    return ra;
	}
	if (obj instanceof JSMap) {
	    JSMap jm = (JSMap)obj;
	    RubyHash rh = new RubyHash(runtime);
	    ThreadContext context = runtime.getCurrentContext();
	    for (Object key : jm.keys())
		rh.op_aset(context, toRuby(s, runtime, key), toRuby(s, runtime, jm.get(key), key.toString()));
	    return rh;
	}
	if (obj instanceof BigDecimal)
	    return new RubyBigDecimal(runtime, (BigDecimal)obj);
	if (obj instanceof BigInteger)
	    return new RubyBignum(runtime, (BigInteger)obj);
	if (obj instanceof JSObject)
	    return new RubyJSObjectWrapper(s, runtime, (JSObject)obj);
	return JavaUtil.convertJavaToUsableRubyObject(runtime, obj);
    }

    /** Given a Ruby object, returns a JavaScript object. */
    public static Object toJS(org.jruby.Ruby runtime, IRubyObject r) {
	if (r == null)
	    return null;
	if ("MongoObjectId".equals(((RubyObject)r).type().name()))
	    return new ObjectId(r.toString());
	if (r instanceof RubyBignum)
	    return JavaUtil.convertRubyToJava(r, BigInteger.class);
	if (r instanceof RubyBigDecimal)
	    return ((RubyBigDecimal)r).getValue();
	if (r instanceof RubyNumeric)
	    return JavaUtil.convertRubyToJava(r);
	if (r instanceof RubyString)
	    return new JSString(JavaUtil.convertRubyToJava(r).toString());
	if (r instanceof RubyArray) {
	    RubyArray ra = (RubyArray)r;
	    int len = ra.getLength();
	    JSArray ja = new JSArray(len);
	    for (int i = 0; i < len; ++i)
		ja.setInt(i, RubyObjectWrapper.toJS(runtime, ra.entry(i)));
	    return ja;
	}
	if (r instanceof RubyHash) {
	    RubyHash rh = (RubyHash)r;
	    JSMap map = new JSMap();
	    RubyArray keys = rh.keys();
	    int len = keys.getLength();
	    for (int i = 0; i < len; ++i) {
		IRubyObject key = keys.entry(i);
		map.set(JavaUtil.convertRubyToJava(key).toString(), toJS(runtime, rh.op_aref(runtime.getCurrentContext(), key)));
	    }
	    return map;
	}
	return JavaUtil.convertRubyToJava(r); // punt
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
                    if (args.length != 0) Arity.raiseArgumentError(_runtime, args.length, 0, 0);
		    return RubyString.newString(_runtime, _obj.toString());
                }
                @Override public Arity getArity() { return Arity.noArguments(); }
            });
	eigenclass.callMethod(context, "method_added", _runtime.fastNewSymbol(internedName));
    }
}
