// PythonSmallWrappers.java

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

import org.python.core.*;

import ed.js.*;
import ed.db.*;

public class PythonSmallWrappers {
    

    public static class PyObjectId extends PyObject {
        PyObjectId( ObjectId id ){
            _id = id;
        }

        public String toString(){
            return _id.toString();
        }

        public PyObject __reduce_ex__(int arg){
            return new PyTuple( PyString.TYPE , new PyTuple( Py.newString( toString() ) ) );
        }

        final ObjectId _id;
    }

}
