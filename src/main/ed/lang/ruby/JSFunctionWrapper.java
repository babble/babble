/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.lang.ruby;

import java.util.List;
import java.util.ArrayList;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;

import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;
import static ed.lang.ruby.RubyObjectWrapper.toJS;
import static ed.lang.ruby.RubyObjectWrapper.toRuby;

/**
 * JSFunctionWrapper acts as a bridge between Ruby blocks and JSFunctions.
 * This is a JSFunction that contains and calls a Ruby block.
 *
 * @see RubyJSFunctionWrapper
 */
public class JSFunctionWrapper extends JSFunctionCalls0 {

    static final IRubyObject[] EMPTY_IRUBY_OBJECT_ARRAY = new IRubyObject[0];

    private Scope _scope;
    private Ruby _runtime;
    private Block _block;

    public JSFunctionWrapper(Scope scope, Ruby runtime, Block block) {
        if (RubyObjectWrapper.DEBUG_CREATE)
            System.err.println("wrapping a block in a JSFunctionWrapper");
        _scope = scope;
        _runtime = runtime;
        _block = block;
    }

    public Object callBlock(Scope scope, Object ... args) {
        if (RubyObjectWrapper.DEBUG_FCALL)
            System.err.println("calling Ruby block");
        List<IRubyObject> rargs = new ArrayList<IRubyObject>();
        if (args != null)
            for (Object obj : args)
                rargs.add(toRuby(_scope, _runtime, obj));
        return toJS(scope, _block.call(_runtime.getCurrentContext(), (IRubyObject[])rargs.toArray(EMPTY_IRUBY_OBJECT_ARRAY)));
    }

    public Block getBlock() { return _block; }

    public RubyProc getProc() {
        RubyProc p = _block.getProcObject();
        if (p != null)
            return p;

        p = RubyProc.newProc(_runtime, _block, _block.type);
        _block.setProcObject(p);
        return p;
    }

    public Object call(Scope scope, Object[] extra) { return callBlock(scope, extra); }
}
