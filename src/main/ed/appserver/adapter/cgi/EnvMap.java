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

package ed.appserver.adapter.cgi;

import java.util.HashMap;

/**
 *  Simple extension of HashMap to prevent null values
 */
public class EnvMap extends HashMap<String, Object> {

    public EnvMap() {
    }

    public void set(String name, String value)
    {
        put(name, nonNull(value));
    }

    /**
     * Mainly for WSGI, which can pass pythong thingies (e.g. tupeles) for values
     * 
     * @param name name of var
     * @param value value of var
     */
    public void set(String name, Object value)
    {
        put(name, value);
    }

    public String nonNull(String s)
    {
        return s == null ? "" : s;
    }
}
