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

package ed.js;

import java.util.*;

import ed.util.MapEntryImpl;


public class JSObjectEntryIterator implements Iterator<Map.Entry<String,Object> > {

    public JSObjectEntryIterator(JSObject obj) {
        this._object = obj;
        this._keyIter = obj.keySet().iterator();
    }
    public boolean hasNext() {
        return _keyIter.hasNext();
    }

    public Map.Entry<String,Object> next() {
        final String key = _keyIter.next();
        final Object value = _object.get( key );
        
        return new MapEntryImpl( key, value );
    }

    public void remove() {
        _keyIter.remove();
    }

    private final JSObject _object;
    private final Iterator<String> _keyIter;
}
