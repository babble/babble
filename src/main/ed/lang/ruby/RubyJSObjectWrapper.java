// RubyJSObjectWrapper.java

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

import org.jruby.*;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.IdUtil;

import ed.js.JSObject;
import ed.js.engine.Scope;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby objects and JavaScript
 * objects. An instance of RubyJSObjectWrapper is a Ruby object that turns
 * reads and writes of Ruby instance variables into reads and writes of the
 * underlying JavaScript object's instance variables.
 */
public class RubyJSObjectWrapper extends RubyObjectWrapper {

    private final JSObject _jsobj;
    private boolean _initializing;

    RubyJSObjectWrapper(Scope s, org.jruby.Ruby runtime, JSObject obj) {
	super(s, runtime, obj);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyJSObjectWrapper");
	_initializing = true;
	_jsobj = (JSObject)_obj;
	try {
	    for (String key : _jsobj.keySet()) {
		String ivarName = "@" + key;
		if (IdUtil.isValidInstanceVariableName(ivarName)) {
		    if (RubyObjectWrapper.DEBUG)
			System.err.println("  adding ivar named " + ivarName);
		    Object val = _jsobj.get(key);
		    if (val instanceof JSObject) {
			IRubyObject robj = RubyObjectWrapper.create(_scope, _runtime, val, key, this);
			if (!(robj instanceof RubyJSFunctionWrapper))
			    _addIvar(key, robj);
		    }
		    else
			_addIvar(key, JavaUtil.convertJavaToUsableRubyObject(_runtime, val));
		}
	    }
	}
	catch (UnsupportedOperationException e) { }
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  done creating RubyJSObjectWrapper");
	_initializing = false;
    }

    // Add the ivar @key and accessor methods for that ivar
    private void _addIvar(String key, IRubyObject val) {
	RubyClass eigenclass = getSingletonClass();
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("    eigenclass of this object is named " + eigenclass.getName()); // DEBUG
	instance_variable_set(RubySymbol.newSymbol(_runtime, "@" + key), val);
	eigenclass.attr_accessor(_runtime.getCurrentContext(), new IRubyObject[] {RubySymbol.newSymbol(_runtime, key)}); // attr_accessor symbol is "foo" not "@foo"
    }

    protected IRubyObject variableTableStore(String name, IRubyObject value) {
	if (!_initializing)
	    _updateJSObject(name, value, "");
	return super.variableTableStore(name, value);
    }

    protected IRubyObject variableTableFastStore(String internedName, IRubyObject value) {
	if (!_initializing)
	    _updateJSObject(internedName, value, " (fast)");
	return super.variableTableFastStore(internedName, value);
    }

    private void _updateJSObject(String name, IRubyObject value, String storeType) {
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  storing into ivar " + name + storeType + "; value = " + value + "; class of value = " + value.getClass().getName());
	if (_jsobj != null) {
	    String key = name.substring(1);
	    if (_jsobj.containsKey(key)) {
		Object obj = JavaUtil.convertRubyToJava(value);
		_jsobj.set(key, obj);
		if (RubyObjectWrapper.DEBUG)
		    System.err.println("    class of value stored into ivar is " + obj.getClass().getName());
	    }
	}
    }
}
