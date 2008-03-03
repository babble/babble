// Scope.java

package ed.js.engine;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import ed.io.*;
import ed.js.*;
import ed.js.func.*;

public class Scope implements JSObject {

    static final boolean DEBUG = false;
    private static int ID = 1;
    
    public static Scope GLOBAL = new Scope( "GLOBAL" , JSBuiltInFunctions._myScope  );
    static {
        GLOBAL._locked = true;
        GLOBAL._global = true;
    }

    static class _NULL {
        public String toString(){
            return "This is an internal thing for Scope.  It means something is null.  You should never seen this.";
        }
    }
    static _NULL NULL = new _NULL();
    
    public Scope( String name , Scope parent ){
        this( name , parent , null );
    }
    
    public Scope( String name , Scope parent , Scope alternate ){
        this( name , parent , alternate , null );
    }

    
    public Scope( String name , Scope parent , Scope alternate , File root ){
        _name = name;
        _parent = parent;
        _root = root;
        
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
            _globalThis = new JSObjectBase();
    }

    public Scope child(){
        return child( (File)null );
    }

    public Scope child( String name ){
        return new Scope( name , this , null , null );
    }

    public Scope child( File f ){
        return new Scope( _name + ".child" , this , null , f );
    }

    public Object set( Object n , Object v ){
        return put( n.toString() , v , true );
    }
    public Object get( Object n ){
        return get( n.toString() );
    }
    
    public void removeField( Object n ){
        removeField( n.toString() );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "no" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "no" );
    }

    public Collection<String> keySet(){
        return new HashSet<String>( _objects.keySet() );
    }

    public boolean containsKey( String s ){
        throw new RuntimeException( "not sure this makes sense" );
    }

    public void removeField( String name ){
        _objects.remove( name );
    }

    public Object put( String name , Object o , boolean local ){
        if ( o != null && o instanceof String ) 
            o = new JSString( o.toString() );

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

            Scope pref = getTLPreferred();

            if ( pref != null ){
                pref._objects.put( name , o );
                return o;
            }
	    
	    if ( _lockedObject != null && _lockedObject.contains( name ) )
		throw new RuntimeException( "trying to set locked object : " + name );

            _objects.put( name , o );
            return o;
        }
        
        _parent.put( name , o , false );
        return o;
    }
    
    public Object get( String name ){
        return get( name , _alternate );
    }
    
    public Object get( String name , Scope alt ){
        return get( name , alt , null );
    }
    
    public Object get( String name , Scope alt , JSObject with[] ){
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
        
        Object foo = _objects == null ? null : _objects.get( name );
        if ( foo != null ){
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
                    if ( with != null && with.length > 0 )
                        with[0] = temp;
                    return temp.get( name );
                }
            }
        }
        
        if ( alt != null && _global ){
            if ( ! alt._global )
                throw new RuntimeException( "i fucked up" );
            return alt.get( name , null );
        }

        Scope pref = getTLPreferred();
        if ( pref != null && pref._objects.containsKey( name ) ){
            Object temp = pref._objects.get( name );
            if ( temp == NULL )
                return null;
            return temp;
        }
        
        if ( _parent == null )
            return null;
        
        return _parent.get( name , alt );
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
        if ( _global )
            return this;
        if ( _parent != null )
            return _parent.getGlobal();
        return null;
    }
    
    public Scope getParent(){
	return _parent;
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
        JSObject with[] = new JSObject[1];
        Object o = get( name , _alternate , with );
        
        if ( o == null )
            throw new NullPointerException( name );
        
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
        if ( _this.size() == 0 )
            return getGlobalThis();
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
        return _this.pop()._this;
    }

    public Object clearThisNormal( Object o ){
        //if ( _this.size() > 0 ){
            if ( DEBUG ) System.out.println( "popping this from (clearThisNormal) : " + _id );
            _this.pop();
            //}
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

    public void setGlobal( boolean g ){
        _global = g;

        if ( _global ){
            if ( _globalThis == null )
                _globalThis = new JSObjectBase();
        }
        else {
            _globalThis = null;
        }
            
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
            Convert c = new Convert( name , code );
            JSFunction f = c.get();
            if ( hasReturn != null && hasReturn.length > 0 )
                hasReturn[0] = c.hasReturn();
            return f.call( this );
        }
        catch( IOException ioe ){
            throw new RuntimeException( "weird ioexception" , ioe );
        }
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

    public boolean orSave( Object a ){

        boolean res = JSInternalFunctions.JS_evalToBool( a );
        if ( res )
            _orSave = a;

        return res;
    }

    public Object getOrSave(){
        return _orSave;
    }

    public void debug(){
        debug( 0 );
    }
    
    public void debug( int indent ){
        for ( int i=0; i<indent; i++ )
            System.out.print( "  " );
        System.out.print( _id + ":" + _name + ":" );
        if ( _global )
            System.out.print( "G:" );
        if ( _objects != null )
            System.out.print( _objects.keySet() );
        System.out.println();

        if ( _alternate != null ){
            System.out.println( "  ALT:" );
            _alternate.debug( indent + 1 );
        }
        
        if ( _parent != null )
            _parent.debug( indent + 1 );
    }

    public void lock( String s ){
	if ( _lockedObject == null )
	    _lockedObject = new HashSet<String>();
	_lockedObject.add( s );
    }

    final String _name;
    final Scope _parent;
    final Scope _alternate;
    final File _root;
    public final int _id = ID++;
    
    boolean _locked = false;
    boolean _global = false;
    
    Map<String,Object> _objects;
    Set<String> _lockedObject;
    private ThreadLocal<Scope> _tlPreferred = null;

    Stack<This> _this = new Stack<This>();
    Stack<JSObject> _with;
    Object _orSave;
    JSObject _globalThis;
    
    public void makeThreadLocal(){
        _threadLocal.set( this );
    }
    
    public static void clearThreadLocal(){
        _threadLocal.set( null );
    }

    public static Scope getThredLocal(){
        return _threadLocal.get();
    }
    
    private static ThreadLocal<Scope> _threadLocal = new ThreadLocal<Scope>();

    static class This {
        This( Object o ){
            _this = o;
        }
        
        This( Object o , String n ){
            _nThis = o;
            _nThisFunc = n;
        }
        
        // js this
        Object _this;
        // native this
        Object _nThis;
        String _nThisFunc;
    }

    private static final Object[] EMPTY_OBJET_ARRAY = new Object[0];
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    
    private static final JSFunctionCalls0 _nativeFuncCall = new JSFunctionCalls0(){
            Map< Class , Map< String , List<Method> > > _classToMethods = new HashMap< Class , Map< String , List<Method> > >();
            
            List<Method> getMethods( Class c , String n ){
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

            public Object call( Scope s , Object params[] ){
                
                final boolean debug = false;
                
                This temp = s._this.peek();
                final Object obj = temp._nThis;
                final String name = temp._nThisFunc;
                
                if ( obj == null )
                    throw new NullPointerException( "object was null.  name was:" + name );
                
                if ( debug ) System.out.println( obj.getClass() + " : " + name );

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
                    throw new NullPointerException( "no function called : " + name );
                
                throw new NullPointerException( name + " (from a [" + obj.getClass() + "])" );
            }
        };

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
            
            
            if ( ! myClass.isAssignableFrom( params[i].getClass() ) ){
                if ( debug ) System.out.println( "\t native assignement failed b/c " + myClasses[i] + " " + params[i].getClass() );
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
    

    static final Comparator<Method> _methodLengthComparator = new Comparator<Method>(){
        public int compare( Method a , Method b ){
            return a.getParameterTypes().length - b.getParameterTypes().length;
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };

    static final Comparator<Constructor> _consLengthComparator = new Comparator<Constructor>(){
        public int compare( Constructor a , Constructor  b ){
            return a.getParameterTypes().length - b.getParameterTypes().length;
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };
}
