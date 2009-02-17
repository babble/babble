// JSObjectLame.java

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

    public final Set<String> keySet(){
        return keySet( true );
    }

    public Set<String> keySet( boolean includePrototype ){
        throw _getException();
    }

    public boolean containsKey( String s ){
        throw _getException();
    }

    public boolean containsKey( String s , boolean includePrototype ){
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
