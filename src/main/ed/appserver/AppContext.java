// AppContext.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.db.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppContext {

    static final boolean DEBUG = AppServer.D;
    static final String INIT_FILES[] = new String[]{ "_init.js" , "/~~/core/init.js" };

    public AppContext( File f ){
        this( f.toString() );
    }

    public AppContext( String root ){
        this( root , guessName( root ) );
    }

    public AppContext( String root , String name ){
        _name = name;
        _root = root;
        _rootFile = new File( _root );

        _scope = new Scope( "AppContext:" + root , name.equals( "grid" ) ? ed.cloud.Cloud.getInstance().getScope() : Scope.GLOBAL , null , _rootFile );
        _scope.setGlobal( true );

        _logger = ed.log.Logger.getLogger( _name );
        _usage = new UsageTracker( _name );
        
        _baseScopeInit();
    }

    private void _baseScopeInit(){
        // --- libraries
        
        _jxpObject = new JSFileLibrary( _rootFile , "jxp" , this );
        _scope.put( "jxp" , _jxpObject , true );
        
        try {
            JxpSource config = getSource( new File( _rootFile , "_config.js" ) );
            if ( config != null )
                config.getFunction().call( _scope );
        }
        catch ( Exception e ){
            throw new RuntimeException( "couldn't load config" , e );
        }

        _core = new CoreJS( JS.toString( _scope.get( "corejsversion" ) ) , this );
        _scope.put( "core" , _core , true );

        _scope.put( "external" , new JSFileLibrary( new File( "/data/external" ) ,  "external" , this ) , true );

        _scope.put( "_rootFile" , _rootFile , true );
        _scope.lock( "_rootFile" );

        // --- db
        
        _scope.put( "db" , DBProvider.get( _name ) , true );
	_scope.put( "setDB" , new JSFunctionCalls1(){
		public Object call( Scope s , Object name , Object extra[] ){
		    s.put( "db" , DBProvider.get( name.toString() , false ) , false );
		    return true;
		}
	    } , true );

        // --- output
        
	_scope.put( "SYSOUT" , new JSFunctionCalls1(){
		public Object call( Scope s , Object str , Object foo[] ){
		    System.out.println( _name + " \t " + str );
		    return true;
		}
	    } , true );

        _scope.put( "log" , _logger , true );

        // --- random?
        
        _scope.put( "openFile" , new JSFunctionCalls1(){
		public Object call( Scope s , Object name , Object extra[] ){
                    return new JSLocalFile( _rootFile , name.toString() );
                }
            } , true );
        
        _scope.put( "globalHead" , _globalHead , true  );

	_scope.lock( "user" ); // protection against global user object
    }

    private static String guessName( String root ){
        String pcs[] = root.split("/");

        if ( pcs.length == 0 )
            throw new RuntimeException( "no root for : " + root );
        
        // handle anything with sites/foo
        for ( int i=0; i<pcs.length-1; i++ )
            if ( pcs[i].equals( "sites" ) )
                return pcs[i+1];
        
        for ( int i=pcs.length-1; i>0; i-- ){
            String s = pcs[i];
            
            if ( s.equals("master" ) || 
                 s.equals("test") || 
                 s.equals("www") || 
                 s.equals("staging") || 
                 s.equals("dev" ) )
                continue;
            
            return s;
        }
        
        return pcs[0];
    }
    
    public String getName(){
        return _name;
    }

    JSFile getJSFile( String id ){

        if ( id == null )
            return null;
        
        DBBase db = (DBBase)_scope.get( "db" );
        DBCollection f = db.getCollection( "_files" );
        return (JSFile)(f.find( new ObjectId( id ) ));
    }

    public Scope getScope(){
	return _scope();
    }

    Scope scopeChild(){
        Scope s = _scope().child( "AppRequest" );
        s.setGlobal( true );
        return s;
    }
    
    private synchronized Scope _scope(){
        
        if ( _getScopeTime() > _lastScopeInitTime )
            _scopeInited = false;

        if ( _scopeInited )
            return _scope;
        
        _scopeInited = true;
        _lastScopeInitTime = System.currentTimeMillis();
        
        _initScope();

        return _scope;
    }
    
    public File getFile( final String uri ){
        File f = _files.get( uri );
        
        if ( f != null )
            return f;
        
        if ( uri.startsWith( "/~~/" ) || uri.startsWith( "~~/" ) )
            f = new File( _core._base , uri.substring( 3 ) );
	else if ( uri.startsWith( "/%7E%7E/" ) )
	    f = new File( _core._base , uri.substring( 7 ) );
        else if ( uri.startsWith( "/@@/" ) || uri.startsWith( "@@/" ) )
            f = new File( "/data/external/" , uri.substring( 3 ) );
        else if ( uri.startsWith( "/%40%40/" ) )
            f = new File( "/data/external/" , uri.substring( 7 ) );
        else
            f = new File( _rootFile , uri );

        _files.put( uri , f );
        return f;
    }
    
    public void resetScope(){
        _scopeInited = false;
        _scope.reset();
        _baseScopeInit();
    }
    
    public String getRoot(){
        return _root;
    }

    public AppRequest createRequest( HttpRequest request ){
        return createRequest( request , request.getURI() );
    }
    
    public AppRequest createRequest( HttpRequest request , String uri ){
        return new AppRequest( this , request , uri );
    }

    /**
     *  Tries to find the given file, assuming that it's missing the ".jxp" extension
     *  
     * @param f  File to check
     * @return same file if not found to be missing the .jxp, or a new File w/ the .jxp appended
     */
    File tryNoJXP( File f ){
        if ( f.exists() )
            return f;

        if ( f.getName().indexOf( "." ) >= 0 )
            return f;
        
        File temp = new File( f.toString() + ".jxp" );
        return temp.exists() ? temp : f;
    }

    /**
     *    Maps a servlet-like URI to a jxp file
     *    
     *    /wiki/geir  ->  maps to wiki.jxp if exists
     *    
     * @param f File to check
     * @return new File with <root>.jxp if exists, orig file if not
     */
    File tryServlet( File f ){
        if ( f.exists() )
            return f;
        
        String uri = f.toString();
	
        if ( uri.startsWith( _rootFile.toString() ) )
            uri = uri.substring( _rootFile.toString().length() );
	
        if ( _core != null && uri.startsWith( _core._base.toString() ) )
            uri = "/~~" + uri.substring( _core._base.toString().length() );
        
        while ( uri.startsWith( "/" ) )
            uri = uri.substring( 1 );

        int start = 0;
        while ( true ){
            
            int idx = uri.indexOf( "/" , start );
            if ( idx < 0 )
                break; 
            String foo = uri.substring( 0 , idx );

            File temp = getFile( foo + ".jxp" );

            if ( temp.exists() )
                f = temp;
            
            start = idx + 1;
        }

        return f;
    }

    /**
     *   Returns the index.jxp for the File argument if it's an existing directory, 
     *   and the index.jxp file exists
     *   
     * @param f  directory to check
     * @return new File for index.jxp in that directory, or same file object if not
     */
    File tryIndex( File f ){

        if ( ! ( f.isDirectory() && f.exists() ) )
            return f;
        
        File temp = new File( f , "index.jxp" );
        if ( temp.exists() )
            return temp;
        
        return f;
    }
    
    public JxpSource getSource( File f )
        throws IOException {
    
        if ( DEBUG ) System.err.println( "getSource\n\t " + f );
        
        File temp = _findFile(f);
        
        if ( DEBUG ) System.err.println( "\t " + temp );
        
        if (!temp.exists())
            return null;

        /*
         *   if it's a directory (and we know we can't find the index file)
         *  TODO : at some point, do something where we return an index for the dir?
         */
        if ( temp.isDirectory() )
            return null;
        
        /*
         *   if we at init time, save it as an initializaiton file
         */
        loadedFile(temp);

        
        /*
         *   Ensure that this is w/in the right tree for the context
         */
        if ( _jxpObject.isIn(temp) )
            return _jxpObject.getSource(temp);
        
        /*
         *  if not, is it core?
         */
        if ( _core.isIn(temp) )
            return _core.getSource(temp);
        
        throw new RuntimeException( "what?  can't find:" + f );
    }

    /**
     *  Finds the appropriate file for the given path. 
     *  
     *  We have a hierarchy of attempts as we try to find a file : 
     *  
     *  1) first, see if it exists as is, or if it's really a .jxp w/o the extension
     *  2) next, see if it can be deconstructed as a servlet such that /foo/bar maps to /foo.jxp
     *  3) See if we can find the index file for it if a directory
     */
    File _findFile(File f) {
        
        File temp;
        
        if ((temp = tryNoJXP(f)) != f) {
            return temp;
        }
        
        if ((temp = tryServlet(f)) != f) {
            return temp;
        }
        
        if ((temp = tryIndex(f)) != f) {
            return temp;
        }
    
        return f;
    }
    
    public void loadedFile( File f ){
        if ( _inScopeInit )
            _initFlies.add( f );
    }

    public JxpServlet getServlet( File f )
        throws IOException {
        JxpSource source = getSource( f );
        if ( source == null )
            return null;
        return source.getServlet( this );
    }

    private void _initScope(){
        final Scope save = _scope.getTLPreferred();
        _scope.setTLPreferred( null );
        
        _inScopeInit = true;
        
        try {
            for ( int i=0; i<INIT_FILES.length; i++ ){
                File f = getFile( INIT_FILES[i] );
                if ( f.exists() ){
                    _initFlies.add( f );
                    JxpSource s = getSource( f );
                    JSFunction func = s.getFunction();
                    func.call( _scope );
                }
            }

            _lastScopeInitTime = _getScopeTime();
        }
        catch ( RuntimeException re ){
            _scopeInited = false;
            throw re;
        }
        catch ( Exception e ){
            _scopeInited = false;
            throw new RuntimeException( e );
        }
        finally {
            _inScopeInit = false;
            _scope.setTLPreferred( save );
        }
        
    }
    
    long _getScopeTime(){
        long last = 0;
        for ( File f : _initFlies )
            if ( f.exists() )
                last = Math.max( last , f.lastModified() );
        return last;
    }
    

    public String toString(){
        return _rootFile.toString();
    }

    public void fix( Throwable t ){
        _jxpObject.fix( t );
        _core.fix( t );
    }

    public JSArray getGlobalHead(){
        return _globalHead;
    }

    final String _name;
    final String _root;
    final File _rootFile;

    JSFileLibrary _jxpObject;
    JSFileLibrary _core;
    
    final ed.log.Logger _logger;
    final Scope _scope;
    final UsageTracker _usage;
    
    final JSArray _globalHead = new JSArray();
    
    private final Map<String,File> _files = new HashMap<String,File>();
    private final Set<File> _initFlies = new HashSet<File>();

    boolean _scopeInited = false;
    boolean _inScopeInit = false;
    long _lastScopeInitTime = 0;
}
