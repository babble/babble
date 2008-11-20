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
 * @see JSObject
 */
public class JSObjectWrapper implements JSObject {

    private Scope _scope;
    private RubyObject _robj;
    private WrappedRuby _wrappedRuby;

    public JSObjectWrapper(Scope scope, RubyObject robj) {
        _scope = scope;
        _robj = robj;
        _wrappedRuby = new WrappedRuby(robj);
    }

    public RubyObject getRubyObject() { return _robj; }

    protected ThreadContext context() { return _robj.getRuntime().getCurrentContext(); }

    public Object set(Object n, Object v) {
        String skey = n.toString();

        if ("_id".equals(skey) && v instanceof ObjectId) // needed so database can update object's id on save
            _robj.instance_variable_set(_wrappedRuby.ivarName(n), toRuby(_scope, _robj.getRuntime(), v.toString()));
        else if (isCallableJSFunction(v)) {
            toRuby(_scope, _robj.getRuntime(), (JSFunction)v, skey, _robj, this); // attaches method to eigenclass of _robj
        }
        else {
            skey += "=";
            if (_robj.respondsTo(skey))
                return toJS(_scope, _robj.callMethod(context(), skey, new IRubyObject[] {toRuby(_scope, _robj.getRuntime(), v, skey, _robj)}, Block.NULL_BLOCK));
            else if (skey.startsWith("_")) // Assume it's an internal field; let it set the ivar
                return toJS(_scope, _robj.instance_variable_set(_wrappedRuby.ivarName(n), toRuby(_scope, _robj.getRuntime(), v)));
            else
                throw new IllegalArgumentException("no such method: " + skey);
        }
        return v;
    }

    public Object get(Object n) {
        String skey = n.toString();
        if (_robj.respondsTo(skey))
            return toJS(_scope, _robj.callMethod(context(), skey, JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK));
        else if (skey.equals("_id")) {
            IRubyObject val = _robj.instance_variable_get(context(), _wrappedRuby.ivarName(skey));
            return (val == null || val.isNil()) ? toJS(_scope, null) : new ObjectId(val.toString());
        }
        else if (skey.startsWith("_")) // Assume it's an internal field; return the ivar value
            return toJS(_scope, _robj.instance_variable_get(context(), _wrappedRuby.ivarName(skey)));
        else
            return toJS(_scope, null);
    }

    public Object setInt(int n, Object v) {
        return v;
    }

    public Object getInt(int n) {
        return null;
    }

    public Object removeField(Object n) {
        Object o = get(n);
        _wrappedRuby.removeIvarIfExists(n.toString());
        _wrappedRuby.removeMethodIfExists(n.toString());
        return o;
    }

    // TODO ignore top-level (Kernel) methods defined from JavaScript

    public boolean containsKey(String s) {
        return containsKey( s , true );
    }

    public boolean containsKey(String s, boolean includePrototype) {
        return _robj.hasInstanceVariable("@" + s) ||
            _wrappedRuby.respondsToAndIsNotXGen(s);
    }

    public Set<String> keySet() {
        return keySet(true);
    }

    public Set<String> keySet(boolean includePrototype) {
        Set<String> names = new HashSet<String>();

        /* Add each ivar's name iff the ivar has a setter or a getter. */
        for (Variable<IRubyObject> var : _robj.getInstanceVariables().getInstanceVariableList()) {
            String name = var.getName().substring(1); // Strip off leading "@"
            if (_robj.respondsTo(name) || _robj.respondsTo(name + "="))
                names.add(name);
        }

        if (includePrototype)
            for (Object name : ((RubyArray)_robj.methods(context(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY)))
                if (_wrappedRuby.respondsToAndIsNotXGen(name.toString()))
                    names.add(name.toString());
        return names;
    }

    public JSFunction getConstructor() {
        return new JSRubyClassWrapper(_scope, _robj.type());
    }

    /* Can't return super (?) because Ruby super != JavaScript super. */
    public JSObject getSuper() {
        return null;
    }

    public JSFunction getFunction(String name) {
        if (_wrappedRuby.respondsToAndIsNotXGen(name))
            return (JSFunction)toJS(_scope, _robj.method(_robj.getRuntime().newString(name)));
        else
            return null;
    }

    public String toString() {
        return _robj.callMethod(context(), "to_s", JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK).toString();
    }
}
