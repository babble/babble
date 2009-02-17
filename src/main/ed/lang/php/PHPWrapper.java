// PHPWrapper.java

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

package ed.lang.php;

import java.util.*;

import com.caucho.quercus.env.*;

import ed.js.*;
import ed.util.*;

public class PHPWrapper extends JSObjectLame {
    
    PHPWrapper( PHPConvertor convertor , Value v ){
        _convertor = convertor;
        _value = v;
        _arrayValue = _value instanceof ArrayValue ? (ArrayValue)_value : null;
    }

    public Object get( Object n ){
        return _convertor.toJS( _value.get( (Value)(_convertor.toOther( n )) ) );
    }

    public Object set( Object n , Object v ){
        if ( _arrayValue != null )
            return _convertor.toJS( _arrayValue.put( _convertor.toPHP( n ) , _convertor.toPHP( v ) ) );
        throw new RuntimeException( "can't set something on a : " + _value.getClass().getName() );
    }

    public Set<String> keySet( boolean includePrototype ){
        Set<String> keys = new OrderedSet<String>();
        if ( _arrayValue != null )
            for ( Object foo : _arrayValue.keySet() )
                keys.add( foo.toString() );
            
        return keys;
    }

    public boolean containsKey( String s ){
	if ( _arrayValue == null )
	    return false;

	for ( Object foo : _arrayValue.keySet() )
	    if ( foo.toString().equals( s ) )
		return true;

	return false;
    }

    final PHPConvertor _convertor;
    final Value _value;
    final ArrayValue _arrayValue;
}
