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
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;

import ed.db.ObjectId;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;
import static ed.lang.ruby.RubyObjectWrapper.isCallableJSFunction;

/**
 * JSObjectWrapper acts as a bridge between Ruby objects and JSObjects. This
 * is a JSObject that contains and forwards value changes to a RubyObject.
 *
 * @see RubyJSObjectWrapper
 */
public class JSObjectWrapper implements JSObject {

    private static final IRubyObject[] EMPTY_ARG_LIST = new IRubyObject[0];

    private Scope _scope;
    private RubyObject _robj;

    public JSObjectWrapper(Scope scope, RubyObject robj) {
	_scope = scope;
	_robj = robj;
    }

    public RubyObject getRubyObject() { return _robj; }

    protected ThreadContext context() { return _robj.getRuntime().getCurrentContext(); }

    protected IRubyObject ivarName(Object key) {
	String str = key.toString();
	if (!str.startsWith("@"))
	    str = "@" + str;
	return RubyString.newString(_robj.getRuntime(), str);
    }

    protected void _removeIvarIfExists(String skey) {
	IRubyObject name = ivarName(skey);
	if (_robj.instance_variable_defined_p(context(), name).isTrue())
	    _robj.remove_instance_variable(context(), name, Block.NULL_BLOCK);
	_removeMethodIfExists(skey);
	_removeMethodIfExists(skey + "=");
    }

    protected void _removeMethodIfExists(String skey) {
	if (_robj.respondsTo(skey)) {
	    ThreadContext context = context();
	    IRubyObject[] names = new IRubyObject[] {RubyString.newString(_robj.getRuntime(), skey)};
	    try {
		_robj.getSingletonClass().remove_method(context, names);
	    }
	    catch (Exception e) {
		try {
		    _robj.type().remove_method(context, names);
		}
		catch (Exception e2) {}
	    }
	}
    }

    public Object set(Object n, Object v) {
	String skey = n.toString();

	if ("_id".equals(skey) && v instanceof ObjectId)
	    _robj.instance_variable_set(ivarName(n), toRuby(_scope, _robj.getRuntime(), v.toString()));
	else if (isCallableJSFunction(v)) {
	    _removeIvarIfExists(skey);
	    toRuby(_scope, _robj.getRuntime(), (JSFunction)v, skey, _robj, this);
	}
	else {
	    _removeMethodIfExists(skey);
	    _robj.instance_variable_set(ivarName(n), toRuby(_scope, _robj.getRuntime(), v));
	}
	return v;
    }

    public Object get(Object n) {
	String skey = n.toString();

	IRubyObject ivarName = ivarName(skey);
	if (_robj.instance_variable_defined_p(context(), ivarName).isTrue()) {
	    IRubyObject ro = _robj.instance_variable_get(context(), ivarName);
	    if ("_id".equals(skey) && !ro.isNil())
		return new ObjectId(ro.toString());
	    return toJS(_scope, ro);
	}

	if (_robj.respondsTo(skey)) {
	    RubyMethod m = (RubyMethod)_robj.method(_robj.getRuntime().newSymbol(skey));
	    return new JSFunctionWrapper(_scope, _robj.getRuntime(), ((RubyProc)m.to_proc(context(), Block.NULL_BLOCK)).getBlock());
	}

	return toJS(_scope, null);
    }

    public Object setInt(int n, Object v) {
	if (_robj instanceof RubyArray)
	    ((RubyArray)_robj).aset(_robj.getRuntime().newFixnum(n), toRuby(_scope, _robj.getRuntime(), v));
	return v;
    }

    public Object getInt(int n) {
	return _robj instanceof RubyArray ? toJS(_scope, ((RubyArray)_robj).aref(_robj.getRuntime().newFixnum(n))) : null;
    }

    public Object removeField(Object n) {
	Object o = get(n);
	_removeIvarIfExists(n.toString());
	_removeMethodIfExists(n.toString());
	return o;
    }

    public boolean containsKey(String s) {
	return _robj.hasInstanceVariable("@" + s) ||
	    ((RubyArray)_robj.methods(context(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY)).includes(context(), RubyString.newString(_robj.getRuntime(), s));
    }

    public Collection<String> keySet() {
	return keySet(true);
    }

    public Collection<String> keySet(boolean includePrototype) {
	Set<String> names = new HashSet<String>();
	for (Variable var : _robj.getInstanceVariables().getInstanceVariableList())
	    names.add(var.getName().substring(1));
	if (includePrototype)
	    for (Object name : ((RubyArray)_robj.methods(context(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY)))
		names.add(name.toString());
	return names;
    }

    public JSFunction getConstructor() {
	return new JSRubyClassWrapper(_scope, _robj.type());
    }

    // Can't return super (?) because Ruby super != JavaScript super.
    public JSObject getSuper() {
	return null;
    }

}
