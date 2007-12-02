// MethodHolder.java

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
