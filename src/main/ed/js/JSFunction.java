// JSFunction.java

package ed.js;

import ed.lang.*;
import ed.util.*;
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

    public JSObject getPrototype(){
        return _prototype;
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

    public boolean usePassedInScope(){
        Boolean b = _forceUsePassedInScopeTL.get();
        if ( b != null )
            return b;
        
        return _forceUsePassedInScope;
    }

    public void setUsePassedInScope( boolean usePassedInScope ){
        _forceUsePassedInScope = usePassedInScope;
    }

    public Boolean setUsePassedInScopeTL( Boolean usePassedInScopeTL ){
        Boolean old = _forceUsePassedInScopeTL.get();
        _forceUsePassedInScopeTL.set( usePassedInScopeTL );
        return old;
    }

    synchronized Object _cache( Scope s , long cacheTime , Object args[] ){
        if ( _callCache == null )
            _callCache = new LRUCache<Long,Pair<Object,String>>( 1000 * 3600 );
        
        final long hash = JSInternalFunctions.hash( args );

        Pair<Object,String> p = _callCache.get( hash , cacheTime );

        if ( p == null ){
            
            PrintBuffer buf = new PrintBuffer();
            getScope( true ).set( "print" , buf );
            
            p = new Pair<Object,String>();
            p.first = call( s , args );
            p.second = buf.toString();

            _callCache.put( hash , p , cacheTime  );
            clearScope();

        }
        
        JSFunction print = (JSFunction)(s.get( "print" ));
        if ( print == null )
            throw new JSException( "print is null" );
        print.call( s , p.second );

        return p.first;
    }
    
    public Object callAndSetThis( Scope s , Object obj , Object args[] ){
        s.setThis( obj );
        try {
            return call( s , args );
        }
        finally {
            s.clearThisNormal( null );
        }
    }

    public Language getSourceLanguage(){
        return _sourceLanguage;
    }

    private final Scope _scope;
    private final ThreadLocal<Scope> _tlScope = new ThreadLocal<Scope>();
    private boolean _forceUsePassedInScope = false;
    private final ThreadLocal<Boolean> _forceUsePassedInScopeTL = new ThreadLocal<Boolean>();
    

    protected JSObject _prototype;
    protected Language _sourceLanguage = Language.JS;

    protected JSArray _arguments;
    protected String _name = "NO NAME SET";
    
    private LRUCache<Long,Pair<Object,String>> _callCache;

    public static JSFunction _call = new ed.js.func.JSFunctionCalls1(){
            public Object call( Scope s , Object obj , Object[] args ){
                JSFunction func = (JSFunction)s.getThis();
                return func.callAndSetThis( s , obj , args );
            }
        };
       
    static JSFunction _apply = new ed.js.func.JSFunctionCalls2(){
            public Object call( Scope s , Object obj , Object args , Object [] foo ){
                JSFunction func = (JSFunction)s.getThis();

                if ( args == null )
                    args = new JSArray();

                if( ! (args instanceof JSArray) )
                    throw new RuntimeException("second argument to Function.prototype.apply must be an array not a " + args.getClass() );

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

    static JSFunction _cache = new ed.js.func.JSFunctionCalls1(){
            public Object call( Scope s , Object cacheTimeObj , Object[] args ){
                JSFunction func = (JSFunction)s.getThis();

                long cacheTime = Long.MAX_VALUE;
                if ( cacheTimeObj != null && cacheTimeObj instanceof Number )
                    cacheTime = ((Number)cacheTimeObj).longValue();
                
                return func._cache( s , cacheTime , args );
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
        
        fcons._prototype.set( "call" , _call );
        fcons._prototype.set( "apply" , _apply );
        
        fcons._prototype.set( "cache" , _cache );

        _staticInited = true;
    }
    
    private static boolean _staticInited = false;

    static {
        JS._debugSIDone( "JSFunction" );
    }
}

