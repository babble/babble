// JSObjectBase.java

package ed.js;

import java.io.*;
import java.util.*;

import ed.db.*;
import ed.js.func.*;
import ed.js.engine.*;

public class JSObjectBase implements JSObject {
    
    static final String GETSET_PREFIX = "_____gs____";
    
    static final Set<String> BAD_KEY_NAMES = new HashSet<String>();
    static {
        BAD_KEY_NAMES.add( "__proto__" );
        BAD_KEY_NAMES.add( "__constructor__" );
        BAD_KEY_NAMES.add( "constructor" );
        BAD_KEY_NAMES.add( "__parent____" );
        
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
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( v != null && "_id".equals( n ) &&
	     ( ( v instanceof String ) || ( v instanceof JSString ) )
	     ){
            v = new ObjectId( v.toString() );
        }

        if ( v != null && v instanceof String )
            v = new JSString( v.toString() );
        
        
        if ( n instanceof String ){
            String name = (String)n;
            
            if ( ! name.startsWith( GETSET_PREFIX ) ){
                JSFunction func = getSetter( name );
                if ( func != null )
                    return _call( func , v );
            }

            if ( _map == null ){
                _map = new TreeMap<String,Object>();
                _keys = new ArrayList<String>();
            }
            
            if ( ! BAD_KEY_NAMES.contains( name ) && 
                 ! name.startsWith( GETSET_PREFIX ) )
                if ( ! _map.containsKey( n ) )
                    _keys.add( name );
            
            _map.put( name , v );
            if ( v instanceof JSObjectBase )
                ((JSObjectBase)v)._name = name;
            return v;
        }
        
        if ( n instanceof Number ){
            setInt( ((Number)n).intValue() , v );
            return v;
        }
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public String getAsString( Object n ){
        final Object v = get( n );
        if ( v == null )
            return null;
        
        return v.toString();
    }

    public Object get( Object n ){

        prefunc();

        if ( n == null )
            n = "null";
        
        if ( n instanceof JSString )
            n = n.toString();
        
        if ( ! "__preGet".equals( n ) ){
            Object foo = _simpleGet( "__preGet" );
            if ( foo != null && foo instanceof JSFunction )
                _call( (JSFunction)foo , n );
        }

        if ( n instanceof String )
            return _simpleGet( (String)n );
        
        if ( n instanceof Number )
            return getInt( ((Number)n).intValue() );
        
        throw new RuntimeException( "object key can't be a [" + n.getClass() + "]" );
    }

    public Object _simpleGet( String s ){
        return _simpleGet( s , 0 );
    }
    
    Object _simpleGet( String s , int depth ){
        final boolean getOrSet = s.startsWith( GETSET_PREFIX );
        
        Object res = null;
        
        if ( depth == 0 && 
             ! getOrSet ){
            JSFunction f = getGetter( s );
            if ( f != null )
                return _call( f );
        }

        if ( _map != null ){
            res = _map.get( s );
            if ( res != null ) return res;
        }

        if ( _map != null ){
            JSObject proto = (JSObject)_map.get( "prototype" );
            if ( proto != null ){

                res = 
                    proto instanceof JSObjectBase ? 
                    ((JSObjectBase)proto)._simpleGet( s  , depth + 1) : 
                    proto.get( s );
                
                if ( res != null ) return res;
            }
        };
        
        JSObject proto = null;
        
        if ( _map != null ){
            proto = (JSObject)_map.get( "__proto__" );
            if ( proto != null ){
                res = proto.get( s );
                if ( res != null ) return res;                
            }
        }

        if ( _constructor != null && _constructor._prototype != proto ){
            res = _constructor._prototype._simpleGet( s , depth + 1);
            if ( res != null ) return res;
        }


        if ( _objectLowFunctions != null 
             && ( _map == null || _map.get( "prototype" ) == null ) 
             && _constructor == null ){
            res = _objectLowFunctions.get( s );
            if ( res != null ) return res;
        }

        if ( _constructor != null && 
             ! getOrSet &&
             ! s.equals( "prototype" ) ){
            // basically static lookup
            res = _constructor._simpleGet( s , depth + 1 );
            if ( res != null ) return res;
        }

        if ( depth == 0 && 
             ! "__notFoundHandler".equals( s ) &&
             ! "__preGet".equals( s ) && 
             ! getOrSet && 
             ! BAD_KEY_NAMES.contains( s )
             ){

            Object blah = _simpleGet( "__notFoundHandler" , depth + 1 );
            if ( blah instanceof JSFunction ){
                JSFunction f = (JSFunction)blah;
                Scope scope = f.getScope();
                if ( scope == null )
                    scope = Scope.getLastCreated();
                scope = scope.child();
                scope.setThis( this );
                if ( ! _inNotFoundHandler.get() ){
                    try {
                        _inNotFoundHandler.set( true );
                        return f.call( scope , s );
                    }
                    finally {
                        _inNotFoundHandler.set( false );
                    }
                }
            }
        }

        return null;
    }

    public Object removeField( Object n ){
        if ( n == null )
            return null;
        
        if ( n instanceof JSString )
            n = n.toString();
        
        Object val = null;

        if ( n instanceof String ){
            if ( _map != null )
                val = _map.remove( (String)n );
            if ( _keys != null )
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
        prefunc();
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

    // ----
    // [gs]etter
    // ---

    void setSetter( String name , JSFunction func ){
        set( setterName( name ) , func );
    }
    
    void setGetter( String name , JSFunction func ){
        set( getterName( name ) , func );
    }

    JSFunction getSetter( String name ){
        return (JSFunction)(_simpleGet( setterName( name ) ) );
    }

    JSFunction getGetter( String name ){
        return (JSFunction)(_simpleGet( getterName( name ) ) );
    }


    static String setterName( String name ){
        return GETSET_PREFIX + "SET" + name;
    }

    static String getterName( String name ){
        return GETSET_PREFIX + "GET" + name;
    }

    // ---

    public String toString(){
        Object temp = get( "toString" );
        
        if ( ! ( temp instanceof JSFunction ) )
            return OBJECT_STRING;
        
        JSFunction f = (JSFunction)temp;

        Scope s = f.getScope().child();
        s.setThis( this );
        
        Object res = f.call( s );
        if ( res == null )
            return "Object(toString was null)";
        return res.toString();
    }

    protected void addAll( JSObject other ){
        for ( String s : other.keySet() )
            set( s , other.get( s ) );
    }

    private Object _call( JSFunction func , Object ... params ){
        Scope sc = Scope.getAScope();
        sc.setThis( this );
        try {
            return func.call( sc , params );
        }
        finally {
            sc.clearThisNormal( null );
        }
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
                s = Scope.getThreadLocal();
            
            s = s.child();
            
            s.setThis( this );
            _constructor.call( s , args );
        }
    }

    public JSFunction getConstructor(){
        return _constructor;
    }

    public JSObject getSuper(){

        if ( _constructor != null && _constructor._prototype != null )
            return _constructor._prototype;

        return (JSObject)_simpleGet( "prototype" );
        
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

    public void debug(){
        try {
            debug( 0 , System.out );
        }
        catch ( IOException ioe ){
            ioe.printStackTrace();
        }
    }

    Appendable _space( int level , Appendable a )
        throws IOException {
        for ( int i=0; i<level; i++ )
            a.append( "  " );
        return a;
    }

    public void debug( int level , Appendable a )
        throws IOException {
        _space( level , a );
        
        a.append( "me :" );
        if ( _name != null )
            a.append( " name : [" ).append( _name ).append( "] " );
        if( _keys != null )
            a.append( "keys : " ).append( _keys.toString() );
        a.append( "\n" );
        
        if ( _map != null ){
            JSObjectBase p = (JSObjectBase)_simpleGet( "prototype" );
            if ( p != null ){
                _space( level + 1 , a ).append( "prototype ||\n" );
                p.debug( level + 2 , a );
            }
            
        }

        if ( _constructor != null ){
            _space( level + 1 , a ).append( "__constructor__ ||\n" );
            _constructor.debug( level + 2 , a );
        }
    }


    // -----
    // name is very weird. it probably doesn't work the way you think or want
    // ----

    public String _getName(){
        return _name;
    }
        
    public void _setName( String n ){
        _name = n;
    }

    protected Map<String,Object> _map = null;
    private List<String> _keys = null;
    private JSFunction _constructor;
    private boolean _readOnly = false;
    private String _name;

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


            set( "merge" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object other , Object args[] ){
                        
                        if ( other == null )
                            return null;
                        
                        Object blah = s.getThis();
                        if ( ! ( blah != null && blah instanceof JSObject ) )
                            throw new RuntimeException( "extend not passed real thing" );
                        
                        if ( ! ( other instanceof JSObject ) )
                            throw new RuntimeException( "can't extend with a non-object" );
                        
                        JSObjectBase n = new JSObjectBase();
                        n.extend( (JSObject)s.getThis() );
                        n.extend( (JSObject)other );

                        return n;
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
                            throw new RuntimeException( "extend not passed real thing" );
                    
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
                            func = (JSFunction)s.get( name );

                        if ( func == null )
                            throw new NullPointerException( "can't find method [" + name + "] to send" );

                        return func.call( s , args );
                    }
                
                } );

            set( "__keySet" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        return new JSArray( obj.keySet() );
                    }
                } );

            set( "__debug" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        obj.debug();
                        return null;
                    }
                } );

            set( "__hashCode" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        JSObjectBase obj = ((JSObjectBase)s.getThis());
                        return obj.hashCode();
                    }
                } );

            set( "is_a_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object type , Object args[] ){
                        return JSInternalFunctions.JS_instanceof( s.getThis() , type );
                    }
                } );
            

            set( "eql_q_" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object o , Object crap[] ){
                        return s.getThis().equals( o );
                    }
                } );

            set( "_lb__rb_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        return ((JSObjectBase)s.getThis()).get( name );
                    }
                } );

            set( "key_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        if ( name == null )
                            return null;
                        return ((JSObjectBase)s.getThis()).containsKey( name.toString() );
                    }
                } );

            set( "has_key_q_" , get( "key_q_" ) );

            set( "__delete" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object name , Object args[] ){
                        return ((JSObjectBase)s.getThis()).removeField( name );
                    }
                } );
            
            set( "const_defined_q_" , new JSFunctionCalls1(){
                    public Object call( Scope s , Object type , Object args[] ){
                        return s.get( type ) != null;
                    }
                } );

            set( "__defineGetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );
                        
                        JSObjectBase o = (JSObjectBase)s.getThis();
                        o.setGetter( name.toString() , (JSFunction)func );
                        return null;
                    }
                } );

            set( "__defineSetter__" , new JSFunctionCalls2(){
                    public Object call( Scope s , Object name , Object func , Object args[] ){
                        if ( ! ( s.getThis() instanceof JSObjectBase ) )
                            throw new RuntimeException( "not a JSObjectBase" );
                        
                        JSObjectBase o = (JSObjectBase)s.getThis();
                        o.setSetter( name.toString() , (JSFunction)func );
                        return null;
                    }
                } );

            set( "to_i" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){                    
                        return JSInternalFunctions.parseNumber( s.getThis() , null );
                    }
                } );

        }
        
        public Collection<String> keySet(){
            return _things.keySet();
        }

        private Map<String,Object> _things = new HashMap<String,Object>();
    }

    public static final JSObject _objectLowFunctions = new BaseThings();
    
    private static final ThreadLocal<Boolean> _inNotFoundHandler = new ThreadLocal<Boolean>(){
        protected Boolean initialValue(){
            return false;
        }
    };
}
