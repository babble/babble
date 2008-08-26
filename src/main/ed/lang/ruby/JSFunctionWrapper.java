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

import java.util.List;
import java.util.ArrayList;

import org.jruby.RubyProc;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.JSFunction;
import ed.js.engine.Scope;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * RubyJSObjectWrapper acts as a bridge between Ruby blocks and JSFunctions.
 * Ruby blocks can be called as if they are JSFunctions.
 */
public class JSFunctionWrapper extends JSFunction {

    private static final IRubyObject[] TYPE_ARRAY = new IRubyObject[0];

    private Scope _scope;
    private org.jruby.Ruby _runtime;
    private Block _block;

    public JSFunctionWrapper(Scope scope, org.jruby.Ruby runtime, Block block) {
	super(0);
	_scope = scope;
	_runtime = runtime;
	_block = block;
    }

    public Object callBlock(Object ... args) {
	List<IRubyObject> rargs = new ArrayList<IRubyObject>();
	for (Object obj : args)
	    rargs.add(toRuby(_scope, _runtime, obj));
	return toJS(_scope, _runtime, _block.call(_runtime.getCurrentContext(), (IRubyObject[])rargs.toArray(TYPE_ARRAY)));
    }

    public Block getBlock() { return _block; }

    public RubyProc getProcObject() {
	RubyProc p = _block.getProcObject();
	if (p != null)
	    return p;

	p = RubyProc.newProc(_runtime, _block, _block.type);
	_block.setProcObject(p);
	return p;
    }

    public Object call(Scope scope, Object[] extra) { return callBlock(); }

    public Object call(Scope scope, Object p0, Object[] extra) { return callBlock(p0); }

    public Object call(Scope scope, Object p0, Object p1, Object[] extra) { return callBlock(p0, p1); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object[] extra) { return callBlock(p0, p1, p2); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object[] extra) { return callBlock(p0, p1, p2, p3); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object[] extra) { return callBlock(p0, p1, p2, p3, p4); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object p23, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object p23, Object p24, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object p23, Object p24, Object p25, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object p23, Object p24, Object p25, Object p26, Object[] extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26); }

    public Object call(Scope scope, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12, Object p13, Object p14, Object p15, Object p16, Object p17, Object p18, Object p19, Object p20, Object p21, Object p22, Object p23, Object p24, Object p25, Object p26, Object p27, Object ... extra) { return callBlock(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27); }
}
