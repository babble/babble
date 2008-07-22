// JSMap.java

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

import ed.util.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

import java.util.*;

/** See the wiki page describing <a href="http://www.10gen.com/wiki/ref.Map">Map</a>.
 * @expose */
public class JSMap extends JSObjectBase {

    /** @unexpose  */
    public final static JSFunction _cons = new JSFunctionCalls0(){

            public JSObject newOne(){
                return new JSMap();
            }

            public Object call( Scope scope , Object[] extra ){
                return new JSMap();
            }
        };

    /** Adds a name/value pair to this map
     * @param n The key
     * @param v The value
     * @return The value
     */
    public Object set( Object n , Object v ){
        _map.put( n , v );
        return v;
    }

    /** Get an object from this map by a given name.
     * @param n The name of the object to fetch
     * @return The object, if found
     */
    public Object get( Object n ){
        Object foo = _map.get( n );
        if ( foo != null )
            return foo;

        if ( n instanceof String ||
             n instanceof JSString ||
             n instanceof Number )
            return super.get( n );

        return null;
    }

    /** Deletes a field with a given name from this map.
     * @param n Name of the field to remove
     * @return The value removed
     */
    public Object removeField( Object n ){
        Object val = _map.remove( n );
        Object val2 = super.removeField( n );

        return val == null ? val2 : val;
    }

    /** Finds this map's keyset
     * @return The keyset
     */
    public Collection keys(){
        return _map.keySet();
    }

    /** Finds this map's values
     * @return The values
     */
    public Collection values(){
        return _map.values();
    }

    /** @unexpose */
    private final Map _map = new CustomHashMap(){
            public boolean doEquals( Object a , Object b ){

                if ( a.getClass() == JSObjectBase.class &&
                     b.getClass() == JSObjectBase.class ){

                    JSObjectBase ao = (JSObjectBase)a;
                    JSObjectBase bo = (JSObjectBase)b;

                    Collection<String> ak = ao.keySet();
                    Collection<String> bk = bo.keySet();

                    if ( ak.size() != bk.size() )
                        return false;

                    for ( String k : ak )
                        if ( ! JSInternalFunctions.JS_eq( ao.get( k ) , bo.get( k ) ) )
                            return false;

                    return true;

                }

                return JSInternalFunctions.JS_eq( a , b );
            }

            public int doHash( Object key ){

                if ( key == null )
                    return 0;

                if ( key instanceof JSObject ){
                    int hash = 0;
                    for ( String s : ((JSObject)key).keySet() )
                        hash += s.hashCode();
                    return hash;
                }

                return key.hashCode();

            }

        };
}
