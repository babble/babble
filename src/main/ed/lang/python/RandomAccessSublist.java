// RandomAccessSublist.java

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

package ed.lang.python;

import java.util.*;

public class RandomAccessSublist extends java.util.AbstractList {
    public RandomAccessSublist( List l , int start , int end ){
        _l = l;
        _start = start;
        _end = end;
    }

    public void add( int index, Object value ){
        _l.add( index+_start , value );
        _end++;
    }

    public Object get( int index ){
        return _l.get( index + _start );
    }

    public Object remove( int index ){
        return _l.remove( index + _start );
    }

    public Object set( int index , Object value ){
        return _l.set( index + _start , value );
    }

    public int size(){
        return _end - _start;
    }

    List _l;
    int _start;
    int _end;
}
