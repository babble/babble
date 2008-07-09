// JSFunction.java

package ed.js;

import ed.lang.*;
import ed.util.*;
import ed.js.engine.Scope;

/** @expose */
public abstract class JSFunction extends JSFunctionBase {

    static {
        JS._debugSIStart( "JSFunction" );
    }

    /** "JSFunction : " */
    public static final String TO_STRING_PREFIX = "JSFunction : ";

    /** Initialize this function with a default scope and name.
     * @param num The number of parameters.
     */
    public JSFunction( int num ){
        this( null , null , num );
    }

    /** Initialize this function with a given scope, name, and number of parameters.
     * @param scope Scope in which this function should run.
     * @param name This function's name.
     * @param num The number of parameters.
     */
    public JSFunction( Scope scope , String name , int num ){
        super( num );
        _scope = scope;
        _name = name;

        _prototype = new JSObjectBase();

        _init();

        set( "prototype" , _prototype );

        init();
    }

    /** Returns the number of parameters taken by this function.
     * @return The number of parameters taken by this function.
     */
    public int getNumParameters(){
        return _num;
    }

    /** Set a property or the prototype object of this function.
     * @param n The key to set.  Oddly, n can be null and it will just set the property "null". If <tt>n</tt> is "prototype", this function's prototype object will be set to <tt>b</tt>.
     * @param b The value to set.
     * @return <tt>b</tt>
     */
    public Object set( Object n , Object b ){
        if ( n != null && "prototype".equals( n.toString() ) )
            _prototype = (JSObjectBase)b;

        return super.set( n , b );
    }

    /** Creates a new object with this function as its constructor.
     * @return The newly created object.
     */
    public JSObject newOne(){
        return new JSObjectBase( this );
    }

    /** Initializes this function.  Empty method at present. */
    protected void init(){}

    /** Returns a value with a given key from this function object or this function's prototype object.
     * @param n Object to find.
     * @returns The value corresponding to the key <tt>n</tt>.
     */
    public Object get( Object n ){
        Object foo = super.get( n );
        if ( foo != null )
            return foo;
        return _prototype.get( n );
    }

    /** Set this function's name.
     * @param name Set this function's name.
     */
    public void setName( String name ){
        _name = name;
    }

    /** Returns this function's name when it has been compiled into Java.
     * @return This function's name.
     */
    public String getName(){
        return _name;
    }

    /** Returns the scope in which this function is running.
     */
    public Scope getScope(){
        return getScope( false );
    }

    /** Return this function's prototype object.
     * @param This function's prototype object.
     */
    public JSObject getPrototype(){
        return _prototype;
    }

    /** Returns the scope in which this function is running.
     * @param threadLocal if this is true, it returns a thread local scope that you can modify for your thread
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

    /** Set the thread local scope to a given scope.
     * @param tl Scope to set to.
     */
    public void setTLScope( Scope tl ){
        _tlScope.set( tl );
    }

    /** If it exists, return the thread local scope.
     * @return The thread local scope.
     */
    public Scope getTLScope(){
        return _tlScope.get();
    }

    /** Clear all objects and reset this function's scope. */
    public void clearScope(){
        Scope s = _tlScope.get();
        if ( s != null )
            s.reset();
    }

    /** Return a string representation of this function.
     * @return A string "JSFunction : " and this function's name
     */
    public String toString(){
        return TO_STRING_PREFIX + _name;
    }

    /** Returns an array of the parameter names.
     * @return An array of the parameter names.
     */
    public JSArray argumentNames(){
        if ( _arguments != null )
            return _arguments;

        JSArray temp = new JSArray();
        for ( int i=0; i<_num; i++ ){
            temp.add( "unknown" + i );
        }
        return temp;
    }

    /** Returns if this function is using a passed in scope.
     * @return If this function is using a passed in scope.
     */
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

    Object _cache( Scope s , long cacheTime , Object args[] ){

        LRUCache<Long,Pair<Object,String>> myCache = _callCache;

        if ( myCache == null ){
            myCache = new LRUCache<Long,Pair<Object,String>>( 1000 * 3600 , 100 );
            _callCache = myCache;
        }

        myCache = _callCache;

        // yes, its possible for 2 threads to create their own cache and each to use a different one, but thats ok
        // all that happens is that 2 threads both do the work.
        // but its a race condition anyway, so behavior is a bit odd no matter what

        synchronized ( myCache ){

            final long hash = JSInternalFunctions.hash( args );

            Pair<Object,String> p = myCache.get( hash , cacheTime );

            if ( p == null ){

                PrintBuffer buf = new PrintBuffer();
                getScope( true ).set( "print" , buf );

                p = new Pair<Object,String>();
                p.first = call( s , args );
                p.second = buf.toString();

                myCache.put( hash , p , cacheTime  );
                clearScope();

            }

            JSFunction print = (JSFunction)(s.get( "print" ));
            if ( print == null )
                throw new JSException( "print is null" );
            print.call( s , p.second );

            return p.first;
        }
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

    public int hashCode(){
        return System.identityHashCode( this );
    }

    private final Scope _scope;
    private final ThreadLocal<Scope> _tlScope = new ThreadLocal<Scope>();
    private boolean _forceUsePassedInScope = false;
    private final ThreadLocal<Boolean> _forceUsePassedInScopeTL = new ThreadLocal<Boolean>();


    protected JSObjectBase _prototype;
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

