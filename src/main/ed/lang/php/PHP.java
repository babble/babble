// PHP.java

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
import java.io.File;
import javax.script.*;

import ed.lang.*;
import ed.js.engine.*;
import ed.appserver.jxp.*;
import ed.appserver.adapter.AdapterType;
import ed.appserver.AppContext;
import ed.appserver.JSFileLibrary;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.script.*;

public class PHP extends Language {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.PHP" );

    public PHP(){
        super( "php" );
    }

    public JxpSource getAdapter(AdapterType type, File f, AppContext context, JSFileLibrary lib) {
        // TODO - fix me
        return null;
    }

    public boolean isScriptable(){
        return true;
    }

    static QuercusScriptEngineFactory _phpFactory = new QuercusScriptEngineFactory();
    
    public ScriptEngine getScriptEngine(){
        QuercusScriptEngine engine = (QuercusScriptEngine)(_phpFactory.getScriptEngine());
        
        add( engine , Scope.class , PHPJSObjectClassDef.class );

        return engine;
    }

    static PHPConvertor getConvertor( Env env ){
	
	if ( env instanceof PHPEnv )
	    return ((PHPEnv)env)._convertor;

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
