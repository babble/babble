// PHPConvertor.java

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

import java.lang.reflect.*;
import java.util.*;
import javax.script.*;

import ed.util.*;
import ed.lang.*;
import ed.js.*;
import ed.js.engine.*;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.expr.*;
import com.caucho.quercus.script.*;
import com.caucho.quercus.module.*;
import com.caucho.quercus.function.*;
import com.caucho.quercus.program.*;

public class PHPConvertor extends Value implements ObjectConvertor {

    PHPConvertor( Env env ){
        _env = env;
        _moduleContext = env.getModuleContext();
        _marshalFactory = _moduleContext.getMarshalFactory();
    }
    
    public Object[] toJS( Value[] values ){
        Object[] js = new Object[values.length];
        for ( int i=0; i<values.length; i++ )
            js[i] = toJS( values[i] );
        return js;
    }
    
    public Object toJS( Object o ){
        if ( o == null || o instanceof NullValue || o instanceof UnsetValue )
            return null;
        
        // handle special PHP Stuff

        if ( o instanceof Var )
            o = ((Var)o).toValue();
        
        if ( o instanceof Expr )
            o = _getMarshal( o ).marshal( _env , (Expr)o , o.getClass() );

        // use internal convertor for stuff we don't know about

        if ( o instanceof Value )
            o = ((Value)o).toJavaObject();

        // native handling
  
        if ( o instanceof JSObject )
            return o;

        if ( o instanceof String || o instanceof StringValue || o instanceof StringBuilderValue )
            return new JSString( o.toString() );
        
        if ( o instanceof Number || o instanceof Boolean )
            return o;
        
        if ( o instanceof NumberValue || o instanceof BooleanValue ) 
            return ((Value)o).toJavaObject();

        if ( o instanceof ArrayValue )
            return new PHPWrapper( this , (Value) o );
        
        if ( o instanceof ed.db.ObjectId )
            return o;
        
        
        throw new RuntimeException( "don't know what to do with : " + o.getClass().getName() + " : " + o );
    }

    public Value toPHP( Object o ){
        return (Value)toOther( o );
    }
    
    public Object toOther( Object o ){
        if ( o == null )
            return null; // TODO: should this be NullValue
        
        if( o instanceof PHPWrapper)
            return ((PHPWrapper) o)._value;
        
        if ( o instanceof JSString )
            o = o.toString();
        
        checkConfigged( o );
        
        if ( o instanceof JSObject )
            return _getClassDef( o.getClass() ).wrap( _env , o );

        return _getMarshal( o ).unmarshal( _env , o );
    }

    void checkConfigged( Object o ){
        checkConfigged( o , true );
    }
    
    void checkConfigged( Object o , boolean tryAgain ){
        if ( ! ( o instanceof JSObject ) )
            return;

        Class c = o.getClass();
        if ( _configged.contains( c ) )
            return;
        
        JavaClassDef def = null;
        
        try {
            if ( PHP.DEBUG ) System.out.println( "Adding for : " + c.getName() );
            def = _moduleContext.addClass( c.getName() , c , null , PHPJSObjectClassDef.class );
        }
        catch ( Exception e ){
            throw new RuntimeException( "internal error : " + c.getClass() , e );
        }
        
        if ( def instanceof PHPJSObjectClassDef ){
            _configged.add( c );
            return;
        }
            
        if ( ! tryAgain )
            throw new RuntimeException( "someone got in ahead of me and i can't recover" );
                        
        
        try {
            Map m = (Map)(PHP.getField( _moduleContext , "_javaClassWrappers" ));
            m.remove( c.getName() );
            m.remove( c.getName().toLowerCase() );
            checkConfigged( o , false );
        }
        catch( Exception e ){
            throw new RuntimeException( "someone got in ahead of me and i can't recover" , e  );
        }
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

    PHPJSObjectClassDef _getClassDef( Class c ){
        PHPJSObjectClassDef def = _defCache.get( c );
        if ( def == null ){
            def = new PHPJSObjectClassDef( _moduleContext , c.getName() , c );
            _defCache.put( c , def );
        }
        return def;
    }

    final Env _env;
    final ModuleContext _moduleContext;
    final MarshalFactory _marshalFactory;
    
    final Map<Class,Marshal> _cache = new HashMap<Class,Marshal>();
    final IdentitySet<Class> _configged = new IdentitySet<Class>();
    final Map<Class,PHPJSObjectClassDef> _defCache = new HashMap<Class,PHPJSObjectClassDef>();
}
