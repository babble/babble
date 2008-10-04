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

package ed.js;

import java.util.Iterator;

public class JSObjectValueIterator implements Iterator<Object> {

    public JSObjectValueIterator(JSObject obj) {
        this._object = obj;
        this._keyIter = obj.keySet().iterator();
    }
    public boolean hasNext() {
        return _keyIter.hasNext();
    }

    public Object next() {
        return _object.get( _keyIter.next() );
    }

    public void remove() {
        _keyIter.remove();
    }

    private final JSObject _object;
    private final Iterator<String> _keyIter;
}
