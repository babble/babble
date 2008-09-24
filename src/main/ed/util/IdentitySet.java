// IdentitySet.java

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

package ed.util;

import java.util.*;

public class IdentitySet<T> implements Iterable<T> {

    public void add( T t ){
        _map.put( t , "a" );
    }

    public boolean contains( T t ){
        return _map.containsKey( t );
    }

    public void remove( T t ){
        _map.remove( t );
    }

    public void clear(){
	_map.clear();
    }

    public int size(){
	return _map.size();
    }

    public Iterator<T> iterator(){
        return _map.keySet().iterator();
    }

    public void addAll( Collection<T> c ){
        for ( T t : c ){
            add( t );
        }
    }

    final IdentityHashMap<T,String> _map = new IdentityHashMap<T,String>();
}
