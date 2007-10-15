// Scope.java

package ed.js.engine;

import java.lang.reflect.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;

public class Scope {
    
    public static Scope GLOBAL = new Scope( "GLOBAL" , JSBuiltInFunctions._myScope  );
    static {
        GLOBAL._locked = true;
        GLOBAL._global = true;
    }

    static class _NULL {
        
    }
    static _NULL NULL = new _NULL();
    
    public Scope( String name , Scope parent ){
        _name = name;
        _parent = parent;
    }

    public Scope child(){
        return new Scope( _name + ".child" , this );
    }

    public Object put( String name , Object o , boolean local ){
        
        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( local
             || _parent == null
             || _parent._locked 
             || ( _objects != null && _objects.containsKey( name ) )
             || _global
             ){
            
            if ( o == null )
                o = NULL;
            if ( o instanceof String) 
                o = new JSString( (String)o );
            if ( _objects == null )
                _objects = new TreeMap<String,Object>();
            _objects.put( name , o );
            return o;
        }
        
        
        _parent.put( name , o , false );
        return o;
    }
    
    public Object get( String name ){
        final Object r = _get( name );
        return r;
    }

    Object _get( String name ){
        Object foo = _objects == null ? null : _objects.get( name );
        if ( foo != null ){
            if ( foo == NULL )
                return null;
            return foo;
        }
        
        if ( _parent == null )
            return null;
        
        return _parent._get( name );
    }

    public JSFunction getFunction( String name ){
        Object o = get( name );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
        return (JSFunction)o;
    }

    public Scope newThis( JSFunction f ){
        _this = new JSClass( f );
        return this;
    }

    public Scope setThis( JSObject o ){
        _this = o;
        return this;
    }

    public JSFunction getFunctionAndSetThis( final Object obj , final String name ){
        if ( obj instanceof JSObject ){
            JSObject jsobj = (JSObject)obj;
            _this = jsobj;
            return (JSFunction)(jsobj.get( name ));
        }
        
        return new JSFunctionCalls0(){
            public Object call( Scope s , Object params[] ){
                Method all[] = obj.getClass().getMethods();
                
                methods:
                for ( Method m : all ){
                
                    if ( ! name.equals( m.getName() ) )
                        continue;
                    
                    Class myClasses[] = m.getParameterTypes();
                    if ( myClasses != null ){
                        
                        if ( params == null )
                            params = EMPTY_OBJET_ARRAY;
                        
                        if ( myClasses.length != params.length )
                            continue;
                        
                        for ( int i=0; i<myClasses.length; i++ ){
                            // null is fine with me
                            if ( params[i] == null ) 
                                continue;
                            
                            if ( myClasses[i] == String.class )
                                params[i] = params[i].toString();
                            
                            if ( ! myClasses[i].isAssignableFrom( params[i].getClass() ) )
                                continue methods;
                            
                        }
                    }
                    
                    m.setAccessible( true );
                    try {
                        return m.invoke( obj , params );
                    }
                    catch ( Exception e ){
                        throw new RuntimeException( e );
                    }
                }
                throw new RuntimeException( "can't find a valid native method for : " + name );
            }
        };
    }

    public JSObject getThis(){
        return _this;
    }

    public JSObject clearThisNew( Object whoCares ){
        JSObject foo = _this;
        _this = null;
        return foo;
    }

    public Object clearThisNormal( Object o ){
        _this = null;
        return o;
    }
    
    final String _name;
    final Scope _parent;

    boolean _locked = false;
    boolean _global = false;

    Map<String,Object> _objects;
    JSObject _this;


    private static final Object[] EMPTY_OBJET_ARRAY = new Object[0];
}
