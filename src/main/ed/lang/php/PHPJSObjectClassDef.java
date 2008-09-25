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
import ed.util.MapEntryImpl;

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
            this( env , obj , def , PHP.getConvertor( env ) );
        }

        Adapter( Env env , JSObject obj , PHPJSObjectClassDef def , PHPConvertor convertor ){
            super( env , obj , def );
            _object = obj;
            _convertor = convertor;
            _def = def;
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
            
            JSFunction myPrint = new JSFunctionCalls1(){
		    public Object call( Scope s , Object o , Object[] extra ){
			env.print( o );
			return null;
		    }
                };

	    s.set( "print" , myPrint );
            func.getScope( true ).set( "print" , myPrint );

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

        // -----------------
        // STUFF NEEEDED FOR JavaAdapter
        // -----------------
        public Iterator<Value> getKeyIterator( Env env ) {
            return new  PHPConvertedIterator( env , _object.keySet().iterator() );
        }

        public Iterator<Value> getValueIterator( Env env ) {
            return new  PHPConvertedIterator( env , new JSObjectValueIterator( _object ) );
        }

        public Iterator<Map.Entry<Value, Value> > getIterator( Env env ) {
          return new PHPConvertedEntryIterator( getEnv(),  new JSObjectEntryIterator( _object ) );
        }

        public Set<Map.Entry<Object, Object> > objectEntrySet() {
            Set<Map.Entry<Object, Object> > entries = new HashSet<Map.Entry<Object,Object> >();
            
            fillCollection( getIterator( getEnv() ) , (Collection)entries );
            return entries;
        }

        public Set<Map.Entry<Value, Value> > entrySet() {
            Set<Map.Entry<Value, Value> > entries = new HashSet<Map.Entry<Value,Value> >();

            fillCollection( getIterator( getEnv() ) , entries );
            return entries;
        }

        public Collection<Value> values() {
            ArrayList<Value> values = new ArrayList<Value>();
            fillCollection( getValueIterator( getEnv() ) , values );
            return values;
        }
        
        public Value createTailKey() {
            long nextAvailableIndex = 0;

            for ( String key : _object.keySet() ) {
                long keyIndex;
                
                try {
                    double temp = Double.parseDouble( key );
                    if( ! JSNumericFunctions.couldBeLong( temp ) )
                        continue;
                    keyIndex = (long)temp;
                }
                catch( NumberFormatException e ) {
                    continue;
                }
                nextAvailableIndex = Math.max( nextAvailableIndex , keyIndex + 1 );
            }
            
            return LongValue.create( nextAvailableIndex );
        }

        public Value putImpl( Value key, Value value ){
            return wrapJava( _object.set( _convertor.toJS( key ) , _convertor.toJS( value ) ) );
        }

        public void clear(){
            ArrayList<String> keys = new ArrayList<String>( _object.keySet() );
            for ( String key : keys )
                _object.removeField( key );
        }
        
        public Value copy( Env env, IdentityHashMap<Value, Value> map ){
            return new Adapter( env , _object , _def , _convertor ); 
        }
        
        public Value copy(){
            return new Adapter( getEnv() , _object , _def , _convertor ); 
        }
        
        private static <T> void fillCollection(Iterator<? extends T> srcIter, Collection<T> dest) {
            while( srcIter.hasNext() )
                dest.add( srcIter.next() );
        }
        
        public static class PHPConvertedIterator implements Iterator<Value> {
            
            public PHPConvertedIterator(Env env, Iterator<?> iter) {
                _env = env;
                _inner = iter;
            }
            
            public boolean hasNext() {
                return _inner.hasNext();
            }
            
            public Value next() {
                return _env.wrapJava( _inner.next() );
            }
            
            public void remove() {
                _inner.remove();
            }
            
            private final Iterator<?> _inner;
            private final Env _env;
        }
        
        public static class PHPConvertedEntryIterator implements Iterator<Map.Entry<Value, Value> > {
            
            public PHPConvertedEntryIterator(Env env, Iterator<? extends Map.Entry> iter) {
                _env = env;
                _inner = iter;
            }
            
            public boolean hasNext() {
                return _inner.hasNext();
            }
            
            public Map.Entry<Value, Value> next() {
                Map.Entry org = _inner.next();
                return new MapEntryImpl<Value, Value>( _env.wrapJava( org.getKey() ) , _env.wrapJava( org.getValue() ) );
            }
            
            public void remove() {
                _inner.remove();
            }
            
            private final Iterator<? extends Map.Entry> _inner;
            private final Env _env;
        }
        
        
        
        
        final JSObject _object;
        final PHPConvertor _convertor;
        final PHPJSObjectClassDef _def;
    }
}
