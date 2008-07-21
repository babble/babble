// MethodHolder.java

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
