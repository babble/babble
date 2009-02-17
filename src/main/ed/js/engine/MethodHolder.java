// MethodHolder.java

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

package ed.js.engine;

import java.util.*;
import java.lang.reflect.*;

public class MethodHolder {

    public static boolean isMethodName( Class c , String name ){
	Set<String> names = _methods.get( c );
	if ( names == null ){
	    names = new HashSet<String>();
	    for ( Method m : c.getMethods() )
		names.add( m.getName() );
	    _methods.put( c , names );
	}
	return names.contains( name );
    }

    private static Map<Class,Set<String>> _methods = Collections.synchronizedMap( new HashMap<Class,Set<String>>() );
}
