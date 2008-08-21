// Scope.java

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

package ed.js.engine;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.script.*;

import ed.io.*;
import ed.js.*;
import ed.js.func.*;
import ed.lang.*;
import ed.util.*;

public final class Scope implements JSObject , Bindings {

    static {
        JS._debugSIStart( "Scope" );
    }

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.SCOPE" );
    private static long ID = 1;

    private static ThreadLocal<Scope> _threadLocal = new ThreadLocal<Scope>();

    public static Scope newGlobal(){
        return JSBuiltInFunctions.create();
    }

    public static Scope newGlobal( String name ){
        return JSBuiltInFunctions.create( name );
    }
    
    static class _NULL {
        public String toString(){
            return "This is an internal thing for Scope.  It means something is null.  You should never seen this.";
        }
    }
    static _NULL NULL = new _NULL();

    static final Object _fixNull( Object o ){
        if ( o == NULL )
            return null;
        return o;
    }

    public Scope( String name , Scope parent ){
        this( name , parent , null , Language.JS );
    }

    public Scope( String name , Scope parent , File root ){
        this( name , parent , null , Language.JS , root );
    }

    
    public Scope( String name , Scope parent , Scope alternate , Language lang ){
        this( name , parent , alternate , lang , null );
    }

    
    public Scope( String name , Scope parent , Scope alternate , Language lang , File root ){
        if ( DEBUG ) System.err.println( "Creating scope with name : " + name + "\t" + _id );
        _name = name;
        _parent = parent;
        _root = root;
        _lang = lang;
    
        _maybeWritableGlobal = getGlobal( false );
    
        {
            Object pt = alternate == null ? null : alternate.getThis( false );
            if ( pt instanceof JSObjectBase )
                _possibleThis = (JSObjectBase)pt;
            else
                _possibleThis = null;
        }
        
        Scope alt = null;
        if ( alternate != null ){
            Scope me = getGlobal();
            Scope them = alternate.getGlobal();
            if ( me != them ){
                if ( them.hasParent( me ) ){
                    alt = them;
                }
            }
        }
        _alternate = alt;

        if ( _parent == null )
            _globalThis = _createGlobalThis();
        
    }

    public Scope child(){
        return child( (File)null );
    }

    public Scope child( String name ){
        return new Scope( name , this , null , _lang , null );
    }

    public Scope child( File f ){
        return new Scope( _name + ".child" , this , null , _lang , f );
    }

    public Object set( Object n , Object v ){
        return put( n.toString() , v , true );
    }
    public Object get( Object n ){
        return get( n.toString() );
    }
    
    public Object remove( Object n ){
        return removeField( n );
    }

    public Object removeField( Object n ){
        return removeField( n.toString() );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "no" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "no" );
    }

    public Set<String> keySet(){
        return keySet( false );
    }

    public Set<String> keySet( boolean includePrototype ){
        if ( _objects == null )
            return new HashSet<String>();
        return _objects.keySet( true );
    }

    public Set<Map.Entry<String,Object>> entrySet(){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public Collection<Object> values(){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public void clear(){
        throw new RuntimeException( "can't clear a scope" );
    }

    public boolean containsKey( Object o ){
        return containsKey( o.toString() );
    }

    public boolean containsKey( String s ){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public boolean containsValue( Object o ){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public boolean isEmpty(){
        return _objects != null && ! _objects.isEmpty();
    }

    public int size(){
        if ( _objects == null )
            return 0;
        return _objects.size();
    }

    public Object removeField( String name ){
        if ( _objects == null )
            return null;
        return _objects.remove( name );
    }
    
    public void putExplicit( String name , Object o ){

        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( _killed )
            throw new RuntimeException( "killed" );

        _ensureObjectMap();

        _mapSet( name.hashCode() , name , o );
    }
    
    public Object put( String name , Object o ){
        return put( name , o , true );
    }

    public Object put( String name , Object o , boolean local ){
        _throw();
        o = JSInternalFunctions.fixType( o , false );

        if ( o == null )
            o = NULL;

        return _put( name.hashCode() , name , o , local );
    }
    
    private Object _put( final int nameHash , final String name , final Object o , final boolean local ){
        
        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( _with != null ){
            for ( int i=_with.size()-1; i>=0; i-- ){
                JSObject temp = _with.get( i );
                if ( temp.containsKey( name ) ){
                    return temp.set( name , _fixNull( o ) );
                }
            }
        }

        if ( _killed ){
            if  ( _parent == null )
                throw new RuntimeException( "already killed and no parent" );
            return _parent.put( name , o , local );
        }
        
        if ( local
             || _global
             || _parent == null
             || _parent._locked 
             || ( _objects != null && _objects.containsKey( nameHash , name ) )
             ){
            
            Scope pref = getTLPreferred();
            
            if ( pref != null ){
                pref._mapSet( nameHash , name , o );
                return _fixNull( o );
            }
	    
	    if ( _lockedObject != null && _lockedObject.contains( name ) )
		throw new RuntimeException( "trying to set locked object : " + name );

            _mapSet( nameHash , name , o );
            return _fixNull( o );
        }
        
        _parent._put( nameHash , name , o , false );
        return _fixNull( o );
    }

    private final void _mapSet( final int nameHash , final String name , final Object o ){
        _ensureObjectMap();
        _objects.put( nameHash , name , o );
        if ( o instanceof JSObjectBase )
            ((JSObjectBase)o)._setName( name );
    }
    
    public Object get( String name ){
        return get( name , _alternate );
    }
    
    public Object get( String name , Scope alt ){
        return get( name , alt , null );
    }
    
    public Object get( String name , Scope alt , JSObject with[] ){

        boolean noThis = false;
        
        if ( "scope".equals( name ) )
            return this;

        if ( "globals".equals( name ) ){
            Scope foo = this;
            while ( true ){
                if ( foo._global )
                    break;
                if ( foo._parent == null )
                    break;
                if ( foo._parent._locked )
                    break;
                foo = foo._parent;
            }
            return foo;
        }

        if ( "__path__".equals( name ) )
            return ed.appserver.JSFileLibrary.findPath();


        if ( name.equals( "__puts__" ) ){
            noThis = true;
            name = "print";
        }

	return _get( name.hashCode() , name , alt , with , noThis , 0 );
    }
    
    private Object _get( final int nameHash , final String name , Scope alt , JSObject with[] , boolean noThis , int depth ){
        final Object r = _geti( nameHash , name , alt ,with , noThis , depth  );
        if ( DEBUG ) {
            System.out.println( "GET [" + name + "] = " + r );
            if ( r == null && depth == 0 )
                debug();
        }
	
	if ( r != null && _warnedObject != null && _warnedObject.contains( name ) )
	    ed.log.Logger.getRoot().getChild( "scope" ).warn( "using [" + name + "] in scope" );

        return r;
    }
    private Object _geti( final int nameHash , final String name , Scope alt , JSObject with[] , boolean noThis , int depth ){

        Scope pref = getTLPreferred();
        if ( pref != null && pref._objects.containsKey( nameHash , name ) ){
            return _fixNull( pref._objects.get( nameHash , name ) );
        }
        
        Object foo =  _killed || _objects  == null ? null : _objects.get( nameHash , name );
        if ( foo != null )
            return _fixNull( foo );
        
        // WITH
        if ( _with != null ){
            for ( int i=_with.size()-1; i>=0; i-- ){
                JSObject temp = _with.get( i );
                if ( temp == null ) continue;
                if ( temp.containsKey( name ) ){
                    if ( with != null && with.length > 0 )
                        with[0] = temp;
                    return _fixNull( temp.get( name ) );
                }
            }
        }
        
        if ( alt != null && _global ){
            if ( ! alt._global )
                throw new RuntimeException( "i fucked up" );
            return alt.get( name , null );
        }
        
        if ( _parent == null )
            return null;
        
        if ( foo != null )
            throw new RuntimeException( "eliot is stupid" );

        // TODO: this makes lookups inside classes work
        //       this is for ruby
        //       it technically violates JS rules
        //       it should probably only work within ruby.
        //       not sure how to do that...

        JSObjectBase pt = null;
        
        if ( depth == 1 && ! noThis ){
            Object t = getThis( false );
            
            if ( t != null && t.getClass() == JSObjectBase.class ){
                JSObjectBase obj = (JSObjectBase)t;
                pt = obj;
                foo = _getFromThis( obj , name );

                if ( foo != null ){
                    if ( foo instanceof JSFunction && with != null )
                        with[0] = pt;
                    
                    return foo;
                }
            }
        }
                
        if ( depth == 0 && _possibleThis != null && ! name.equals( "print" )  ){ // TODO: this is a hack for ruby right now...
            pt = _possibleThis;
            foo = _getFromThis( _possibleThis , name );
            
            if ( foo != null ){
                
                if ( foo instanceof JSFunction && with != null )
                    with[0] = pt;
                
                return foo;
            }
        }

        return _parent._get( nameHash , name , alt , with , noThis , depth + 1 );
    }

    private Object _getFromThis( JSObjectBase t , String name ){
        if ( t == null )
            return null;

        if ( ! isRuby() )
            return null;
        
        Object o = t.get( name );
        if ( o == null && t.getConstructor() != null )
            o = t.getConstructor().get( name );
        
        if ( o == null )
            return null;
        
        return o;
    }

    public Object getOrThis( String name ){
        return _get( name.hashCode() , name , null , null , false , 0 );
    }

    public boolean isRuby(){
        return _lang == Language.RUBY;
    }
    
    public void enterWith( JSObject o ){
        if ( _with == null )
            _with = new SimpleStack<JSObject>();
        _with.push( o );
    }
    
    public void leaveWith(){
        _with.pop();
    }

    public final Scope getGlobal(){
        return _maybeWritableGlobal;
    }
    
    public final Scope getGlobal( boolean writable ){
        if ( ! writable && _maybeWritableGlobal != null )
            return _maybeWritableGlobal;

        if ( _killed )
            return _parent.getGlobal();
        if ( _global )
            return this;
        if ( _parent == null )
	    return this;
	
	if ( _parent._locked && writable )
	    return this;

	return _parent.getGlobal();
    }

    public Scope getParent(){
	return _parent;
    }

    public JSObject getSuper(){
        return getParent();
    }

    /**
     * @return true if s is a parent of this
     */
    public final boolean hasParent( Scope s ){
        if ( this == s )
            return true;
        if ( _parent == null )
            return false;
        return _parent.hasParent( s );
    }

    public Scope getTLPreferred(){
        if ( _tlPreferred == null )
            return null;
        return _tlPreferred.get();
    }
    

    public void setTLPreferred( Scope from , Scope s ){
        // this is a hack for the NativeBridge
        setTLPreferred( s );
    }

    public void setTLPreferred( Scope s ){
	if ( s == this )
	    s = null;

        if ( s == null && _tlPreferred == null )
            return;
        
        if ( s != null ){

            if ( this != s._parent )
                throw new RuntimeException( "_tlPreferred has to be child of this" );
            
            if ( s._parent._objects == null )
                throw new RuntimeException( "this is weird" );
            
        }
        
        if ( _tlPreferred == null )
            _tlPreferred = new ThreadLocal<Scope>();
        _tlPreferred.set( s );
    }

    public JSFunction getFunction( String name ){
        //System.err.println( "getFunction : " + name );
        JSObject with[] = new JSObject[1];
        Object o = get( name , _alternate , with );
        //System.err.println( "\t" + o + "\t" + with[0] );
        
        if ( o == null ){
            if ( getParent().getThis( false ) instanceof JSObject ){
                JSObject pt = (JSObject)getParent().getThis();
                o = pt.get( name );
                if ( o instanceof JSFunction ){
                    JSFunction func = (JSFunction)o;
                    // THIS IS BROKEN  TODO: fix for JS
                    //if ( func.getSourceLanguage() == Language.RUBY || 
                    //( pt instanceof JSFunction && ((JSFunction)pt).getSourceLanguage() == Language.RUBY ) )
                    _this.push( new This( pt ) );
                    //else {
                    // = null;
                    //throw new RuntimeException( "not doing something b/c language is : " + func.getSourceLanguage() );
                    //}
                }
            }
        }

        if ( o == null ){
            throw new NullPointerException( name );
        }
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
        if ( with[0] != null )
            _this.push( new This( with[0] ) );
        
        return (JSFunction)o;
    }

    public Scope newThis( JSFunction f ){
        JSObject o = null;

        if ( f != null )
            o = f.newOne();
        else 
            o = new JSObjectBase();

        _this.push( new This( o ) );
        return this;
    }

    public Scope setThis( Object o ){
        _this.push( new This( o ) );
        return this;
    }

    public JSFunction getFunctionAndSetThis( final Object obj , final String name ){
        
        if ( obj == null )
            throw new NullPointerException( "try to get function [" + name + "] from a null object" );

        if ( DEBUG ) System.out.println( _id + " getFunctionAndSetThis.  name:" + name );
        
        if ( obj instanceof Number ){
            JSFunction func = ((JSFunction)(getFunction( "Number" ).get( name )));
            if ( func != null ){
                _this.push( new This( obj ) );
                return func;
            }
        }

        if ( obj instanceof JSObject ){
            JSObject jsobj = (JSObject)obj;
            
            Object shouldBeFunc = jsobj.get( name );
            if ( shouldBeFunc != null && ! ( shouldBeFunc instanceof JSFunction ) )
                throw new RuntimeException( name + " is not a function.  is a:" + shouldBeFunc.getClass()  );
            
            JSFunction func = (JSFunction)shouldBeFunc;
            
            if ( func != null ){
                if ( DEBUG ) System.out.println( "\t pushing js" );
                _this.push( new This( jsobj ) );
                return func;
            }
            
        }
        
        if ( DEBUG ) System.out.println( "\t pushing native" );
        _this.push( new This( obj , name ) );

        return NativeBridge._nativeFuncCall;
    }
    
    public Object getThis(){
        return getThis( true );
    }
    
    public Object getThis( boolean getGlobalIfNeeded ){
        if ( _this.size() == 0 ){
            if ( getGlobalIfNeeded )
                return getGlobalThis();
            return null;
        }
        return _this.peek()._this;
    }

    public JSObject getGlobalThis(){
        if ( _globalThis != null )
            return _globalThis;
        if ( _parent != null )
            return _parent.getGlobalThis();
        return null;
    }

    public Object clearThisNew( Object whoCares ){
        if ( DEBUG ) System.out.println( "popping this from (clearThisNew) : " + _id );
        
        Object o = _this.pop()._this;

        if ( o instanceof JSNumber )
            return ((JSNumber)o).get();

        return o;
    }

    public Object clearThisNormal( Object o ){
        if ( DEBUG ) System.out.println( "popping this from (clearThisNormal) : " + _id );
        _this.pop();
        return o;
    }

    public void lock(){
        _locked = true;
    }

    public void reset(){
        if ( _locked )
            throw new RuntimeException( "can't reset locked scope" );
        if ( _objects != null )
            _objects.clear();
        _this.clear();
    }

    public void kill(){
        _killed = true;
    }

    public void setGlobal( boolean g ){
        _global = g;

        if ( _global ){
            if ( _globalThis == null )
                _globalThis = _createGlobalThis();
            
        }
        else {
            _globalThis = null;
        }
            
    }

    public Object evalFromPath( String file )
        throws IOException {
        return evalFromPath( file , file.replaceAll( "^.*/(\\w+.js)$" , "$1" ) );
    }

    public Object evalFromPath( String file , String name )
        throws IOException {
        return eval( ClassLoader.getSystemClassLoader().getResourceAsStream( file ) , name );
    }

    public Object eval( File f )
        throws IOException {
        return eval( f , f.toString() );
    }

    public Object eval( File f , String name )
        throws IOException {
        return eval( new FileInputStream( f ) , name );
    }

    public Object eval( InputStream in , String name )
        throws IOException {
        return eval( StreamUtil.readFully( in ) , name );
    }

    public Object eval( String code ){
        return eval( code , "anon" + Math.random() );
    }

    public Object eval( String code , String name ){
        return eval( code , name , null );
    }
    
    public Object eval( String code , String name , boolean hasReturn[] ){
        try {
            
            if ( code.matches( "\\d+" ) )
                return Integer.parseInt( code );

            if ( code.matches( "\\w[\\w\\.]+\\w" ) )
                return findObject( code );
            
            // tell the Convert CTOR that we're in the context of eval so
            //  not use a private scope for the execution of this code

            Convert c = new Convert( name , code, true);
            
            if ( hasReturn != null && hasReturn.length > 0 ) {
                hasReturn[0] = c.hasReturn();
            }
            
            return c.get().call( this );
        }
        catch( IOException ioe ){
            throw new RuntimeException( "weird ioexception" , ioe );
        }
    }

    Object findObject( final String origName ){
        
        String name = origName;
        int idx;
        JSObject o = this;

        String soFar = "";

        while ( ( idx = name.indexOf( "." ) ) > 0 ){
            String a = name.substring( 0 , idx );
            
            if ( soFar.length() > 0 )
                soFar += ".";
            soFar += a;
            
            name = name.substring( idx + 1 );
            Object foo = o.get( a );
            if ( foo == null )
                throw new NullPointerException( soFar );
            
            if ( foo instanceof Number )
                return getFunction( "Number" ).get( origName );

            if ( ! ( foo instanceof JSObject ) )
                throw new JSException( soFar + " is not a JSObject" );
            
            o = (JSObject)foo;
        }
        
        if ( o == null )
            throw new NullPointerException( origName );
        
        return o.get( name );
    }
    
    /**
     * returns my root.  if i have none, returns my parent's root
     */
    public File getRoot(){
        if ( _root != null )
            return _root;
        
        if ( _parent == null )
            return null;
        
        return _parent.getRoot();
    }

    // special or/and stuff

    public boolean orSave( Object a ){

        boolean res = JSInternalFunctions.JS_evalToBool( a );
        if ( res )
            _orSave = a;

        return res;
    }
    public Object getorSave(){
        return _orSave;
    }

    public boolean andSave( Object a ){

        boolean res = ! JSInternalFunctions.JS_evalToBool( a );
        if ( res )
            _andSave = a;

        return res;
    }
    public Object getandSave(){
        return _andSave;
    }

    // ---- 

    public void debug(){
        debug( 0 );
    }
    
    public void debug( int indent ){
        debug( indent , true );
    }
    
    public void debug( int indent , boolean showKeys ){
        for ( int i=0; i<indent; i++ )
            System.out.print( "  " );
        System.out.print( toString() + ":" );
        
        if ( _global )
            System.out.print( "G" );
        if ( _killed )
            System.out.print( "K" );
        if ( _locked )
            System.out.print( "L" );
        
        System.out.print( ":" );
        if ( showKeys && _objects != null )
            System.out.print( _objects.keySet() );
        
        System.out.print( "||" );

        for ( int i=0; i<_this.size(); i++ ){
            This t = _this.get(i);
            System.out.print( t );
            System.out.print( "|" );
        }
        
        System.out.println();
        
        if ( _alternate != null ){
            System.out.println( "  ALT:" );
            _alternate.debug( indent + 1 );
        }
        
        if ( _parent != null )
            _parent.debug( indent + 1 );
    }

    public long getId(){
        return _id;
    }

    public String toString(){
        return _id + ":" + _name;
    }

    public void lock( String s ){
	if ( _lockedObject == null )
	    _lockedObject = new HashSet<String>();
	_lockedObject.add( s );
    }

    public void warn( String s ){
	if ( _warnedObject == null )
	    _warnedObject = new HashSet<String>();
	_warnedObject.add( s );
    }

    public JSFunction getConstructor(){
        return null;
    }
    
    public void putAll(Map<? extends String,? extends Object> toMerge){
        throw new RuntimeException( "not implemented" );
    }

    public void putAll( Scope s ){
        if ( s == null )
            return;

        if ( s._objects == null )
            return;
        
        _ensureObjectMap();
        _objects.putAll( s._objects );
    }


    public Throwable currentException(){
        if ( _exceptions == null )
            return null;
        return _exceptions.peek();
    }

    public void pushException( Throwable t ){
        if ( _exceptions == null )
            _exceptions = new SimpleStack<Throwable>();
        StackTraceHolder.getInstance().fix( t );
        _exceptions.push( t );
    }

    public Throwable popException(){
        return _exceptions.pop();
    }


    /**
     * this causes the scope to throw this exception on the next access
     */
    public void setToThrow( RuntimeException e ){
        _toThrow = e;
    }

    public void setToThrow( Error e ){
        _toThrowError = e;
    }

    public void clearToThrow(){
        _toThrowError = null;
        _toThrow = null;
    }

    private void _throw(){
        if ( _toThrow != null ){
            _toThrow.fillInStackTrace();
            throw _toThrow;
        }

        if ( _toThrowError != null ){
            _toThrowError.fillInStackTrace();
            throw _toThrowError;
        }

        if ( _parent == null )
            return;

        _parent._throw();
    }

    public void setRoot( String dir ){
	if ( ! ed.security.Security.isCoreJS() )
	    throw new RuntimeException( "you can't set scope root" );
	_root = new File( dir );
    }

    private void _ensureObjectMap(){
        if ( _objects == null ){
            //_objects = new HashMap<String,Object>();
            _objects = new FastStringMap();
        }
    }

    final String _name;
    final Scope _parent;
    final Scope _maybeWritableGlobal;
    final Scope _alternate;
    final JSObjectBase _possibleThis;
    final Language _lang;

    private File _root;

    public final long _id = ID++;
    
    boolean _locked = false;
    boolean _global = false;
    boolean _killed = false;
    
    FastStringMap _objects;
    Set<String> _lockedObject;
    Set<String> _warnedObject;
    private ThreadLocal<Scope> _tlPreferred = null;

    SimpleStack<This> _this = new SimpleStack<This>();
    SimpleStack<Throwable> _exceptions;
    SimpleStack<JSObject> _with;
    Object _orSave;
    Object _andSave;
    JSObject _globalThis;

    RuntimeException _toThrow;
    Error _toThrowError;
    
    public void makeThreadLocal(){
        _threadLocal.set( this );
    }
    
    public static void clearThreadLocal(){
        _threadLocal.set( null );
    }
    
    public static Scope getThreadLocal(){
        return _threadLocal.get();
    }
    
    public static Object getThreadLocal( String name , Object def){
        return getThreadLocal( name , def , false );
    }

    public static Object getThreadLocal( String name , Object def , boolean warn ){
        final Scope s = getThreadLocal();
        if ( s != null ){
            Object o = s.get( name );
            if ( o != null )
                return o;
        }
        //if ( warn ) System.out.println( "WARNING: using default for [" + name + "] has scope:" + ( s != null ) );
        return def;
    }

    public static JSFunction getThreadLocalFunction( String name , JSFunction def ){
        return (JSFunction)getThreadLocal( name , def );
    }

    public static JSFunction getThreadLocalFunction( String name , JSFunction def , boolean warn ){
        return (JSFunction)getThreadLocal( name , def , warn );
    }

    public static Scope getAScope(){
        return getAScope( true );
    }

    public static Scope getAScope( boolean createIfNeeded ){
        Scope s = getThreadLocal();
        if ( s != null )
            return s;
        
        if ( ! createIfNeeded ){
            return null;
        }

        s = newGlobal();
        s.makeThreadLocal();
        return s;
    }

    static class This {
        This( Object o ){
            _this = o;
        }
        
        This( Object o , String n ){
            _nThis = o;
            _nThisFunc = n;
        }

        public String toString(){
            if ( _this == null && _nThisFunc == null )
                return null;
            
            if ( _this == null )
                return _nThis.toString();
            return ((JSObject)_this).keySet().toString();
        }
        
        // js this
        Object _this;
        // native this
        Object _nThis;
        String _nThisFunc;
    }
    
    static JSObject _createGlobalThis(){
        JSObjectBase o = new JSObjectBase();
        o.set( "__globalThis" , true );
        return o;
    }

    static {
        JS._debugSIDone( "Scope" );
    }
}
