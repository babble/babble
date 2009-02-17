// JSPyIterableObject.java

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

public class JSPyIterableObject extends JSPyObjectWrapper implements Iterable {
    public JSPyIterableObject( PyObject p ){
        super(p);
    }

    public Iterator iterator(){
        return new IterWrapper( _p.asIterable().iterator() );
    }

    class IterWrapper implements Iterator {
        Iterator _iter;
        IterWrapper( Iterator i ){
            _iter = i;
        }
        public Object next(){
            return Python.toJS( _iter.next() );
        }

        public void remove(){
            _iter.remove();
        }

        public boolean hasNext(){
            return _iter.hasNext();
        }
    }
}
