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

import ed.io.*;
import ed.js.*;
import ed.js.func.*;
import ed.lang.*;

public class Scope implements JSObject {

    static {
        JS._debugSIStart( "Scope" );
    }

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.SCOPE" );
    private static long ID = 1;

    private static ThreadLocal<Scope> _threadLocal = new ThreadLocal<Scope>();
    private static ThreadLocal<Scope> _lastCreated = new ThreadLocal<Scope>();

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
    
    public Scope( String name , Scope parent , Scope alternate , Language lang ){
        this( name , parent , alternate , lang , null );
    }

    
    public Scope( String name , Scope parent , Scope alternate , Language lang , File root ){
        if ( DEBUG ) System.err.println( "Creating scope with name : " + name + "\t" + _id );
        _name = name;
        _parent = parent;
        _root = root;
        _lang = lang;

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
        
        _lastCreated.set( this );
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
    
    public Object removeField( Object n ){
        return removeField( n.toString() );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "no" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "no" );
    }

    public Collection<String> keySet(){
        if ( _objects == null )
            return new HashSet<String>();
        return new HashSet<String>( _objects.keySet() );
    }

    public boolean containsKey( String s ){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public Object removeField( String name ){
        return _objects.remove( name );
    }
    
    public void putExplicit( String name , Object o ){

        if ( _locked )
            throw new RuntimeException( "locked" );
        
        if ( _killed )
            throw new RuntimeException( "killed" );

        if ( _objects == null )
            _objects = new TreeMap<String,Object>();
        _mapSet( name , o );
    }
    
    public Object put( String name , Object o , boolean local ){

        o = JSInternalFunctions.fixType( o );

        if ( _locked )
            throw new RuntimeException( "locked" );

        if ( _with != null ){
            for ( int i=_with.size()-1; i>=0; i-- ){
                JSObject temp = _with.get( i );
                if ( temp.containsKey( name ) ){
                    return temp.set( name , o );
                }
            }
        }

        if ( _killed ){
            if  ( _parent == null )
                throw new RuntimeException( "already killed and no parent" );
            return _parent.put( name , o , local );
        }
        
        if ( local
             || _parent == null
             || _parent._locked 
             || ( _objects != null && _objects.containsKey( name ) )
             || _global
             ){
            
            if ( o == null )
                o = NULL;
            if ( _objects == null )
                _objects = new TreeMap<String,Object>();

            Scope pref = getTLPreferred();

            if ( pref != null ){
                pref._mapSet( name , o );
                return _fixNull( o );
            }
	    
	    if ( _lockedObject != null && _lockedObject.contains( name ) )
		throw new RuntimeException( "trying to set locked object : " + name );

            _mapSet( name , o );
            return _fixNull( o );
        }
        
        _parent.put( name , o , false );
        return _fixNull( o );
    }

    private final void _mapSet( String name , Object o ){
        _objects.put( name , o );
        if ( o instanceof JSObjectBase )
            ((JSObjectBase)o)._setName( name );
    }
    
    public Object get( String name ){
        return get( name , _alternate );
    }
    
    public Object get( String name , Scope alt ){
        return get( name , alt , null );
    }
    
    public Object get( final String origName , Scope alt , JSObject with[] ){
        return _get( origName , alt , with , 0  );
    }
    
    private Object _get( final String origName , Scope alt , JSObject with[] , int depth ){
        final Object r = _geti( origName , alt ,with , depth  );
        if ( DEBUG ) {
            System.out.println( "GET [" + origName + "] = " + r );
            if ( r == null && depth == 0 )
                debug();
        }
        return r;
    }
    private Object _geti( final String origName , Scope alt , JSObject with[] , int depth ){
        String name = origName;
        boolean noThis = false;

        if ( name.equals( "__puts__" ) ){
            noThis = true;
            name = "print";
        }

        boolean finder = false;
        if ( name.startsWith( "@@" ) & name.endsWith( "!!" ) ){
            name = name.substring( 2 , name.length() - 2 );
            finder = true;
            System.out.println( " finder on [" + name + "]" );
        }

        if ( "scope".equals( name ) ){
            return this;
        }

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

        Scope pref = getTLPreferred();
        if ( pref != null && pref._objects.containsKey( name ) ){

            if ( finder ) throw new ScopeFinder( name , this );
            
            return _fixNull( pref._objects.get( name ) );
        }
        
        Object foo =  _killed || _objects  == null ? null : _objects.get( name );
        if ( foo != null ){

            if ( finder ) throw new ScopeFinder( name , this );
            
            if ( foo == NULL )
                return null;
            return foo;
        }
        
        // WITH
        if ( _with != null ){
            for ( int i=_with.size()-1; i>=0; i-- ){
                JSObject temp = _with.get( i );
                if ( temp == null ) continue;
                if ( temp.containsKey( name ) ){
                    
                    if ( finder ) throw new ScopeFinder( name , this );
                    
                    if ( with != null && with.length > 0 )
                        with[0] = temp;
                    return temp.get( name );
                }
            }
        }
        
        if ( alt != null && _global ){
            if ( ! alt._global )
                throw new RuntimeException( "i fucked up" );
            return alt.get( origName , null );
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
                    
                    if ( finder )
                        throw new ScopeFinder( name , this );
                    
                    if ( foo instanceof JSFunction && with != null )
                        with[0] = pt;
                    
                    return foo;
                }
            }
        }
                
        if ( depth == 0 && ! name.equals( "print" ) ){ // TODO: this is a hack for ruby right now...
            if ( _possibleThis != null ){
                pt = _possibleThis;
                foo = _getFromThis( _possibleThis , name );
                
                if ( foo != null ){
                    if ( finder )
                        throw new ScopeFinder( name , this );
                    
                    if ( foo instanceof JSFunction && with != null )
                        with[0] = pt;
                    
                    return foo;
                }
            }
        }

        return _parent._get( origName , alt , with , depth + 1 );
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
        return _get( name , null , null , 0  );
    }

    public boolean isRuby(){
        return _lang == Language.RUBY;
    }
    
    public void enterWith( JSObject o ){
        if ( _with == null )
            _with = new Stack<JSObject>();
        _with.push( o );
    }
    
    public void leaveWith(){
        _with.pop();
    }

    public final Scope getGlobal(){
        if ( _killed )
            return _parent.getGlobal();
        if ( _global )
            return this;
        if ( _parent != null )
            return _parent.getGlobal();
        return null;
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
            JSFunction func = JSNumber.getFunction( name );
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

        return _nativeFuncCall;
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

        for ( This t : _this ){
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

    public JSFunction getConstructor(){
        return null;
    }
    
    public void putAll( Scope s ){
        if ( s == null )
            return;

        if ( s._objects == null )
            return;
        
        if ( _objects == null )
            _objects = new TreeMap<String,Object>();

        _objects.putAll( s._objects );
    }


    public Throwable currentException(){
        if ( _exceptions == null )
            return null;
        return _exceptions.peek();
    }

    public void pushException( Throwable t ){
        if ( _exceptions == null )
            _exceptions = new Stack<Throwable>();
        StackTraceHolder.getInstance().fix( t );
        _exceptions.push( t );
    }

    public Throwable popException(){
        return _exceptions.pop();
    }

    final String _name;
    final Scope _parent;
    final Scope _alternate;
    final File _root;
    final JSObjectBase _possibleThis;
    final Language _lang;
    public final long _id = ID++;
    
    boolean _locked = false;
    boolean _global = false;
    boolean _killed = false;
    
    Map<String,Object> _objects;
    Set<String> _lockedObject;
    private ThreadLocal<Scope> _tlPreferred = null;

    Stack<This> _this = new Stack<This>();
    Stack<Throwable> _exceptions;
    Stack<JSObject> _with;
    Object _orSave;
    Object _andSave;
    JSObject _globalThis;


    
    public void makeThreadLocal(){
        _threadLocal.set( this );
    }
    
    public static void clearThreadLocal(){
        _threadLocal.set( null );
        _lastCreated.set( null );
    }
    
    public static Scope getThreadLocal(){
        return _threadLocal.get();
    }
    
    public static Scope getLastCreated(){
        return _lastCreated.get();
    }

    public static Scope getAScope(){
        return getAScope( true , false );
    }

    public static Scope getAScope( boolean createIfNeeded , boolean lastCreated ){
        Scope s = getThreadLocal();
        if ( s != null )
            return s;
        
        if ( ! createIfNeeded ){
            if ( lastCreated )
                return _lastCreated.get();
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

    private static final Object[] EMPTY_OBJET_ARRAY = new Object[0];
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    
    private static final JSFunctionCalls0 _nativeFuncCall = new JSFunctionCalls0(){

            public Object call( Scope s , Object params[] ){
                
                final boolean debug = false;
                
                This temp = s._this.peek();
                if ( temp._this != null )
                    throw new RuntimeException( "why is this._this is not null : " + temp._this + " s:" + s.hashCode() );
                final Object obj = temp._nThis;
                final String name = temp._nThisFunc;
                
                if ( obj == null )
                    throw new NullPointerException( "object was null.  name was:" + name );
                
                if ( debug ) System.out.println( obj.getClass() + " : " + name );
                
                return callNative( s , obj , name , params , debug );
            }
        };

    private static final Map< Class , Map< String , List<Method> > > _classToMethods = new HashMap< Class , Map< String , List<Method> > >();
    
    private static List<Method> getMethods( Class c , String n ){
        Map<String,List<Method>> m = _classToMethods.get( c );
        if ( m == null ){
            m = new HashMap<String,List<Method>>();
            _classToMethods.put( c , m );
        }
        
        List<Method> l = m.get( n );
        if ( l != null )
            return l;
        
        l = new ArrayList<Method>();
        Method all[] = c.getMethods();
        Arrays.sort( all , Scope._methodLengthComparator );
        for ( Method method : all )
            if ( method.getName().equals( n ) )
                l.add( method );
        m.put( n , l );
        return l;
    }

    private static final Set<String> _disallowedNativeNames = new HashSet<String>();
    static {
        _disallowedNativeNames.add( "getClassLoader" );
        _disallowedNativeNames.add( "loadClass" );
    }

    public static Object callNative( Scope s , Object obj , String name , Object params[] ){
        return callNative( s , obj , name , params , false );
    }
    
    public static Object callNative( Scope s , Object obj , String name , Object params[]  , boolean debug ){
        
        if ( _disallowedNativeNames.contains( name ) )
            throw new JSException( "[" + name + "] is not allowed" );

        List<Method> methods = getMethods( obj.getClass() , name );
        if ( methods != null && methods.size() > 0 ){
            methods:
            for ( Method m : methods ){
                if ( debug ) System.out.println( "\t " + m.getName() );
                
                Object nParams[] = doParamsMatch( m.getParameterTypes() , params , debug );
                if ( nParams == null ){
                    if ( debug ) System.out.println( "\t\t boo" );
                    continue;
                }
                
                if ( debug ) System.out.println( "\t\t yay" );
                
                m.setAccessible( true );
                try {
                    Object ret = m.invoke( obj , nParams );
                    if ( ret != null ){
                        if ( ret instanceof String )
                            ret = new JSString( ret.toString() );
                        else if ( ret instanceof java.util.Date ) 
                            ret = new JSDate( (java.util.Date)ret );
                        else if ( ret instanceof java.util.Collection ){
                            if ( ! ( ret instanceof JSArray  ) ){
                                JSArray a = new JSArray();
                                for ( Object o : (Collection)ret )
                                    a.add( o );
                                ret = a;
                            }
                        }
                        else if ( ret.getClass().isArray() ){
                            if ( ret.getClass().getName().toString().length() > 3 ){ // primitive
                                JSArray a = new JSArray();
                                for ( Object o : ((Object[])ret) )
                                    a.add( o );
                                return a;
                            }
                        }
                    }
                    return ret;
                }
                catch ( InvocationTargetException e ){
                    StackTraceHolder.getInstance().fix( e.getCause() );
                    if ( e.getCause() instanceof RuntimeException )
                        throw (RuntimeException)(e.getCause());
                    if ( e.getCause() instanceof Error )
                        throw (Error)e.getCause();
                    throw new RuntimeException( e.getCause() );
                }
                catch ( RuntimeException e ){
                    throw e;
                }
                catch ( Exception e ){
                    throw new RuntimeException( e );
                }
            }
            
                    throw new NullPointerException( "no method with matching params [" + name + "] (from a [" + obj.getClass() + "])" );
        }
                
        if ( obj.getClass() == JSObjectBase.class )
            throw new NullPointerException( "no function called : " + name + " fields [" + ((JSObjectBase)obj).keySet() + "]" );
        
        if ( obj instanceof ed.appserver.JSFileLibrary )
            throw new NullPointerException( "included file [" + ((ed.appserver.JSFileLibrary)obj).getURIBase().replaceAll( "^jxp" , "" )  + "/" + name + "] does not exist" );

        throw new NullPointerException( name + " (from a [" + obj.getClass() + "])" );

    }

    static Object[] doParamsMatch( Class myClasses[] , Object params[] ){
        return doParamsMatch( myClasses , params , false );
    }
    
    static Object[] doParamsMatch( Class myClasses[] , Object params[] , final boolean debug ){
        
        if ( myClasses == null )
            myClasses = EMPTY_CLASS_ARRAY;
        
        if ( params == null )
            params = EMPTY_OBJET_ARRAY;
        
        if ( myClasses.length > params.length ){
            Object n[] = new Object[myClasses.length];
            for ( int i=0; i<params.length; i++ )
                n[i] = params[i];
            params = n;
        }

        if ( myClasses.length != params.length ){
            if ( debug ) System.out.println( "param length don't match " + myClasses.length + " != " + params.length );
            return null;
        }
        
        for ( int i=0; i<myClasses.length; i++ ){

            // null is fine with me
            if ( params[i] == null ) 
                continue;
            
            Class myClass = myClasses[i];
            final Class origMyClass = myClass;
            
            if ( myClass == String.class )
                params[i] = params[i].toString();
            
            if ( myClass.isPrimitive() ){
                if ( myClass == Integer.TYPE || 
                     myClass == Long.TYPE || 
                     myClass == Double.TYPE ){
                    myClass = Number.class;
                }
                else if ( myClass == Boolean.TYPE ) 
                    myClass = Boolean.class;
            }
            
            
            if ( myClass.isArray() && params[i] instanceof JSArray ){
                params[i] = ((JSArray)params[i]).toArray();
                continue;
            }

            if ( ! myClass.isAssignableFrom( params[i].getClass() ) ){
                if ( debug ) System.out.println( "\t native assignement failed b/c " + myClasses[i] + " is not mappable from " + params[i].getClass() );
                return null;
            }
            
            if ( myClass == Number.class && origMyClass != params[i].getClass() ){
                Number theNumber = (Number)params[i];
                
                if ( origMyClass == Double.class || origMyClass == Double.TYPE )
                    params[i] = theNumber.doubleValue(); 
                else if ( origMyClass == Integer.class || origMyClass == Integer.TYPE )
                    params[i] = theNumber.intValue(); 
                else if ( origMyClass == Float.class || origMyClass == Float.TYPE )
                    params[i] = theNumber.floatValue(); 
                else if ( origMyClass == Long.class || origMyClass == Long.TYPE )
                    params[i] = theNumber.longValue(); 
                else if ( origMyClass == Short.class || origMyClass == Short.TYPE )
                    params[i] = theNumber.shortValue(); 
                else
                    throw new RuntimeException( "what is : " + origMyClass );
            }

            if ( myClass == Object.class && params[i].getClass() == JSString.class )
                params[i] = params[i].toString();

        }
        
        return params;
    }
    

    static final int compareParams( Class as[] , Class bs[] ){
        if ( as.length != bs.length )
            return as.length - bs.length;
        
        for ( int i=0; i<as.length; i++ ){
            
            Class a = as[i];
            Class b = bs[i];

            if ( a == String.class && b != String.class )
                return 1;
            if ( b == String.class && a != String.class )
                return -1;
        }

        return 0;
    }

    static final Comparator<Method> _methodLengthComparator = new Comparator<Method>(){
        public int compare( Method a , Method b ){
            return compareParams( a.getParameterTypes() , b.getParameterTypes() );
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };

    static final Comparator<Constructor> _consLengthComparator = new Comparator<Constructor>(){
        public int compare( Constructor a , Constructor  b ){
            return compareParams( a.getParameterTypes() , b.getParameterTypes() );
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };

    public static class ScopeFinder extends RuntimeException {
        ScopeFinder( String name , Scope scope ){
            super( "Finder found [" + name + "] in " + scope.toString() );
            _name = name;
            _scope = scope;
        }

        public String getName(){
            return _name;
        }

        public Scope getScope(){
            return _scope;
        }

        final String _name;
        final Scope _scope;
    }

    static {
        JS._debugSIDone( "Scope" );
    }
}
