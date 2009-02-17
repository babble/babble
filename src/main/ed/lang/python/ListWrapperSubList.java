// ListWrapperSubList.java

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

/**
 * Sublist implementation for arbitrary lists.
 *
 * I'm too lazy to write a list-specific sublist when I can write a sublist
 * that wraps any sublist.
 *
 * Supports the java.util.List API interface by keeping start/end
 * pointers and adjusting on all incoming method calls. This is only
 * efficient if the list we wrap is random access. Since I only use this on
 * JSPySequenceListWrapper, it's fine, but be careful if you use it yourself.
 */
public class ListWrapperSubList extends java.util.AbstractList {
    public ListWrapperSubList( List l , int start , int end ){
        _l = l;
        _start = start;
        _end = end;
    }

    public void add( int index, Object value ){
        _l.add( index+_start , value );
        _end++;
    }

    public Object get( int index ){
        if( index + _start >= _end )
            throw new IndexOutOfBoundsException();
        return _l.get( index + _start );
    }

    public Object remove( int index ){
        // FIXME: decrease _end?
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
