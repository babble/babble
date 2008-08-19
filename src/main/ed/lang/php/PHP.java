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

import com.caucho.quercus.*;
import com.caucho.quercus.script.*;

public class PHP {
    
    static QuercusScriptEngineFactory _phpFactory = new QuercusScriptEngineFactory();

    public static ScriptEngine getScriptEngine(){
        QuercusScriptEngine engine = (QuercusScriptEngine)(_phpFactory.getScriptEngine());
        Quercus quercus = null;

        try {
            Method m = engine.getClass().getDeclaredMethod( "getQuercus" );
            m.setAccessible( true );
            quercus = (Quercus)(m.invoke( engine ));
        }
        catch ( Exception e ){
            throw new RuntimeException( "can't do quercus hack" , e );
        }
        
        //quercus.getModuleContext().addClass( java.lang.String name, java.lang.Class type, java.lang.String extension, java.lang.Class javaClassDefClass) ;
        //quercus.getModuleContext().addClass( null , null , null , null );
        

        return engine;
    }
}
