// PHPConvertor.java

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
import javax.script.*;

import ed.lang.*;
import ed.js.*;
import ed.js.engine.*;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.expr.*;
import com.caucho.quercus.script.*;
import com.caucho.quercus.module.*;
import com.caucho.quercus.function.*;

public class PHPConvertor extends Value implements ObjectConvertor {

    PHPConvertor( Env env ){
        _env = env;
        _marshalFactory = env.getModuleContext().getMarshalFactory();
    }
    
    public Object[] toJS( Value[] values ){
        Object[] js = new Object[values.length];
        for ( int i=0; i<values.length; i++ )
            js[i] = toJS( values[i] );
        return js;
    }
    
    public Object toJS( Object o ){
        if ( o == null || o instanceof NullValue )
            return null;

        if ( o instanceof Value )
            o = _getMarshal( o ).marshal( _env , (Value)o , o.getClass() );
        
        if ( o instanceof Expr )
            o = _getMarshal( o ).marshal( _env , (Expr)o , o.getClass() );

        if ( o instanceof String || o instanceof StringBuilderValue )
            return new JSString( o.toString() );
        
        if ( o instanceof Number || o instanceof Boolean )
            return o;

        throw new RuntimeException( "don't know what to do with : " + o.getClass() );
    }
    
    public Object toOther( Object o ){
        if ( o == null )
            return null; // TODO: should this be NullValue
        
        if ( o instanceof JSString )
            o = o.toString();
        
        return _getMarshal( o ).unmarshal( _env , o );
    }
    
    private Marshal _getMarshal( Object o ){
        Class c = o.getClass();
        Marshal m = _cache.get( c );
        if ( m == null ){
            m = _marshalFactory.create( c );
            _cache.put( c , m );
        }
        return m;
    }

    final Env _env;
    final MarshalFactory _marshalFactory;
    final Map<Class,Marshal> _cache = new HashMap<Class,Marshal>();
}
