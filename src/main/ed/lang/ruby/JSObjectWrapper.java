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
import org.jruby.internal.runtime.methods.DynamicMethod;
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

    private Scope _scope;
    private RubyObject _robj;
    private RubyModule _xgenModule; // cached copy for quick access

    public JSObjectWrapper(Scope scope, RubyObject robj) {
	_scope = scope;
	_robj = robj;
	_xgenModule = RubyJxpSource.xgenModule(_robj.getRuntime());
    }

    public RubyObject getRubyObject() { return _robj; }

    protected ThreadContext context() { return _robj.getRuntime().getCurrentContext(); }

    protected IRubyObject ivarName(Object key) {
	String str = key.toString();
	if (!str.startsWith("@"))
	    str = "@" + str;
	return RubyString.newString(_robj.getRuntime(), str);
    }

    /**
     * Returns <code>true</code> if the metaclass of _robj has a method named
     * <var>name</var> and it is not implemented only in the XGen module. In
     * other words, we want to return <code>true</code> if the method exists
     * but it is not a top-level JavaScript method that was imported early on
     * (either it is not a JS method or it was overridden).
     */
    protected boolean respondsToAndIsNotXGen(String name) {
	if (!_robj.respondsTo(name))
	    return false;
	DynamicMethod dm = _robj.getMetaClass().searchMethod(name);
	return !dm.getImplementationClass().equals(_xgenModule);
    }

    protected void _removeIvarIfExists(String skey) {
	IRubyObject name = ivarName(skey);
	if (_robj.instance_variable_defined_p(context(), name).isTrue())
	    _robj.remove_instance_variable(context(), name, Block.NULL_BLOCK);
	_removeMethodIfExists(skey);
	_removeMethodIfExists(skey + "=");
    }

    protected void _removeMethodIfExists(String skey) {
	if (respondsToAndIsNotXGen(skey)) {
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

	if ("_id".equals(skey) && v instanceof ObjectId) // needed so database can update object's id on save
	    _robj.instance_variable_set(ivarName(n), toRuby(_scope, _robj.getRuntime(), v.toString()));
	else if (isCallableJSFunction(v)) {
	    toRuby(_scope, _robj.getRuntime(), (JSFunction)v, skey, _robj, this); // attaches method to eigenclass of _robj
	}
	else {
	    skey += "=";
	    if (_robj.respondsTo(skey))
		return toJS(_scope, _robj.callMethod(context(), skey, new IRubyObject[] {toRuby(_scope, _robj.getRuntime(), v, skey, _robj)}, Block.NULL_BLOCK));
	    else
		throw new IllegalArgumentException("no such method: " + skey);
	}
	return v;
    }

    public Object get(Object n) {
	String skey = n.toString();
	if (_robj.respondsTo(skey))
	    return toJS(_scope, _robj.callMethod(context(), skey, JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK));
	else
	    throw new IllegalArgumentException("no such method: " + skey);
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

    // TODO ignore top-level (Kernel) methods defined from JavaScript
    public boolean containsKey(String s) {
	return _robj.hasInstanceVariable("@" + s) ||
	    respondsToAndIsNotXGen(s);
    }

    public Collection<String> keySet() {
	return keySet(true);
    }

    public Collection<String> keySet(boolean includePrototype) {
	Set<String> names = new HashSet<String>();

	// Add each ivar's name iff the ivar has a setter or a getter
	for (Variable var : _robj.getInstanceVariables().getInstanceVariableList()) {
	    String name = var.getName().substring(1); // Strip off leading "@"
	    if (_robj.respondsTo(name) || _robj.respondsTo(name + "="))
		names.add(var.getName().substring(1));
	}

	if (includePrototype)
	    for (Object name : ((RubyArray)_robj.methods(context(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY)))
		if (respondsToAndIsNotXGen(name.toString()))
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

    public JSFunction getFunction(String name) {
	if (respondsToAndIsNotXGen(name))
	    return (JSFunction)toJS(_scope, _robj.method(RubyString.newString(_robj.getRuntime(), name)));
	else
	    return null;
    }

    public String toString() {
	return _robj.callMethod(context(), "to_s", JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK).toString();
    }
}
