// Box.java

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

/**
 *  Simple wrapper class to make arguments mutable
 * @expose
 */
public class Box<T> {
    /** Initializes a new wrapper for an object.
     * @param t Object to wrap
     */
    public Box( T t ){
        _t = t;
    }

    /** Sets this object to a new value.
     * @param t New value for the object.
     */
    public void set( T t ){
        _t = t;
    }

    /** Returns the object.
     * @return The object
     */
    public T get(){
        return _t;
    }

    private T _t;
}
