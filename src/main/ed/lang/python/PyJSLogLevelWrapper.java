// PyJSLogLevelWrapper.java

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
import org.python.expose.*;
import org.python.expose.generate.*;

import ed.js.*;
import ed.log.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

@ExposedType(name = "log_level")
public class PyJSLogLevelWrapper extends PyString {

    public PyJSLogLevelWrapper( Level level ){
        super(level.toString());
        _level = level;
    }

    /*    @ExposedMethod
    public final boolean jswrapper___nonzero__(){
        return _js.keySet().size() > 0;
    }

    public boolean __nonzero__(){
        return jswrapper___nonzero__();
        }*/

    public String toString(){
        return "[wrapper for " + _level + "]";
    }

    @ExposedMethod(names = {"__repr__", "__str__"})
    public String log_level___repr__(){
        return _level.toString();
    }

    final Level _level;

}
