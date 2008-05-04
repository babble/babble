// JSObjectBase.java

package ed.js;

import java.util.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;

public class JSObjectBase implements JSObject {

    static {
        JS._debugSIStart( "JSObjectBase" );
    }

    static final String OBJECT_STRING = "Object";

    public JSObjectBase(){
    }

    public JSObjectBase( JSFunction constructor ){
        setConstructor( constructor );
    }

    public void prefunc(){}

    public Object set( Object n , Object v ){
        _readOnlyCheck();
        prefunc();
        if ( n == null )
            n = "null";
        
        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( v != null &&  "_id".equals( n ) &&
	     ( ( v instanceof String ) || ( v instanceof JSString ) )
	     ){
            v = new ObjectId( v.toString() );
        }
            
        
        if ( n instanceof String ){
            String name = (String)n;
            
            if ( _map == null ){
                _map = new TreeMap<String,Object>();
                _keys = new ArrayList<String>();
            }
            
            
            if ( ! ( name.equals( "__proto__" ) || 
                     name.equals( "__constructor__" ) || 
                     name.equals( "constructor" ) || 
                     name.equals( "__parent__" ) ) )
                if ( ! _map.containsKey( n ) )
                    _keys.add( name );
            
            _map.put( name , v );
            return v;
        }
        
        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return v;
        }
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public Object get( Object n ){

        prefunc();

        if ( n == null )
            n = "null";
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( ! "__preGet".equals( n ) ){
            Object foo = _simpleGet( "__preGet" );
            if ( foo != null && foo instanceof JSFunction ){
                Scope s = Scope.getLastCreated();
                if ( s != null ){
                    try {
                        s.setThis( this );
                        ((JSFunction)foo).call( s , n );
                    }
                    finally {
                        s.clearThisNormal( null );
                    }
                }
            }
        }

        if ( n instanceof String )
            return _simpleGet( (String)n );
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public Object _simpleGet( String s ){
        Object res = null;
        
        if ( _map != null )
            res = _map.get( s );
        
        if ( res == null && _constructor != null )
            res = _constructor._prototype.get( s );
        
        if ( res == null && _objectLowFunctions != null )
            res = _objectLowFunctions.get( s );

        return res;
    }

    public Object removeField( Object n ){
        if ( n == null )
            return null;
        
        if ( n instanceof JSString )
            n = n.toString();
        
        Object val = null;

        if ( n instanceof String ){
            val = _map.remove( (String)n );
            _keys.remove( n );
        }
        
        return val;
    }


    public Object setInt( int n , Object v ){
        _readOnlyCheck();
        prefunc();
        return set( String.valueOf( n ) , v );
    }

    public Object getInt( int n ){
        prefunc();
        return get( String.valueOf( n ) );
    }


    public boolean containsKey( String s ){
        if ( _keys != null && _keys.contains( s ) )
            return true;
        
        if ( _constructor != null && _constructor._prototype.containsKey( s ) )
            return true;

        return false;
    }

    public Collection<String> keySet(){
        prefunc();
        if ( _keys == null )
            return EMPTY_SET;
        return _keys;
    }

    public String toString(){
        Object temp = get( "toString" );
        
        if ( ! ( temp instanceof JSFunction ) )
            return OBJECT_STRING;
        
        JSFunction f = (JSFunction)temp;

        Scope s = f.getScope().child();
        s.setThis( this );
        return f.call( s ).toString();
    }

    protected void addAll( JSObject other ){
        for ( String s : other.keySet() )
            set( s , other.get( s ) );
    }

    public String getJavaString( Object name ){
        Object foo = get( name );
        if ( foo == null )
            return null;
        return foo.toString();
    }

    public void setConstructor( JSFunction cons ){
        setConstructor( cons , false );
    }

    public void setConstructor( JSFunction cons , boolean exec ){
        setConstructor( cons , exec , null );
    }
    
    public void setConstructor( JSFunction cons , boolean exec , Object args[] ){
        _readOnlyCheck();

        _constructor = cons;
        set( "__constructor__" , _constructor );
        set( "constructor" , _constructor );
        set( "__proto__" , _constructor == null ? null : _constructor._prototype );

        if ( _constructor != null && exec ){
            
            Scope s = _constructor.getScope();
            
            if ( s == null )
                s = Scope.GLOBAL;
            
            s = s.child();
            
            s.setThis( this );
            _constructor.call( s , args );
        }
    }

    public JSFunction getConstructor(){
        return _constructor;
    }

    public void lock(){
        setReadOnly( true );
    }

    public void setReadOnly( boolean readOnly ){
        _readOnly = readOnly;
    }

    private final void _readOnlyCheck(){
        if ( _readOnly )
            throw new RuntimeException( "can't modify JSObject - read only" );
    }

    public void extend( JSObject other ){
        if ( other == null )
            return;

        for ( String key : other.keySet() ){
            set( key , other.get( key ) );
        }

    }

    protected Map<String,Object> _map = null;
    private List<String> _keys = null;
    private JSFunction _constructor;
    private boolean _readOnly = false;

    static final Set<String> EMPTY_SET = Collections.unmodifiableSet( new HashSet<String>() );

    private static class BaseThings extends JSObjectLame {
        
        BaseThings(){
            init();
        }

        public Object get( Object o ){
            String name = o.toString();
            return _things.get( name );
        }

        public Object set( Object name , Object val ){
            _things.put( name.toString() , val );
            return val;
        }
        
        protected void init(){
            
            set( "__extend" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){
                        
                        if ( other == null )
                            return null;
                        
                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                            throw new RuntimeException( "extendt not passed real thing" );
                    
                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't extend with a non-object" );
                    
                        ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                        return null;
                    }
                } );
        
            set( "__include" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){
                    
                        if ( other == null )
                            return null;

                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't include with a non-object" );
                    
                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObjectBase ) )
                            throw new RuntimeException( "extendt not passed real thing" );
                    
                        ((JSObjectBase)(s.getThis())).extend( (JSObject)other );
                        return null;
                    }
                } );
        

            set( "__send" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){

                        JSObject obj = ((JSObject)s.getThis());
                        if ( obj == null )
                            throw new NullPointerException( "send called on a null thing" );
                    
                        JSFunction func = ((JSFunction)obj.get( name ) );
                    
                        if ( func == null ){
                            // this is a dirty dirty hack for namespace collisions
                            // i hate myself for even writing it in the first place
                            func = ((JSFunction)obj.get( "__" + name ) );
                        }

                        if ( func == null )
                            throw new NullPointerException( "can't find method [" + name + "] to send" );

                        return func.call( s , args );
                    }
                
                } );


        }
        
        public Collection<String> keySet(){
            return _things.keySet();
        }

        private Map<String,Object> _things = new HashMap<String,Object>();
    }

    public static final JSObject _objectLowFunctions = new BaseThings();

}
