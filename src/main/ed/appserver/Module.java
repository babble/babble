// Module.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;

public class Module {

    static final Logger _log = Logger.getLogger( "modules" );    
    public static final String _baseFile; 
    
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
        
        _versioned = ! GitUtils.isSourceDirectory( _root );
        
        _default = _versioned ? new File( _root , "master" ) : _root;
        
        if ( ! _root.exists() && _giturl != null ){
            _root.mkdirs();
            GitUtils.clone( _giturl , _root , "master" );
        }
        
        if ( ! _default.exists() )
            throw new RuntimeException( "Module root for [" + uriBase + "] does not exist : " + _default + " giturl [" + _giturl + "]" );

        _lock = ( "Module-LockKey-" + _root.getAbsolutePath() ).intern();
    }

    String _findGitUrl(){
        final String str = _root.toString();
        if ( str.contains( "/core-modules/" ) || 
             str.contains( "/site-modules/" ) ){
            
            int idx = str.indexOf( "/core-modules/" );
            if ( idx < 0 )
                idx = str.indexOf( "/site-modules/" );
            
            return "ssh://git.10gen.com/data/gitroot" + str.substring( idx );
        }

        if ( str.endsWith( "/corejs" ) )
            return "ssh://git.10gen.com/data/gitroot/corejs";

        return null;
    }

    public synchronized JSFileLibrary getLibrary( String version , AppContext context , Scope scope , boolean pull ){
        synchronized ( _lock ){
            File f = getRootFile( version );
            if ( pull )
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
    
    public File getRootFile( String version ){
        if ( version == null )
            return _default;

        if ( ! _versioned ){
            _log.getLogger( version ).info( "Module [" + _uriBase + "] want version [" + version + "] but not in versioned mode" );
            return _root;
        }

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

    final boolean _versioned;

    final File _default;

    final String _lock;
}
