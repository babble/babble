// JSObjectWrapper.java

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

import java.util.*;

/** @expose */
public class JSObjectWrapper implements JSObject {
    /** Initializes a new wrapper for an object
     * @param wrap The object to wrap
     */
    public JSObjectWrapper( JSObject wrap ){
        _wrap = wrap;
    }

    /** Sets a name/value pair in this object.
     * @param n Name to set
     * @param v Corresponding value
     * @return <tt>v</tt>
     */
    public Object set( Object n , Object v ){
        return _wrap.set( n , v );
    }

    /** Gets a field from this object by a given name.
     * @param n The name of the field fetch
     * @return The field, if found
     */
    public Object get( Object n ){
        return _wrap.get( n );
    }

    /** Add a key/value pair to this object, using a numeric key.
     * @param n Key to use.
     * @param v Value to use.
     * @param v
     */
    public Object setInt( int n , Object v ){
        return _wrap.setInt( n , v );
    }

    /** Get a value from this object whose key is numeric.
     * @param n Key for which to search.
     * @return The corresponding value.
     */
    public Object getInt( int n ){
        return _wrap.getInt( n );
    }

    /** Remove a field with a given name from this object.
     * @param n The name of the field to remove
     * @return The value removed from this object
     */
    public Object removeField( Object n ){
        return _wrap.removeField( n );
    }

    /** Checks if this object contains a field with the given name.
     * @param s Field name for which to check
     * @return if this object contains a field with the given name
     */
    public boolean containsKey( String s ){
        return _wrap.containsKey( s );
    }

    public boolean containsKey( String s , boolean includePrototype ){
        return _wrap.containsKey( s , includePrototype );
    }

    /** Returns this object's fields' names
     * @return The names of the fields in this object
     */
    public Set<String> keySet(){
        return _wrap.keySet();
    }


    public Set<String> keySet( boolean includePrototype ){
        return _wrap.keySet( includePrototype );
    }

    /** Finds the constructor for this object.
     * @return The constructor
     */
    public JSFunction getConstructor(){
        return null;
    }

    /** Gets this object's superclass.
     * @return The superclass
     */
    public JSObject getSuper(){
        return _wrap.getSuper();
    }
    
    public JSFunction getFunction( String name ){
        return _wrap.getFunction( name );
    }

    /** @unexpose */
    final JSObject _wrap;
}
