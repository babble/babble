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

import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;

import ed.db.ObjectId;
import ed.js.JSFunction;
import ed.js.JSObject;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

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

    protected IRubyObject ivarName(Object key) {
	String str = key.toString();
	if (!str.startsWith("@"))
	    str = "@" + str;
	return RubyString.newString(_robj.getRuntime(), str);
    }

    public Object set(Object n, Object v) {
	if (v instanceof ObjectId)
	    v = v.toString();
	_robj.instance_variable_set(ivarName(n), toRuby(_scope, _robj.getRuntime(), v));
	return v;
    }

    public Object get(Object n) {
	IRubyObject ro = _robj.instance_variable_get(_robj.getRuntime().getCurrentContext(), ivarName(n));
	if ("_id".equals(n.toString())) {
	    return new ObjectId(ro.toString());
	}
	return toJS(_scope, ro);
    }

    public Object setInt(int n, Object v) {
	return null;
    }

    public Object getInt(int n) {
	return null;
    }

    public Object removeField(Object n) {
	Object o = get(n);
	_robj.remove_instance_variable(_robj.getRuntime().getCurrentContext(), ivarName(n), Block.NULL_BLOCK);
	return o;
    }

    public boolean containsKey(String s) {
	return _robj.hasInstanceVariable(s); // TODO extend to methods, too?
    }

    public Collection<String> keySet() {
	Set<String> names = new HashSet<String>();
	for (Variable var : _robj.getInstanceVariables().getInstanceVariableList())
	    names.add(var.getName());
	return names;
    }

    public Collection<String> keySet(boolean includePrototype) {
	return keySet();
    }

    public JSFunction getConstructor() {
	return null;
    }

    public JSObject getSuper() {
	return null;
    }

}
