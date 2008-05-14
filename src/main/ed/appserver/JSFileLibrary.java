// JSFileLibrary.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.security.*;
import ed.appserver.jxp.*;
import ed.util.*;

public class JSFileLibrary extends JSFunctionCalls0 {
    
    static final boolean D = Boolean.getBoolean( "DEBUG.JSFL" );
    static final boolean DS = Boolean.getBoolean( "DEBUG.JSFLB" ) || D;
    
    public JSFileLibrary( File base , String uriBase , AppContext context ){
        this( null , base , uriBase , context , null , false );
    }
    
    public JSFileLibrary( File base , String uriBase , Scope scope ){
        this( null , base , uriBase , null , scope , false );
    }
    
    protected JSFileLibrary( JSFileLibrary parent , File base , String uriBase , AppContext context , Scope scope , boolean doInit ){

        if ( uriBase.equals( "core" ) && ! doInit )
            throw new RuntimeException( "you are stupid" );
        
        _parent = parent;
        _base = base;
        _uriBase = uriBase;
        _context = context;
        _scope = scope;
        _doInit = doInit;
        
        if ( DS ) System.out.println( "creating : " + _base );
    }

    private synchronized void _init(){

        if ( D ) System.out.println( "\t " + _base + " _init.  _initSources : " + _initSources );

        if ( ! _doInit )
            return;
        
        if ( _inInit )
            return;

        boolean somethingChanged = false;

        Object init = get( "_init" , false );
        if ( init != _initFunction )
            somethingChanged = true;
        else {
            for ( JxpSource source : _initSources ){
                if ( source.lastUpdated() > _lastInit ){
                    somethingChanged = true;
                    break;
                }
            }
        }
        
        if ( D ) System.out.println( "\t\t somethingChanged : " + somethingChanged + " init : " + init + " _initFunction : " + _initFunction );
        
        if ( ! somethingChanged )
            return;
       
        try {
            _inInit = true;
            _initStack.get().push( _initSources );

            for ( String s : new LinkedList<String>( keySet() ) ){
                if ( s.equals( "_init" ) )
                    continue;
                
                Object thing = super.get( s );
                
                if ( thing instanceof JxpSource || 
                     thing instanceof JSFileLibrary  )
                    removeField( s );
            }
            
            for ( File f : new LinkedList<File>( _sources.keySet() ) ){
                if ( f.toString().endsWith( "/_init.js" ) )
                    continue;
                _sources.remove( f );
            }
            
            if ( init instanceof JSFunction ){
                Scope s = null;
                if ( _context != null )
                    s = _context.getScope();
                else if ( _scope != null )
                    s = _scope;
                else 
                    throw new RuntimeException( "no scope :(" );
                
                _initFunction = (JSFunction)init;
                
                Scope pref = s.getTLPreferred();
                s.setTLPreferred( null );
                try {
                    _initFunction.call( s );
                }
                catch ( RuntimeException re ){
                    set( "_init" , null ); // we need to re-ren
                    throw re;
                }
                s.setTLPreferred( pref );
            }

            _lastInit = System.currentTimeMillis();
        }
        finally {
            _inInit = false;
            _initStack.get().pop();
        }
        
    }

    public JSFunction getFunction( final Object n ){
        return (JSFunction)get( n );
    }
    
    public Object get( final Object n ){
        return get( n , true );
    }

    public synchronized Object get( final Object n , final boolean doInit ){
        if ( doInit )
            _init();

        Object foo = _get( n );
        if ( foo instanceof JxpSource ){
            JxpSource source = (JxpSource)foo;
            
            if ( ! _initStack.get().empty() )
                _initStack.get().peek().add( source );
            
            try {
                JSFunction func = source.getFunction();
                func.setName( _uriBase + "." + n.toString() );
                foo = func;
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
        }
        return foo;
    }
    
    public Object getFromPath( String path ){
        if ( path.contains( ".." ) )
            throw new RuntimeException( "can't have .. in paths" );
        
        path = path.replaceAll( "/+" , "/" );

        if ( path.startsWith( "/" ) ){
            JSFileLibrary root = this;
            while ( root._parent != null )
                root = root._parent;
            return root.getFromPath( path.substring( 1 ) );
        }

        final int idx = path.indexOf( "/" );
        if ( idx < 0 )
            return get( path );

        final String dir = path.substring( 0 , idx );
        final String next = path.substring( idx + 1 );
        
        Object foo = get( dir );
        if ( foo == null )
            return null;
        
        if ( ! ( foo instanceof JSFileLibrary ) )
            throw new RuntimeException( dir + " is not a directory" );
        
        JSFileLibrary lib = (JSFileLibrary)foo;
        return lib.getFromPath( next );
    }

    public boolean isIn( File f ){
        // TODO make less slow
        return f.toString().startsWith( _base.toString() );
    }

    JxpSource getSource( File f )
        throws IOException {
        return getSource( f , true );
    }

    private JxpSource getSource( File f , boolean doInit )
        throws IOException {
        
        if ( D ) System.out.println( "getSource.  base : " + _base + " file : " + f  + " doInit : " + doInit );
        
        String parentString = f.getParent();
        String rootString = _base.toString();
        if ( ! parentString.equals( rootString ) ){

            if ( ! parentString.startsWith( rootString ) )
                throw new RuntimeException( "[" + f.getParent() + "] not a subdir if [" + _base + "]" );
            
            String follow = parentString.substring( rootString.length() );
            while ( follow.startsWith( "/" ) )
                follow = follow.substring( 1 );

            int idx = follow.indexOf( "/" );            
            String dir = idx < 0 ? follow : follow.substring( 0 , idx );

            JSFileLibrary next = (JSFileLibrary)get( dir );
            return next.getSource( f );
        }

        if ( doInit ) _init();
        
        if ( _context != null )
            _context.loadedFile( f );

        JxpSource source = _sources.get( f );
        if ( source == null ){
            source = JxpSource.getSource( f );
            _sources.put( f , source );
        }
        
        JSFunction func = source.getFunction();
        addPath( func.getClass() , this );

        return source;

    }
    
    Object _get( final Object n ){
        if ( DS ) System.out.println( _uriBase + "\t GETTING \t [" + n + "]" );

        Object v = super.get( n );
        if ( v != null )
            return v;
        
        if ( ! ( n instanceof JSString ) && 
             ! ( n instanceof String ) )
            return null;
        
        if ( n.toString().length() == 0 )
            return this;

        File dir = new File( _base , n.toString() );
        File f = null;
        for ( int i=0; i<_srcExtensions.length; i++ ){
            File temp = new File( _base , n + _srcExtensions[i] );

            if ( ! temp.exists() )
                continue;
            
            if ( f != null )
                throw new RuntimeException( "file collision on : " + dir + " " + _base + " " + n  );

            f = temp;
        }
        
        Object theObject = null;
        if ( f != null ){
            try {
                theObject = getSource( f , false );
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
        }
        
        if ( dir.exists() ){
            JSFileLibrary foo = new JSFileLibrary( this , dir , _uriBase + "." + n.toString() , _context , _scope , _doInit );
            foo._mySource = (JxpSource)theObject;
            theObject = foo;
        }

        return set( n , theObject );
    }

    public Object call( Scope s , Object args[] ){
        if ( _mySource == null )
            throw new RuntimeException("trying to call a JSFileLibrary that doesn't have a file : " + _base );
        
        JSFunction f = null;
        try {
            f = _mySource.getFunction();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "couldn't load : " + f , ioe );
        }
        
        return f.call( s , args );
    }
    
    public String toString(){
        return "{ JSFileLibrary.  _base : " + _base + "}";
    }

    public String getURIBase(){
        return _uriBase;
    }

    final JSFileLibrary _parent;
    final File _base;
    final String _uriBase;
    final AppContext _context;
    final Scope _scope;
    final boolean _doInit;
    
    private final Map<File,JxpSource> _sources = new HashMap<File,JxpSource>();
    
    private JxpSource _mySource;
    private JSFunction _initFunction;
    private boolean _inInit = false;
    private long _lastInit = 0;
    private final Set<JxpSource> _initSources = new HashSet<JxpSource>();
    
    private final static ThreadLocal<Stack<Set<JxpSource>>> _initStack = new ThreadLocal<Stack<Set<JxpSource>>>(){
        protected Stack<Set<JxpSource>> initialValue(){
            return new Stack<Set<JxpSource>>();
        }
    };

    static String _srcExtensions[] = new String[] { ".js" , ".jxp" , ".html" , ".rb" , ".rhtml" , ".erb" , ".djang10", ".txt" };



    public static JSFileLibrary findPath(){
        String topjs = Security.getTopJS();
        int idx = topjs.indexOf( "$" );
        if ( idx > 0 )
            topjs = topjs.substring( 0 , idx );
        return _classToPath.get( topjs );
    }
    
    public static void addPath( Class c , JSFileLibrary lib ){
        _classToPath.put( c.getName() , lib );
    }

    private static WeakValueMap<String,JSFileLibrary> _classToPath = new WeakValueMap<String,JSFileLibrary>();
   
}
