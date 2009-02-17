// Box.java

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

package ed.util;

/**
 *  Simple wrapper class to make arguments mutable
 * @expose
 */
public class Box<T> {
    
    public Box(){
        _t = null;
    }
    
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
