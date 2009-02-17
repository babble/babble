// JSRef.java

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

package ed.js;

import ed.js.engine.*;

/** @expose */
public class JSRef {

    /** Initializes a new reference to a field in an object
     * @param scope Scope to use
     * @param object
     * @param key A key referring to a field of the object
     */
    public JSRef( Scope scope , JSObject object , Object key ){
        _scope = scope;
        _object = object;
        _key = key;
    }

    /** Get the field of the object, if the object is non-null, otherwise get the key.
     * @return The object's field.
     */
    public Object get(){
        if ( _object != null )
            return _object.get( _key );
        return _scope.get( _key.toString() );
    }

    /** Set this ref's field to a given value.
     * @param o New value
     */
    public void set( Object o ){
        if ( _object != null )
            _object.set( _key , o );
        else
            _scope.put( _key.toString() , o , false );
    }

    /** Returns the scope, object, and key that this ref represents.
     * @return String representation of this ref
     */
    public String toString(){
        return "[[JSRef Scope: " + ( _scope != null ) + " Object : " + ( _object != null ) + " Key : " + _key + " ]]";
    }

    private final Scope _scope;
    private final JSObject _object;
    private final Object _key;

}
