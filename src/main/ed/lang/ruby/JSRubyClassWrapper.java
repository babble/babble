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

import org.jruby.*;
import org.jruby.runtime.Block;

import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls0;

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
