// JSDict.java

package ed.js;

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
