// PHP.java

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

import java.lang.reflect.*;
import javax.script.*;

import ed.lang.*;
import ed.js.engine.*;
import ed.appserver.jxp.*;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.script.*;

public class PHP {

    static boolean DEBUG = false;

    static QuercusScriptEngineFactory _phpFactory = new QuercusScriptEngineFactory();
    
    public static ScriptEngine getScriptEngine(){
        QuercusScriptEngine engine = (QuercusScriptEngine)(_phpFactory.getScriptEngine());
        
        add( engine , Scope.class , PHPJSObjectClassDef.class );

        return engine;
    }

    static PHPConvertor getConvertor( Env env ){
        Value convertor = env.getConstant( "_10genconvertor");
        if ( ! ( convertor instanceof PHPConvertor) ){

            JxpScriptContext context = (JxpScriptContext)getField( env , "_scriptContext" );
            if ( context != null && context.getObjectConvertor() != null ){
                convertor = (Value)context.getObjectConvertor();
            }
            else {
                convertor = new PHPConvertor( env );
                if ( context != null )
                    context.setObjectConvertor( (ObjectConvertor)convertor );
            }


            env.addConstant( "_10genconvertor" , convertor , false );
        }
        return (PHPConvertor)convertor;
    }

    static void add( QuercusScriptEngine engine , Class c , Class def ){
        try {
            getQuercus( engine ).getModuleContext().addClass( c.getName() , c , null , def );
        }
        catch ( Exception e ){
            throw new RuntimeException( "couldn't add " + c , e );
        }
    }

    static Quercus getQuercus( QuercusScriptEngine engine ){
        try {
            Method m = engine.getClass().getDeclaredMethod( "getQuercus" );
            m.setAccessible( true );
            return (Quercus)(m.invoke( engine ));
        }
        catch ( Exception e ){
            throw new RuntimeException( "can't do quercus hack" , e );
        }

    }

    static Object getField( Object o , String name ){
        if ( o == null )
            throw new NullPointerException( "o can't be null" );
        try {
            Field f = o.getClass().getDeclaredField( name );
            f.setAccessible( true );
            return f.get( o );
        }
        catch ( Exception e ){
            throw new RuntimeException( "your hack failed.  trying to get [" + name + "] from [" + o.getClass().getName() + "]" , e );
        }
    }
}
