// JSObjectLame.java

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
public class JSObjectLame implements JSObject {

    /** @unexpose */
    public Object get( Object n ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Collection<String> keySet(){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public boolean containsKey( String s ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object set( Object n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object setInt( int n , Object v ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object getInt( int n ){
        throw new UnsupportedOperationException();
    }

    /** @unexpose */
    public Object removeField( Object n ){
        throw new UnsupportedOperationException();
    }

    /** Finds the constructor for this object.
     * @return null
     */
    public JSFunction getConstructor(){
        return null;
    }

    /** Gets this object's superclass.
     * @return null
     */
    public JSObject getSuper(){
        return null;
    }
}
