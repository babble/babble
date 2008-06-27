// AppContext.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.appserver.jxp.JxpServlet;
import ed.appserver.jxp.JxpSource;
import ed.appserver.templates.djang10.Djang10Source;
import ed.db.DBBase;
import ed.db.DBCollection;
import ed.db.DBProvider;
import ed.db.ObjectId;
import ed.js.CoreJS;
import ed.js.JS;
import ed.js.JSArray;
import ed.js.JSDate;
import ed.js.JSFile;
import ed.js.JSFunction;
import ed.js.JSLocalFile;
import ed.js.JSObject;
import ed.js.engine.Scope;
import ed.js.func.JSFunctionCalls1;
import ed.lang.Language;
import ed.lang.StackTraceHolder;
import ed.net.httpserver.HttpRequest;
import ed.util.GitUtils;

public class AppContext {

    static final boolean DEBUG = AppServer.D;
    static final String INIT_FILES[] = new String[]{ "_init.js" , "/~~/core/init.js" };

    public AppContext( File f ){
        this( f.toString() );
    }

    public AppContext( String root ){
        this( root , guessNameAndEnv( root )[0] , guessNameAndEnv( root )[1] );
    }

    public AppContext( String root , String name , String environment ){
        this( root , new File( root ) , name , environment );
    }

    public AppContext( String root , File rootFile , String name , String environment ){
        if ( root == null )
            throw new NullPointerException( "AppContext root can't be null" );

        if ( rootFile == null )
            throw new NullPointerException( "AppContext rootFile can't be null" );
        
        if ( name == null )
            name = guessNameAndEnv( root )[0];
        
        if ( name == null )
            throw new NullPointerException( "how could name be null" );
        
        _name = name;
        _root = root;
        _rootFile = rootFile;
        
        _environment = environment;
        _gitBranch = GitUtils.hasGit( _rootFile ) ? GitUtils.getBranchOrTagName( _rootFile ) : null;

        _isGrid = name.equals( "grid" );

        _scope = new Scope( "AppContext:" + root , _isGrid ? ed.cloud.Cloud.getInstance().getScope() : Scope.newGlobal() , null , Language.JS , _rootFile );
        _scope.setGlobal( true );

        _logger = ed.log.Logger.getLogger( _name + ":" + _environment );
        _usage = new UsageTracker( _name );
        
        _baseScopeInit();

        _logger.info( "Started Context.  root:" + _root + " environment:" + environment + " git branch: " + _gitBranch );
    }

    /**
     *  Initializes the base scope for the application
     */
    private void _baseScopeInit(){
        // --- libraries
        
        _jxpObject = new JSFileLibrary( _rootFile , "jxp" , this );
        _scope.put( "jxp" , _jxpObject , true );
        _scope.put( "local" , _jxpObject , true );
        
        try {
            JxpSource config = getSource( new File( _rootFile , "_config.js" ) );
            if ( config != null )
                config.getFunction().call( _scope );
        }
        catch ( Exception e ){
            throw new RuntimeException( "couldn't load config" , e );
        }

        _core = CoreJS.get().getLibrary( JS.toString( _scope.get( "corejsversion" ) ) , this , null , true );
        _scope.put( "core" , _core , true );

        _external = Module.getModule( "external" ).getLibrary( null , this , null , true );
        _scope.put( "external" , _external , true );

        _scope.put( "_rootFile" , _rootFile , true );
        _scope.lock( "_rootFile" );

        _scope.put( "__instance__" , this , true );
        _scope.lock( "__instance__" );

        // --- db
        
        if ( ! _isGrid ){
            _scope.put( "db" , DBProvider.get( _name , this ) , true );
            _scope.put( "setDB" , new JSFunctionCalls1(){

                    public Object call( Scope s , Object name , Object extra[] ){
			if ( name.equals( _lastSetTo ) )
			    return true;
			
                        s.put( "db" , DBProvider.get( name.toString() , AppContext.this ) , false );
			_lastSetTo = name.toString();

                        return true;
                    }
		    
		    String _lastSetTo = null;

                } , true );
        }

        // --- output
        
	_scope.put( "SYSOUT" , new JSFunctionCalls1(){
		public Object call( Scope s , Object str , Object foo[] ){
		    System.out.println( AppContext.this._name + " \t " + str );
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

        Djang10Source.install(_scope);
	_scope.lock( "user" ); // protection against global user object
        
    }

    public String getVersionForLibrary( String name ){
        return getVersionForLibrary( _scope , name );
    }

    public static String getVersionForLibrary( Scope s , String name){
        JSObject o = (JSObject)s.get( "version" );
        if ( o == null )
            return null;
        
        Object v = o.get( name );
        if ( v == null )
            return null;
        return v.toString();
    }

    private static String[] guessNameAndEnv( String root ){
        String pcs[] = root.split("/");

        if ( pcs.length == 0 )
            throw new RuntimeException( "no root for : " + root );
        
        // handle anything with sites/foo
        for ( int i=0; i<pcs.length-1; i++ )
            if ( pcs[i].equals( "sites" ) ){
                return new String[]{ pcs[i+1] , i+2 < pcs.length ? pcs[i+2] : null };
            }
        
        for ( int i=pcs.length-1; i>0; i-- ){
            String s = pcs[i];
            
            if ( s.equals("master" ) || 
                 s.equals("test") || 
                 s.equals("www") || 
                 s.equals("staging") || 
                 s.equals("dev" ) )
                continue;
            
            return new String[]{ s , i + 1 < pcs.length ? pcs[i+1] : null };
        }
        
        return new String[]{ pcs[0] , null };
    }
    
    public String getName(){
        return _name;
    }
    
    DBBase getDB(){
        return (DBBase)_scope.get( "db" );        
    }

    JSFile getJSFile( String id ){

        if ( id == null )
            return null;
        
        DBCollection f = getDB().getCollection( "_files" );
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
    
    public File getFileSafe( final String uri ){
        try {
            return getFile( uri );
        }
        catch ( FileNotFoundException fnf ){
            return null;
        }
    }

    public File getFile( final String uri )
        throws FileNotFoundException {
        File f = _files.get( uri );
        
        if ( f != null )
            return f;
        
        if ( uri.startsWith( "/~~/" ) || uri.startsWith( "~~/" ) )
            f = _core.getFileFromPath( uri.substring( 3 ) );
        else if ( uri.startsWith( "/@@/" ) || uri.startsWith( "@@/" ) )
            f = _external.getFileFromPath( uri.substring( 3 ) );
        else if ( uri.startsWith( "/modules/" ) )
            f = _jxpObject.getFileFromPath( uri );
        else
            f = new File( _rootFile , uri );
        
        if ( f == null )
            throw new FileNotFoundException( uri );

        _files.put( uri , f );
        return f;
    }
    
    public void reset(){
        _reset = true;
    }
    
    public boolean isReset() { 
        return _reset;
    }
    
    public String getRoot(){
        return _root;
    }

    AppRequest createRequest( HttpRequest request ){
        return createRequest( request , request.getURI() );
    }
    
    AppRequest createRequest( HttpRequest request , String uri ){
        _numRequests++;
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

            File temp = getFileSafe( foo + ".jxp" );
            
            if ( temp != null && temp.exists() )
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
    
    JxpSource getSource( File f )
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

    JxpServlet getServlet( File f )
        throws IOException {
        JxpSource source = getSource( f );
        if ( source == null )
            return null;
        return source.getServlet( this );
    }

    private void _initScope(){
        final Scope saveTLPref = _scope.getTLPreferred();
        _scope.setTLPreferred( null );

        final Scope saveTL = Scope.getThreadLocal();
        _scope.makeThreadLocal();

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
            _scope.setTLPreferred( saveTLPref );

            if ( saveTL != null )
                saveTL.makeThreadLocal();
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
        StackTraceHolder.getInstance().fix( t );
    }

    public JSArray getGlobalHead(){
        return _globalHead;
    }

    public JSDate getWhenCreated(){
        return _created;
    }

    public int getNumRequests(){
        return _numRequests;
    }

    public String getStartupGitBranch(){
        return _gitBranch;
    }

    public String getCurrentGitBranch(){
        if ( _gitBranch == null )
            return null;
        return GitUtils.getBranchOrTagName( _rootFile );
    }
    
    public String getEnvironmentName(){
        return _environment;
    }

    final String _name;
    final String _root;
    final File _rootFile;

    final String _gitBranch;
    final String _environment;

    JSFileLibrary _jxpObject;
    JSFileLibrary _core;
    JSFileLibrary _external;
    
    final ed.log.Logger _logger;
    final Scope _scope;
    final UsageTracker _usage;
    
    final JSArray _globalHead = new JSArray();
    
    private final Map<String,File> _files = new HashMap<String,File>();
    private final Set<File> _initFlies = new HashSet<File>();

    boolean _scopeInited = false;
    boolean _inScopeInit = false;
    long _lastScopeInitTime = 0;

    final boolean _isGrid;

    boolean _reset = false;
    int _numRequests = 0;
    final JSDate _created = new JSDate();
}
