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

    public Object get( Object n ){
        throw _getException();
    }

    public JSFunction getFunction( String name ){
        Object o = get( name );
        if ( o == null )
            return null;
        if ( o instanceof JSFunction )
            return (JSFunction)o;
        // TODO: should this return null or throw an exception?
        return null;
    }

    public final Collection<String> keySet(){
        return keySet( true );
    }

    public Collection<String> keySet( boolean includePrototype ){
        throw _getException();
    }

    public boolean containsKey( String s ){
        throw _getException();
    }

    public Object set( Object n , Object v ){
        throw _getException();
    }

    public Object setInt( int n , Object v ){
        throw _getException();
    }

    public Object getInt( int n ){
        throw _getException();
    }

    public Object removeField( Object n ){
        throw _getException();
    }

    public JSFunction getConstructor(){
        return null;
    }

    public JSObject getSuper(){
        return null;
    }
    
    UnsupportedOperationException _getException(){
        return new UnsupportedOperationException( " from class [" + this.getClass().getName() + "]" );
    }
}
