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

import org.jruby.*;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * JSRubyClassWrapper acts as a bridge between Ruby class objects and
 * JSFunctions. This is a JSFunction that creates Ruby objects of the given
 * class, used for JSObject constructors.
 */
public class JSRubyClassWrapper extends JSFunctionCalls0 {

    private Scope _scope;
    private RubyClass _klazz;

    public JSRubyClassWrapper(Scope scope, RubyClass klazz) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("wrapping the class " + klazz.name() + " in a JSRubyClassWrapper");
        _scope = scope;
        _klazz = klazz;
    }

    public Object call(Scope scope, Object[] extra) { return _scope.getThis(); }

    public JSObject newOne() {
        RubyObject r = (RubyObject)_klazz.newInstance(_klazz.getRuntime().getCurrentContext(), JSFunctionWrapper.EMPTY_IRUBY_OBJECT_ARRAY, Block.NULL_BLOCK);
        return new JSObjectWrapper(_scope, r);
    }
}
