// JSFunction.java

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

package ed.js;

import java.util.concurrent.*;

import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.js.func.*;
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

        foo = _prototype.get( n );
        if ( foo != null )
            return foo;

        
        foo = _staticFunctions.get( n );
        if ( foo != null )
            return foo;

        return null;
    }

    public JSFunction getFunction( String name , boolean tryLower ){
	Object blah = _prototype.get( name );
	if ( blah == null && tryLower )
	    blah = _prototype.get( name.toLowerCase() );
	
	if ( blah == null )
	    return null;
	
	if ( ! ( blah instanceof JSFunction ) )
	    return null;
	
	return (JSFunction)blah;
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
        Scope s = null;
        if ( _tlScope != null ){
            s = _tlScope.get();
            if ( s != null ){
                return s;
            }
        }
        
        if ( threadLocal ){
            if ( _scope == null )
                s = new Scope( "func tl scope" , null );
            else
                s = _scope.child( "func tl scope" );
            s.setGlobal( true );
            setTLScope( s );
            return s;
        }

        return _scope;
    }

    /** Set the thread local scope to a given scope.
     * @param tl Scope to set to.
     */
    public void setTLScope( Scope tl ){
        if ( _tlScope == null )
            _tlScope = new ThreadLocal<Scope>();
        _tlScope.set( tl );
    }

    /** If it exists, return the thread local scope.
     * @return The thread local scope.
     */
    public Scope getTLScope(){
        if ( _tlScope == null )
            return null;
        return _tlScope.get();
    }

    /** Clear all objects and reset this function's scope. */
    public void clearScope(){
        if ( _tlScope != null ){
            Scope s = _tlScope.get();
            if ( s != null )
                s.reset();
        }
    }

    public String getSourceCode(){
	return null;
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
        
        if ( _forceUsePassedInScope )
            return true;
        
	if ( ! _forceUsePassedInScopeTLEver )
            return false;
        
        Boolean b = _forceUsePassedInScopeTL.get();
        return b == null ? false : b;
    }

    public void setUsePassedInScope( boolean usePassedInScope ){
        _forceUsePassedInScope = usePassedInScope;
    }

    public Boolean setUsePassedInScopeTL( Boolean usePassedInScopeTL ){
	_forceUsePassedInScopeTLEver = _forceUsePassedInScopeTLEver || usePassedInScopeTL;
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

        final long hash = JSInternalFunctions.hash( args );
        Pair<Object,String> p = null;

        synchronized ( myCache ){
            p = myCache.get( hash , cacheTime );
        }

        if ( p == null ){

            // make sure i have a real db connection
            AppRequest ar = AppRequest.getThreadLocal();
            if ( ar != null )
                ar.getContext().getDB().requestEnsureConnection();

            boolean got = false;
            try {
                _callCacheSem.acquireUninterruptibly();
                got = true;

                synchronized ( myCache ){
                    p = myCache.get( hash , cacheTime );
                }

                if ( p == null ){

                    PrintBuffer buf = new PrintBuffer();
                    getScope( true ).set( "print" , buf );

                    p = new Pair<Object,String>();
                    p.first = call( s , args );
                    p.second = buf.toString();

                    synchronized( myCache ){
                        myCache.put( hash , p , cacheTime  );
                    }
                    clearScope();
                }
            }
            finally {
                _callCacheSem.release();
            }
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

    public JSFunction synchronizedVersion(){
        final JSFunction t = this;
        final String myLock = "some-lock-" + Math.random();
        return new JSFunctionCalls0(){
            public Object call( Scope s , Object args[] ){
                synchronized ( myLock ){
                    return t.call( s , args );
                }
            }
        };
    }

    public Language getSourceLanguage(){
        return _sourceLanguage;
    }

    /** The hash code value of this function.
     * @return The hash code value of this function.
     */
    public int hashCode( IdentitySet seen ){
        return System.identityHashCode( this );
    }

    public boolean isCallable(){
        return true;
    }

    /**
     * @return null if we do'nt what the globals are, or an array of global strings
     */
    public JSArray getGlobals(){
        return _globals;
    }

    private final Scope _scope;
    private ThreadLocal<Scope> _tlScope;
    private boolean _forceUsePassedInScope = false;
    private final ThreadLocal<Boolean> _forceUsePassedInScopeTL = new ThreadLocal<Boolean>();
    private boolean _forceUsePassedInScopeTLEver = false;

    protected JSObjectBase _prototype;
    protected Language _sourceLanguage = Language.JS;

    protected JSArray _arguments;
    protected JSArray _globals;
    protected String _name = "NO NAME SET";

    private LRUCache<Long,Pair<Object,String>> _callCache;
    private Semaphore _callCacheSem = new Semaphore( 1 );

    /** @unexpose */
    public static JSFunction _call = new ed.js.func.JSFunctionCalls1(){
            public Object call( Scope s , Object obj , Object[] args ){
                JSFunction func = (JSFunction)s.getThis();
                return func.callAndSetThis( s , obj , args );
            }
        };

    static JSFunction _apply = new ed.js.func.JSFunctionCalls3(){
            public Object call( Scope s , Object obj , Object args , Object explodeArgs , Object [] foo ){
                JSFunction func = (JSFunction)s.getThis();
                
                if ( args == null )
                    args = new JSArray();

                if( ! (args instanceof JSArray) )
                    throw new RuntimeException("second argument to Function.prototype.apply must be an array not a " + args.getClass() );
                
                JSArray jary = (JSArray)args;

                if ( explodeArgs instanceof JSObject ){
                    if ( func._arguments == null )
                        throw new RuntimeException( "can't explode b/c no argument unnamed" );
                    
                    JSObject explode = (JSObject)explodeArgs;

                    for ( int i=0; i<func._arguments.size(); i++ ){
                        String name = func._arguments.get(i).toString();
                        if ( explode.containsKey( name ) ){
                            if ( jary.get( i ) != null  )
                                throw new RuntimeException( "can't have a named an array value for [" + name + "]" );
                            jary.set( i , explode.get( name ) );
                        }
                    }
                }

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
    

    private static JSObjectBase _staticFunctions = new JSObjectBase();
    static {
        _staticFunctions.set( "wrap" , Prototype._functionWrap );
        _staticFunctions.set( "bind", Prototype._functionBind );
        
        Prototype._functionWrap.lock();
        Prototype._functionBind.lock();

        _staticFunctions.set( "call" , _call );
        _staticFunctions.set( "apply" , _apply );
        _staticFunctions.set( "cache" , _cache );
        _call.lock();
        _apply.lock();
        _cache.lock();
    }
    
    public static void _init( JSFunction fcons ){

        if ( _staticFunctions == null )
            return;
        
        for ( String s : _staticFunctions.keySet() )
            fcons._prototype.set( s , _staticFunctions.get( s ) );

        fcons._prototype.dontEnumExisting();
    }


    static {
        JS._debugSIDone( "JSFunction" );
    }
}
