// PHPJSObjectClassDef.java

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

import com.caucho.quercus.*;
import com.caucho.quercus.script.*;
import com.caucho.quercus.program.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.module.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.lang.*;

public class PHPJSObjectClassDef extends JavaClassDef {

    public PHPJSObjectClassDef(ModuleContext moduleContext, String name, Class type){
        super( moduleContext , name , type );
    }
    
    public Value wrap(Env env, Object obj){
        if ( ! _isInit )
            init();
        
        return new Adapter(env, (Scope) obj, this);
    }       
    
    static class Adapter extends JavaMapAdapter {
        Adapter( Env env , Scope obj , PHPJSObjectClassDef def ){
            super( env , obj , def );
            _object = obj;
        }
        
        public Value callMethod(Env env, int hash, char []name, int nameLen, Value []args){
            String realName = new String( name , 0 , nameLen );
            Object foo = _object.get( realName );
            if ( ! ( foo instanceof JSFunction ) )
                return super.callMethod(env, hash, name , nameLen , args );
            
            JSFunction func = (JSFunction)foo;
            Scope s = func.getScope();
            if ( s == null )
                s = Scope.getAScope();
            s = s.child();
            
            PHPConvertor convertor = PHP.getConvertor( env );

            s.setThis( _object );
            Object ret = func.call( s , convertor.toJS( args ) );
            return (Value)convertor.toOther( ret );
        }
        
        
        final JSObject _object;
    }
}
