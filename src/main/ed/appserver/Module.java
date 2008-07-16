// Module.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;
import ed.cloud.*;

public class Module {

    static final Logger _log = Logger.getLogger( "modules" );    
    public static final String _baseFile; 
    public static final String GITROOT = "ssh://git.10gen.com/data/gitroot/";
    public static final boolean USE_GIT = ! Config.get().getBoolean( "NO-GIT" );
    

    static { 
        String s = System.getenv( "BASE" ) == null ? "/data/" : System.getenv( "BASE" );
        if (s != null && !s.endsWith("/")) {
            s += "/";
        }
        _baseFile = s;
    }
    static File _base = new File( _baseFile );

    public static synchronized Module getModule( String name ){
        Module m = _modules.get( name );
        if ( m != null )
            return m;
        
        m = new Module( name );
        _modules.put( name , m );
        return m;
    }
    private static final Map<String,Module> _modules = new HashMap<String,Module>();

    public Module( String uriBase ){
        this( uriBase , uriBase , JSFileLibrary.INIT_BY_DEFAULT );
    }

    public Module( String root , String uriBase , boolean doInit ){
        this( new File( _base , root ) , uriBase , doInit );
    }

    public Module( File root , String uriBase , boolean doInit ){
        
        _root = root;
        _uriBase = uriBase;
        _doInit = doInit;
        
        _giturl = _findGitUrl();
        _moduleName = _giturl == null ? null : _giturl.substring( GITROOT.length() + 1 );
        
        if ( ! _root.exists() && _giturl != null )
            _root.mkdirs();
        
        _versioned = ! GitUtils.isSourceDirectory( _root );
        
        
        if ( _versioned ){
            String defaultVersion = getSymLink( "stable" );
            if ( defaultVersion == null )
                defaultVersion = "master";
            
            _default = getRootFile( defaultVersion );
        }
        else {
            _default = _root;
        }
        
	if ( ! _default.exists() )
            throw new RuntimeException( "Module root for [" + uriBase + "] does not exist : " + _default + " giturl [" + _giturl + "]" );
        
        _lock = ( "Module-LockKey-" + _root.getAbsolutePath() ).intern();
    }
    
    String _findGitUrl(){
        if ( ! USE_GIT )
            return null;
        
        final String str = _root.toString();
        if ( str.contains( "/core-modules/" ) || 
             str.contains( "/site-modules/" ) ){
            
            int idx = str.indexOf( "/core-modules/" );
            if ( idx < 0 )
                idx = str.indexOf( "/site-modules/" );
            
            return GITROOT + str.substring( idx );
        }

        if ( str.endsWith( "/corejs" ) )
            return GITROOT + "/corejs";

        return null;
    }

    public synchronized JSFileLibrary getLibrary( String version , AppContext context , Scope scope , boolean pull ){
        synchronized ( _lock ){
            File f = getRootFile( version );
            if ( pull && _versioned )
                GitUtils.pull( f , false );
            return new JSFileLibrary( null , f , _uriBase , context , scope , _doInit );
        }
    }
    
    /**
     *  return the base for informational purposes
     * @return
     */
    public static String getBase() {
        return _baseFile;
    }
    
    private String getSymLink( String version ){
        if ( _moduleName == null )
            return null;
        
        Cloud c = Cloud.getInstanceIfOnGrid();
        if ( c == null )
            return null;

        return c.getModuleSymLink( _moduleName , version );
    }

    public File getRootFile( String version ){
        if ( version == null )
            return _default;

        if ( ! _versioned ){
            _log.getLogger( version ).info( "Module [" + _uriBase + "] want version [" + version + "] but not in versioned mode" );
            return _root;
        }

        String symlink = getSymLink( version );
        if ( symlink != null )
            version = symlink;        

        // now try to find best match
        File f = new File( _root , version );
        if ( f.exists() )
            return f;
        
        if ( _giturl == null ){
            throw new RuntimeException( "don't have a giturl for root:[" + _root + "] uri:[" + _uriBase + "] version:[" + version + "]" );
        }

        if ( ! GitUtils.clone( _giturl , _root , version ) ){
            ed.io.FileUtil.deleteDirectory( f );
            throw new RuntimeException( "couldn't clone [" + _giturl + "]" );
        }
        
        if ( ! GitUtils.checkout( f , version ) ){
            ed.io.FileUtil.deleteDirectory( f );
            throw new RuntimeException( "couldn't checkout version [" + version + "] for [" + f + "]" );
        }
        
        return f;
    }

    final File _root;
    final String _uriBase;
    final boolean _doInit;

    final String _giturl;
    final String _moduleName;

    final boolean _versioned;

    final File _default;

    final String _lock;
}
