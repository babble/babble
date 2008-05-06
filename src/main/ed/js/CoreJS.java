// CoreJS.java

package ed.js;

import java.io.*;

import ed.log.*;
import ed.js.engine.*;
import ed.appserver.*;

public class CoreJS extends JSFileLibrary {
    
    static Logger _log = Logger.getLogger( "corejs" );

    private static final File _root = new File( "/data/corejs" );
    private static final boolean _branched;
    private static final File _default;
    
    static {
        if ( ! _root.exists() )
            throw new RuntimeException( "need to have /data/corejs" );
        
        _branched = ! ( new File( _root , ".git" ) ).exists();
        
        _default = _branched ? new File( _root , "master" ) : _root;
        
        if ( ! _default.exists() )
            throw new RuntimeException( _default + " does not exist" );
    }
    
    public static String getDefaultRoot(){
        return _default.toString();
    }

    static File _getRoot( String branch ){
        if ( branch == null || branch.equals( "master" ) )
            return _default;
        
        if ( ! _branched ){
            _log.error( "want branch [" + branch + "] but running in non-branched environment.  returning default" );
            return _default;
        }

        File f = new File( _root , branch );
        if ( f.exists() )
            return f;
        
        // TODO : i should see if it exists at all, and if it does do a pull
        throw new RuntimeException( "branch [" + branch + "] doesn't exists here" );
    }
    
    public CoreJS( Scope scope ){
        this( null , scope , null );
    }

    public CoreJS( AppContext context ){
        this( null , null, context );
    }

    public CoreJS( String branch , Scope scope ){
        this( branch , scope , null );
    }

    public CoreJS( String branch , AppContext context ){
        this( branch , null, context );
    }

    CoreJS( String branch , Scope scope , AppContext context ){
        super( null , _getRoot( branch ) , "core" , context , scope , true );
    }
}
