// JSFileLibrary.java

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

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.security.*;
import ed.appserver.jxp.*;
import ed.db.JSHook;
import ed.util.*;

public class JSFileLibrary extends JSFunctionCalls0 implements JSLibrary {
    
    static final boolean D = Boolean.getBoolean( "DEBUG.JSFL" );
    static final boolean DS = Boolean.getBoolean( "DEBUG.JSFLB" ) || D;
    static final boolean DP = Boolean.getBoolean( "DEBUG.PATHING" ) || D;

    public static final boolean INIT_BY_DEFAULT = false;

    /**
     *  Helper function to load a core appserver library.  Note that 
     *  "src/main/" is the assumed starting point for specifying the library location
     *  
     * @param location Root of library.  E.g. "ed/db"
     * @param uriBase optional package name
     * @param scope 
     * @return Library or null if can't be found
     */
    public static JSFileLibrary loadLibraryFromEd(String location,  String uriBase, Scope scope){ 
        String root = JSHook.whereIsEd;
        if ( root == null ) 
            root = "";
        else
            root += "/";
        root += "src/main/" + location;	    
        File rootFile = new File( root );
        if ( ! rootFile.exists() ){
            System.out.println( "does not exist [" + rootFile + "]" );
            return null;
        }	
        return new JSFileLibrary( rootFile , uriBase, scope);
    }
    
    public JSFileLibrary( File base , String uriBase , AppContext context ){
        this( null , base , uriBase , context , null , INIT_BY_DEFAULT );
    }
    
    public JSFileLibrary( File base , String uriBase , Scope scope ){
        this( null , base , uriBase , null , scope , INIT_BY_DEFAULT );
    }
    
    protected JSFileLibrary( JSFileLibrary parent , File base , String uriBase , AppContext context , Scope scope , boolean doInit ){

        
        _parent = parent;
        _base = base;
        _uriBase = uriBase;
        _context = context;
        _scope = scope;
        _doInit = doInit;
        
        if ( DS ) System.out.println( "creating : " + _base );

        
        if ( uriBase != null ){
            if ( uriBase.equals( "core" ) && ! doInit )
                throw new RuntimeException( "you are stupid" );
            
            // this is ugly - please fix
            if ( uriBase.equals( "core" ) )
                set( "modules" , new ModuleDirectory( "core-modules" , "core.modules" , context , scope ) );
            else if ( uriBase.equals( "local" ) || uriBase.equals( "jxp" ) )
                set( "modules" , new ModuleDirectory( "site-modules" , "local.modules" , context , scope ) );
        }

                            
    }

    private synchronized void _init(){
        
        if ( D ) System.out.println( "\t " + _base + " _init.  _initSources : " + _initSources );
        
        if ( ! _doInit ){
            if ( D ) System.out.println( "\t skipping becuase no _doInit" );
            return;
        }
        
        if ( _inInit ){
            if ( D ) System.out.println( "\t skipping becuase _inInit" );
            return;
        }
        
        boolean somethingChanged = false;

        long initTime = 0;

        Object init = get( "_init" , false );
        if ( init != _initFunction )
            somethingChanged = true;
        else {
            
            for ( JxpSource source : _initSources )
                initTime = Math.max( initTime , source.lastUpdated() );
            
            if ( initTime > _lastInit )
                somethingChanged = true;
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
                if ( D ) System.out.println( "\t\t\t runnning _init" );

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

            _lastInit = _maxInitTime();
        }
        finally {
            _inInit = false;
            _initStack.get().pop();
        }
        
    }

    private long _maxInitTime(){
        long initTime = 0;
        for ( JxpSource source : _initSources )
            initTime = Math.max( initTime , source.lastUpdated() );
        return initTime;
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
                addPath( func , this );
                foo = func;
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
        }
        return foo;
    }

    public File getFileFromPath( final String path ){
        _init();
        Object o = getFromPath( path , false );

        if ( o instanceof File )
            return (File)o;
        
        if ( o instanceof JSFileLibrary ){
            File f = ((JSFileLibrary)o)._base;
            getFileFromPath( path + "/index.jxp" );
            return f;
        }

        JxpSource js = null;

        if ( o instanceof JxpSource )
            js = (JxpSource)o;
        else if ( o instanceof JSObject )
            js = (JxpSource)((JSObject)o).get( JxpSource.JXP_SOURCE_PROP );
        
        if ( js == null )
            return null;
        
        File f = js.getFile();
        
        if ( f != null )
            _fileCache.put( f , js );

        return f;
    }

    public Object getFromPath( String path ){
        return getFromPath( path , true );
    }

    public Object getFromPath( String path , boolean evalToFunction ){
        if ( path == null || path.trim().length() == 0 ){
            return this;
        }
        
        _init();

        path = cleanPath( path );

        if ( path.contains( ".." ) )
            throw new RuntimeException( "can't have .. in paths [" + path + "]" );
        
        path = path.replaceAll( "/+" , "/" );

        if ( path.startsWith( "/" ) ){
            JSFileLibrary root = this;
            while ( root._parent != null )
                root = root._parent;
            return root.getFromPath( path.substring( 1 ) , evalToFunction );
        }

        final int idx = path.indexOf( "/" );
        if ( idx < 0 )
            return evalToFunction ? get( path ) : _get( path );

        final String dir = path.substring( 0 , idx );
        final String next = path.substring( idx + 1 );
        
        Object foo = get( dir );
        if ( foo == null )
            return null;
        
        if ( ! ( foo instanceof JSLibrary ) )
            return null;
        
        JSLibrary lib = (JSLibrary)foo;
        return lib.getFromPath( next , evalToFunction );
    }

    JSLibrary _findLibraryForFile( File f ){
        for ( String s : keySet() ){
            Object o = super.get( s );
            if ( o == null )
                continue;
            
            if ( o instanceof JSLibrary && 
                 f.getAbsolutePath().startsWith( ((JSLibrary)o).getRoot().getAbsolutePath() ) )
                return (JSLibrary)o;
        }

        return null;
    }
    
    public boolean isIn( File f ){
        
        if ( f.toString().startsWith( _base.toString() ) )
            return true;
        
        if ( _findLibraryForFile( f ) != null )
            return true;
        
        File temp = f;
        while ( temp != null ){
            if ( _fileCache.containsKey( temp ) )
                return true;
            temp = temp.getParentFile();
        }
        
        return false;
    }
    
    JxpSource getSource( File f )
        throws IOException {
        return getSource( f , true );
    }

    private JxpSource getSource( File f , boolean doInit )
        throws IOException {
        
        JxpSource source = _fileCache.get( f );
        if ( source != null )
            return source;

        if ( D ) System.out.println( "getSource.  base : " + _base + " file : " + f  + " doInit : " + doInit );
        
        String parentString = f.getParent();
        String rootString = _base.toString();
        if ( ! parentString.equals( rootString ) ){
            
            if ( ! parentString.startsWith( rootString ) ){
            
                JSLibrary lib = _findLibraryForFile( f );
                if ( lib == null )
                    throw new RuntimeException( "[" + f.getParent() + "] not a subdir of [" + _base + "] and can't find a sub-lib for it" );

                String nextPath = f.getAbsolutePath().substring( lib.getRoot().getAbsolutePath().length() );
                while ( nextPath.startsWith( "/" ) )
                    nextPath = nextPath.substring(1);
                
                Object o = lib.getFromPath( nextPath , false );
                if ( o == null )
                    throw new RuntimeException( "can't find [" + nextPath + "] from [" + _base + "]" );
                
                if ( ! ( o instanceof JxpSource ) ){
                    throw new RuntimeException( "wasn't jxp source.  was [" + o.getClass() + "]" );
                }

                return (JxpSource)o;
            }
            
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

        source = _sources.get( f );
        if ( source == null ){
            source = JxpSource.getSource( f , this );
            _sources.put( f , source );
        }
        
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
        
        if ( f == null && dir.exists() && ! dir.isDirectory() )
            f = dir;

        if ( DS ) System.out.println( "\t dir : " + dir + " f : " + f );

        Object theObject = null;
        if ( f != null ){
            try {
                if ( _srcExtensionSet.contains( MimeTypes.getExtension( f ) ) )
                    theObject = getSource( f , false );
                else {
                    theObject = f;
                }
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
        }
        
        if ( dir.exists() && dir.isDirectory() ){
            JSFileLibrary foo = new JSFileLibrary( this , dir , _uriBase + "." + n.toString() , _context , _scope , _context != null || _doInit );
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

    JSFileLibrary getTopParent(){
        if ( _parent == null )
            return this;
        return _parent.getTopParent();
    }

    String cleanPath( final String old ){
        JSFileLibrary l = getTopParent();
        if ( ! old.startsWith( l._base.toString() ) )
            return old;
        
        return old.substring( l._base.toString().length() );
    }

    public File getRoot(){
        return _base;
    }

    public String getName(){
        if ( _mySource != null )
            return _mySource.getName();
        return _base.getName();
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
    private final Map<File,JxpSource> _fileCache = new HashMap<File,JxpSource>();
    
    private final static ThreadLocal<Stack<Set<JxpSource>>> _initStack = new ThreadLocal<Stack<Set<JxpSource>>>(){
        protected Stack<Set<JxpSource>> initialValue(){
            return new Stack<Set<JxpSource>>();
        }
    };

    static String _srcExtensions[] = new String[] { ".js" , ".jxp" , ".html" , ".ruby" , ".rb" , ".rhtml" , ".erb" , ".djang10", ".txt" , ".py" };
    static final Set<String> _srcExtensionSet;
    static {
        Set<String> s = new TreeSet<String>();
        for ( String e : _srcExtensions ){
            if ( ! e.startsWith( "." ) )
                throw new RuntimeException( "blah - something broken" );
            s.add( e.substring(1) );
        }
        _srcExtensionSet = Collections.unmodifiableSet( s );
    }

    public static JSFileLibrary findPath(){
        String topjs = Security.getTopJS();
        int idx = topjs.indexOf( "$" );
        if ( idx > 0 )
            topjs = topjs.substring( 0 , idx );
        if ( DP ) System.out.println( "looking or path : " + topjs );
        JSFileLibrary lib = _classToPath.get( topjs );
        return lib;
    }

    public static void addPath( JSFunction f , JSFileLibrary lib ){
        addPath( f.getClass() , lib );
    }
    
    public static void addPath( Class c , JSFileLibrary lib ){
        if ( DP ) System.out.println( "adding to path : " + c );
        _classToPath.put( c.getName() , lib );
    }

    private static WeakValueMap<String,JSFileLibrary> _classToPath = new WeakValueMap<String,JSFileLibrary>();
   
}
