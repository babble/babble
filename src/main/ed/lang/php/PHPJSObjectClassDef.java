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
        if ( PHP.DEBUG ) System.err.println( "class def for : " + name + " : " + type.getName() );
    }
    
    public Value wrap(Env env, Object obj){
        if ( ! _isInit )
            init();
        
        return new Adapter(env, (JSObject)obj, this);
    }       
    
    static class Adapter extends JavaAdapter {
        Adapter( Env env , JSObject obj , PHPJSObjectClassDef def ){
            super( env , obj , def );
            _object = obj;
            _convertor = PHP.getConvertor( env );
            if ( PHP.DEBUG ) System.out.println( "wrapping : " + obj );
        }
        
        public Value callMethod(final Env env, int hash, char []name, int nameLen, Value []args){
            String realName = new String( name , 0 , nameLen );
            Object foo = _object.get( realName );
            if ( ! ( foo instanceof JSFunction ) ){
                Value ret = super.callMethod(env, hash, name , nameLen , args );
                if ( ret instanceof JavaValue && ret.toJavaObject() instanceof JSObject ){
                    ret = wrapJava( ret.toJavaObject() );
                }
                return ret;
            }
            
            JSFunction func = (JSFunction)foo;
            Scope s = func.getScope();
            if ( s == null )
                s = Scope.getAScope();
            s = s.child();
            
            s.setThis( _object );
	    func.getScope( true ).set( "print" , new JSFunctionCalls1(){
		    public Object call( Scope s , Object o , Object[] extra ){
			env.print( o );
			return null;
		    }
		} );
            Object ret = func.call( s , _convertor.toJS( args ) );
            return wrapJava( ret );
        }
	
        public Value get(Value key){
            if ( PHP.DEBUG ) System.out.println( "GET:" + key );
            Object value = _object.get( key.toJavaObject() );
            if ( value != null )
                return wrapJava( value );
            return UnsetValue.UNSET;            
        }

        public Value remove(Value key){
            Object value = _object.removeField( key.toJavaObject() );
            if ( value != null )
                return wrapJava( value );
            return UnsetValue.UNSET;
        }

        public int getSize(){
            return _object.keySet().size();
        }

        public Value wrapJava( Object obj ){
            PHP.getConvertor( getEnv() ).checkConfigged( obj );
            if ( obj instanceof JSObject )
                return (Value)(PHP.getConvertor( getEnv() ).toOther( obj ) );
            return super.wrapJava( obj );
        }

        // -----------------
        // STUFF NEEEDED FOR JavaAdapter
        // -----------------
        
        public Set<Map.Entry<Object, Object>> objectEntrySet(){
            throw new UnimplementedException();
        }

        public Set<Map.Entry<Value,Value>> entrySet(){
            throw new UnimplementedException();
        }

        public Value createTailKey(){
            throw new UnimplementedException();
        }

        public Value putImpl(Value key, Value value){
            return wrapJava( _object.set( _convertor.toJS( key ) , _convertor.toJS( value ) ) );
        }

        public void clear(){
            throw new UnimplementedException();
        }
        
        public Value copy(Env env, IdentityHashMap<Value,Value> map){
            throw new UnimplementedException();
        }

        public Value copy(){
            throw new UnimplementedException();
        }

        final JSObject _object;
        final PHPConvertor _convertor;
    }


    
}
