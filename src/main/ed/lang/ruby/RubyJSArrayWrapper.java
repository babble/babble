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

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.JSArray;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * RubyJSArrayWrapper acts as a bridge between Ruby arrays and JSArrays. An
 * instance of RubyJSArrayWrapper is a Ruby object that turns reads and writes
 * of Ruby array contents into reads and writes of the underlying JSArray's
 * instance variables.
 */
public class RubyJSArrayWrapper extends RubyArray {

    private Scope _scope;
    private JSArray _jsarray;

    RubyJSArrayWrapper(Scope s, org.jruby.Ruby runtime, JSArray obj) {
	super(runtime, 0, null);
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("  creating RubyJSArrayWrapper");
	_scope = s;
	_jsarray = obj;
	js2ruby();
    }

    public JSArray getJSArray() { return _jsarray; }

    public IRubyObject initialize(ThreadContext context, IRubyObject[] args, Block block) {
	IRubyObject o = super.initialize(context, args, block);
	ruby2js();
	return o;
    }

    public IRubyObject replace(IRubyObject orig) {
	IRubyObject o = super.replace(orig);
	ruby2js();
	return o;
    }

    public IRubyObject insert(IRubyObject arg1, IRubyObject arg2) {
	IRubyObject o = super.insert(arg1, arg2);
	ruby2js();
	return o;
    }

    public IRubyObject insert(IRubyObject[] args) {
	IRubyObject o = super.insert(args);
	ruby2js();
	return o;
    }

    public RubyArray append(IRubyObject item) {
	RubyArray o = super.append(item);
	_jsarray.add(toJS(_scope, item));
	return o;
    }

    public RubyArray push_m(IRubyObject[] items) {
	RubyArray o = super.push_m(items);
	ruby2js();
	return o;
    }

    public IRubyObject pop() {
	IRubyObject o = super.pop();
	_jsarray.remove(_jsarray.size() - 1);
	return o;
    }

    public IRubyObject shift() {
	IRubyObject o = super.shift();
	_jsarray.remove(0);
	return o;
    }

    public RubyArray unshift_m(IRubyObject[] items) {
	RubyArray o = super.unshift_m(items);
	ruby2js();
	return o;
    }

    public IRubyObject aset(IRubyObject arg0, IRubyObject arg1) {
	IRubyObject o = super.aset(arg0, arg1);
	ruby2js();
	return o;
    }

    public IRubyObject aset(IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
	IRubyObject o = super.aset(arg0, arg1, arg2);
	ruby2js();
	return o;
    }

    public RubyArray concat(IRubyObject obj) {
	RubyArray o = super.concat(obj);
	ruby2js();
	return o;
    }

    public IRubyObject compact_bang() {
	IRubyObject o = super.compact_bang();
	ruby2js();
	return o;
    }

    public IRubyObject rb_clear() {
	IRubyObject o = super.rb_clear();
	_jsarray.clear();
	return o;
    }

    public IRubyObject fill(ThreadContext context, IRubyObject[] args, Block block) {
	IRubyObject o = super.fill(context, args, block);
	ruby2js();
	return o;
    }

    public IRubyObject reverse_bang() {
	IRubyObject o = super.reverse_bang();
	ruby2js();
	return o;
    }

    public RubyArray collect_bang(ThreadContext context, Block block) {
	RubyArray o = super.collect_bang(context, block);
	ruby2js();
	return o;
    }

    public IRubyObject delete(ThreadContext context, IRubyObject item, Block block) {
	IRubyObject o = super.delete(context, item, block);
	ruby2js();
	return o;
    }

    public IRubyObject delete_at(IRubyObject obj) {
	IRubyObject o = super.delete_at(obj);
	ruby2js();
	return o;
    }

    public IRubyObject reject_bang(ThreadContext context, Block block) {
	IRubyObject o = super.reject_bang(context, block);
	ruby2js();
	return o;
    }

    public IRubyObject slice_bang(IRubyObject arg0) {
	IRubyObject o = super.slice_bang(arg0);
	ruby2js();
	return o;
    }

    public IRubyObject slice_bang(IRubyObject arg0, IRubyObject arg1) {
	IRubyObject o = super.slice_bang(arg0, arg1);
	ruby2js();
	return o;
    }

    public IRubyObject flatten_bang(ThreadContext context) {
	IRubyObject o = super.flatten_bang(context);
	ruby2js();
	return o;
    }

    public IRubyObject uniq_bang() {
	IRubyObject o = super.uniq_bang();
	ruby2js();
	return o;
    }

    public RubyArray sort_bang(Block block) {
	RubyArray o = super.sort_bang(block);
	ruby2js();
	return o;
    }

    /** Writes contents of JSArray into RubyArray. */
    private void js2ruby() {
	int len = _jsarray.size();
	IRubyObject[] a = new IRubyObject[len];
	for (int i = 0; i < len; ++i)
	    a[i] = toRuby(_scope, getRuntime(), _jsarray.get(i));
	replace(new RubyArray(getRuntime(), len, a));
    }

    /** Writes contents of RubyArrray into JSArray. */
    private void ruby2js() {
	_jsarray.clear();
	int len = size();
	for (int i = 0; i < len; ++i)
	    _jsarray.add(toJS(_scope, entry(i)));
    }
}
