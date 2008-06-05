// Module.java

package ed.appserver;

import java.io.*;

import ed.js.*;
import ed.js.engine.*;
import ed.log.*;
import ed.util.*;

public class Module {

    static Logger _log = Logger.getLogger( "modules" );    

    public Module( File root , String uriBase , boolean doInit ){
        
        _root = root;
        _uriBase = uriBase;
        _doInit = doInit;

        _versioned = GitUtils.isSourceDirectory( _root );
        
        _default = _versioned ? new File( _root , "master" ) : _root;
        if ( ! _default.exists() )
            throw new RuntimeException( "Module root for [" + uriBase + "] does not exist : " + _default );
    }
    
    public JSFileLibrary getLibrary( String version , AppContext context , Scope scope ){
        return new JSFileLibrary( null , getRootFile( version ) , _uriBase , context , scope , _doInit );
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
        
        // need to do smart stuff here
        throw new RuntimeException( "can't find version [" + version + "] for [" + _uriBase + "]" );
    }

    final File _root;
    final String _uriBase;
    final boolean _doInit;

    final boolean _versioned;

    final File _default;
}
