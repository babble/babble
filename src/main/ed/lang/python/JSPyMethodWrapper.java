// JSPyMethodWrapper.java

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

package ed.lang.python;

import java.util.*;

import org.python.core.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

/**
 * Class used when Python sets something in the prototype to be a function.
 *
 * What the Python code has in mind is probably to define a method. Methods
 * get an explicit "self" argument in Python, but in Javascript they can access
 * an implicit "this" object. Of course, you can call the function directly, 
 * through the prototype rather than through an instance..
 * in which case we could decide not to add "this".. or is that too sensible?
 */
public class JSPyMethodWrapper extends JSPyObjectWrapper {
    
    public JSPyMethodWrapper( JSFunction klass , PyFunction o ){
        super( o );
        _f = o;
        _klass = klass;
    }

    public Object call( Scope s , Object [] params ){
        // Check if scope.getThis indicates that this is a method call
        // If direct, don't add to params -- "this" could be anything
        // Maybe it would be JavaScript-ier to just pass this anyhow
        boolean mcall = JSInternalFunctions.JS_instanceof( s.getThis(), _klass );

        return toJS( callPython( s , params , null , mcall ) );
    }

    public JSObject getSuper(){
        return _prototype;
    }
    
    final PyFunction _f;
    final JSFunction _klass;
}
    
