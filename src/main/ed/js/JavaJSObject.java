// JavaJSObject.java

package ed.js;

import java.util.*;
import java.lang.reflect.*;

import ed.util.*;
import ed.js.func.*;
import ed.js.engine.*;

public abstract class JavaJSObject implements JSObject {
    
    public Object get( Object n ){
        return getWrapper().get( this , n.toString() );
    }

    public JSFunction getFunction( String name ){
        return null;
    }

    public final Collection<String> keySet(){
        return keySet( true );
    }

    public Collection<String> keySet( boolean includePrototype ){
        return getWrapper().keySet();
    }

    public boolean containsKey( String s ){
        return getWrapper().containsKey( s );
    }

    public Object set( Object n , Object v ){
        return getWrapper().set( this , n.toString() , v );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "can't use ints with JavaJSObject" );
    }

    public Object getInt( int n ){
        return null;
    }
    
    public Object removeField( Object n ){
        throw new RuntimeException( "can't remove fields from java objects" );
    }

    public JSFunction getConstructor(){
        return getWrapper().getConstructor();
    }

    public JSObject getSuper(){
        return null;
    }

    JavaWrapper getWrapper(){
        if ( _wrapper != null )
            return _wrapper;

        _wrapper = getWrapper( this.getClass() );
        return _wrapper;
    }

    JavaWrapper _wrapper;

    public static class JavaWrapper {
        JavaWrapper( Class c ){
            _class = c;
            _name = c.getName();
            _constructor = new JSFunctionCalls0(){

                    public JSObject newOne(){
                        try {
                            return (JSObject)(_class.newInstance());
                        }
                        catch ( Exception e ){
                            throw new RuntimeException( "can't create a new blank [" + _class.getName() + "]" , e );
                        }
                        
                    }

                    public Object call( Scope s , Object[] extra ){
                        return s.getThis();
                    }
                    
                };
            
            _fields = new TreeMap<String,FieldInfo>();
            for ( Method m : c.getMethods() ){
                if ( ! ( m.getName().startsWith( "get" ) || m.getName().startsWith( "set" ) ) )
                    continue;
                
                String name = m.getName().substring(3);
                if ( name.length() == 0 || IGNORE_FIELDS.contains( name ) )
                    continue;

                FieldInfo fi = _fields.get( name );
                if ( fi == null ){
                    fi = new FieldInfo( name );
                    _fields.put( name , fi );
                }
                
                if ( m.getName().startsWith( "get" ) )
                    fi._getter = m;
                else
                    fi._setter = m;
            }

            Set<String> names = new HashSet<String>( _fields.keySet() );
            for ( String name : names )
                if ( ! _fields.get( name ).ok() )
                    _fields.remove( name );
            
            _keys = Collections.unmodifiableSet( _fields.keySet() );
        }

        public Set<String> keySet(){
            return _keys;
        }

        public boolean containsKey( String key ){
            return _keys.contains( key );
        }

        public JSFunction getConstructor(){
            return _constructor;
        }
        
        public Object get( JavaJSObject t , String name ){
            FieldInfo i = _fields.get( name );
            if ( i == null )
                return null;
            try {
                return i._getter.invoke( t );
            }
            catch ( Exception e ){
                throw new RuntimeException( "could not invoke getter for [" + name + "] on [" + _name + "]" , e );
            }
        }

        public Object set( JavaJSObject t , String name , Object val ){
            FieldInfo i = _fields.get( name );
            if ( i == null )
                throw new IllegalArgumentException( "no field [" + name + "] on [" + _name + "]" );
            try {
                return i._setter.invoke( t , val );
            }
            catch ( Exception e ){
                throw new RuntimeException( "could not invoke setter for [" + name + "] on [" + _name + "]" , e );
            }
        }
        
        final Class _class;
        final String _name;
        final Map<String,FieldInfo> _fields;
        final Set<String> _keys;
        final JSFunction _constructor;
    }

    static class FieldInfo {
        FieldInfo( String name ){
            _name = name;
        }

        boolean ok(){
            return 
                _getter != null &&
                _setter != null;
        }
        
        final String _name;
        Method _getter;
        Method _setter;
    }

    public static JavaWrapper getWrapper( Class c ){
        JavaWrapper w = _wrappers.get( c );
        if ( w == null ){
            w = new JavaWrapper( c );
            _wrappers.put( c , w );
        }
        return w;
    }
    
    private static final Map<Class,JavaWrapper> _wrappers = Collections.synchronizedMap( new HashMap<Class,JavaWrapper>() );
    private static final Set<String> IGNORE_FIELDS = new HashSet<String>();
    static {
        IGNORE_FIELDS.add( "Int" );
    }
}
