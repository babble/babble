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

    public void removeField( String name ){
        _objects.remove( name );
    }

    public Object put( String name , Object o , boolean local ){
        if ( o != null && o instanceof String ) 
            o = new JSString( o.toString() );

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
        Object o = get( name );
        if ( o == null )
            return null;
        
        if ( ! ( o instanceof JSFunction ) )
            throw new RuntimeException( "not a function : " + name );
        
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

    public Scope setThis( JSObject o ){
        _this.push( new This( o ) );
        return this;
    }

    public JSFunction getFunctionAndSetThis( final Object obj , final String name ){
        
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

    public Object eval( String code )
        throws IOException {
        return eval( code , "anon" );
    }

    public Object eval( String code , String name )
        throws IOException {
        return eval( code , name , null );
    }
    
    public Object eval( String code , String name , boolean hasReturn[] )
        throws IOException {
        Convert c = new Convert( name , code );
        JSFunction f = c.get();
        if ( hasReturn != null && hasReturn.length > 0 )
            hasReturn[0] = c.hasReturn();
        return f.call( this );
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
    Object _orSave;
    JSObject _globalThis;
    
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
                for ( Method method : c.getMethods() )
                    if ( method.getName().equals( n ) )
                        l.add( method );
                m.put( n , l );
                return l;
            }

            public Object call( Scope s , Object params[] ){
                
                This temp = s._this.peek();
                final Object obj = temp._nThis;
                final String name = temp._nThisFunc;
                
                if ( obj == null )
                    throw new NullPointerException( "object was null.  name was:" + name );

                methods:
                for ( Method m : getMethods( obj.getClass() , name ) ){
            
                    Object nParams[] = doParamsMatch( m.getParameterTypes() , params );
                    if ( nParams == null )
                        continue;
                    
                    m.setAccessible( true );
                    try {
                        Object ret = m.invoke( obj , nParams );
                        if ( ret != null ){
                            if ( ret instanceof String )
                                ret = new JSString( ret.toString() );
                            else if ( ret instanceof java.util.Date ) 
                                ret = new JSDate( (java.util.Date)ret );
			    else if ( ret instanceof java.util.Collection ){
				JSArray a = new JSArray();
				for ( Object o : (Collection)ret )
				    a.add( o );
				ret = a;
                            }
                            else if ( ret.getClass().isArray() ){
                                JSArray a = new JSArray();
                                for ( Object o : ((Object[])ret) )
                                    a.add( o );
                                return a;
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
                throw new RuntimeException( "can't find a valid native method for : " + name + " which  is a : " + obj.getClass()  );
            }
        };

    static Object[] doParamsMatch( Class myClasses[] , Object params[] ){
        
        if ( myClasses == null )
            myClasses = EMPTY_CLASS_ARRAY;
        
        if ( params == null )
            params = EMPTY_OBJET_ARRAY;
        
        if ( myClasses.length != params.length )
            return null;
        
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
                //System.out.println( "\t native assignement failed b/c " + myClasses[i] + " " + params[i].getClass() );
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
        }
        
        return params;
    }
    
}
