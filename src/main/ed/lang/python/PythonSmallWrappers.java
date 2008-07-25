// PythonSmallWrappers.java

package ed.lang.python;

import org.python.core.*;

import ed.js.*;
import ed.db.*;

public class PythonSmallWrappers {
    

    static class PyObjectId extends PyObject {
        PyObjectId( ObjectId id ){
            _id = id;
        }

        public String toString(){
            return _id.toString();
        }
        
        final ObjectId _id;
    }

}
