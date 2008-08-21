// PHPWrapper.java

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

package ed.lang.php;

import java.util.*;

import com.caucho.quercus.env.*;

import ed.js.*;

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

    public Collection<String> keySet( boolean includePrototype ){
        List<String> keys = new ArrayList<String>();

        if ( _arrayValue != null )
            for ( Object foo : _arrayValue.keySet() )
                keys.add( foo.toString() );
            
        return keys;
    }

    final PHPConvertor _convertor;
    final Value _value;
    final ArrayValue _arrayValue;
}
