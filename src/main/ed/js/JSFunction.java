// JSFunction.java

package ed.js;

import ed.js.engine.Scope;

public abstract class JSFunction extends JSFunctionBase {

    static {
        JS._debugSIStart( "JSFunction" );
    }

    public static final String TO_STRING_PREFIX = "JSFunction : ";
    
    public JSFunction( int num ){
        this( null , null , num );
    }
    
    public JSFunction( Scope scope , String name , int num ){
        super( num );
        _scope = scope;
        _name = name;
        
        _prototype = new JSObjectBase();

        _init();

        set( "prototype" , _prototype );
        set( "isFunction" , true );
        
        init();
    }
    
    public Object set( Object n , Object b ){
        if ( n != null && "prototype".equals( n.toString() ) )
            _prototype = (JSObject)b;
        
        return super.set( n , b );
    }

    public JSObject newOne(){
        return new JSObjectBase( this );
    }

    protected void init(){}

    public Object get( Object n ){
        Object foo = super.get( n );
        if ( foo != null ) 
            return foo;
        return _prototype.get( n );
    }

    public void setName( String name ){
        _name = name;
    }

    public String getName(){
        return _name;
    }

    public Scope getScope(){
        return getScope( false );
    }

    /**
     * @package threadLocal if this is true, it returns a thread local scope that you can modify for your thread
     */
    public Scope getScope( boolean threadLocal ){
        Scope s = _tlScope.get();
        if ( s != null ){
            return s;
        }
        
        if ( threadLocal ){
            if ( _scope == null )
                s = new Scope( "func tl scope" , null );
            else
                s = _scope.child( "func tl scope" );
            s.setGlobal( true );
            _tlScope.set( s );
            return s;
        }

        return _scope;
    }

    public void setTLScope( Scope tl ){
        _tlScope.set( tl );
    }

    public Scope getTLScope(){
        return _tlScope.get();
    }

    public void clearScope(){
        Scope s = _tlScope.get();
        if ( s != null )
            s.reset();
    }
    
    public String toString(){
        return TO_STRING_PREFIX + _name;
    }

    public JSArray argumentNames(){
        if ( _arguments != null )
            return _arguments;
        
        JSArray temp = new JSArray();
        for ( int i=0; i<_num; i++ ){
            temp.add( "unknown" + i );
        }
        return temp;
    }

    private final Scope _scope;
    private final ThreadLocal<Scope> _tlScope = new ThreadLocal<Scope>();

    protected JSObject _prototype;


    protected JSArray _arguments;
    protected String _name = "NO NAME SET";

    public static JSFunction _call = new ed.js.func.JSFunctionCalls1(){
            public Object call( Scope s , Object obj , Object[] args ){
                JSFunction func = (JSFunction)s.getThis();
                s.setThis( obj );
                try {
                    return func.call( s , args );
                }
                finally {
                    s.clearThisNormal( null );
                }
            }
        };
       
    static JSFunction _apply = new ed.js.func.JSFunctionCalls2(){
            public Object call( Scope s , Object obj , Object args , Object [] foo ){
                JSFunction func = (JSFunction)s.getThis();
                if(! (args instanceof JSArray) )
                    throw new RuntimeException("second argument to Function.prototype.apply must be an array");
                JSArray jary = (JSArray)args;
                s.setThis( obj );
                try {
                    return func.call( s , jary.toArray() );
                }
                finally {
                    s.clearThisNormal( null );
                }
            }
        };

    
    private static void _init(){
        if ( _staticInited )
            return;
    
        JSFunction fcons = JSInternalFunctions.FunctionCons;
        if ( fcons == null )
            return;
        
        fcons._prototype.set( "wrap" , Prototype._functionWrap );
        fcons._prototype.set( "bind", Prototype._functionBind );
        
        fcons._prototype.set( "call" , JSFunction._call );
        fcons._prototype.set( "apply" , JSFunction._apply );
        
        _staticInited = true;
    }
    
    private static boolean _staticInited = false;

    static {
        JS._debugSIDone( "JSFunction" );
    }
}

