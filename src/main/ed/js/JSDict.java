// JSDict.java

package ed.js;

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

import java.util.*;

public class JSDict extends JSObjectBase implements Map<String,Object> {

    public JSDict(){
    }
    
    public JSDict( Map m ){
        
        if ( m != null ){
            Set<Map.Entry> entries = m.entrySet();
            for ( Map.Entry e : entries )
                put( e.getKey().toString() , e.getValue() );
        }
        
    }

    public Set<Map.Entry<String,Object>> entrySet(){
        _checkMap();
        return _map.entrySet();
    }

    public Collection<Object> values(){
        _checkMap();
        return _map.values();
    }
    
    public Object remove( Object o ){
        return removeField( o.toString() );
    }

    public boolean containsValue( Object o ){
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty(){
        if ( _map == null )
            return true;
        return _map.isEmpty();
    }

    public int size(){
        if ( _map == null )
            return 0;
        return _map.size();
    }

}
