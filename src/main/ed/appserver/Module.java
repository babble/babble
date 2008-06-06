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
    static final String _baseFile = System.getenv( "BASE" ) == null ? "/data/" : System.getenv( "BASE" );
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

        _versioned = ! GitUtils.isSourceDirectory( _root );
        
        _default = _versioned ? new File( _root , "master" ) : _root;
        if ( ! _default.exists() )
            throw new RuntimeException( "Module root for [" + uriBase + "] does not exist : " + _default );
    }
    
    public JSFileLibrary getLibrary( String version , AppContext context ){
        return getLibrary( version , context , null );
    }

    public JSFileLibrary getLibrary( String version , Scope scope ){
        return getLibrary( version , null , scope );
    }

    public JSFileLibrary getLibrary( String version , AppContext context , Scope scope ){
        return new JSFileLibrary( null , getRootFile( version ) , _uriBase , context , scope , _doInit );
    }

    /**
     *  return the base for informational purposes
     * @return
     */
    public String getBase() {
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
        
        /*
          TODO: do smart stuff here
          pin to 0.0 should go to latest 0.0.x
        */
        throw new RuntimeException( "can't find version [" + version + "] for [" + _uriBase + "]" );
    }

    final File _root;
    final String _uriBase;
    final boolean _doInit;

    final boolean _versioned;

    final File _default;
}
