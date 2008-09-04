// Module.java

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
import ed.js.engine.*;
import ed.io.*;
import ed.log.*;
import ed.util.*;
import ed.cloud.*;

public class Module {

    static final Logger _log = Logger.getLogger( "modules" );    
    public static final String _defaultBaseFile; 
    public static final boolean USE_GIT = ! Config.get().getBoolean( "NO-GIT" );
    

    static { 
        String s = Config.get().getProperty( "BASE" , "/data/" );
        s = System.getenv( "BASE" ) == null ? s : System.getenv( "BASE" );
        if ( s != null && !s.endsWith("/")) {
            s += "/";
        }
        _defaultBaseFile = s;
    }
    static File _defaultBase = new File( _defaultBaseFile );

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
        this( _defaultBase , root , uriBase , doInit );
    }

    public Module( File base , String name , String uriBase , boolean doInit ){
        
        _base = base;
        _name = name;
        _root = new File( base , name );
        _uriBase = uriBase;
        _doInit = doInit;

        _versioned = ! ( _root.exists() && GitUtils.isSourceDirectory( _root ) );
        
        _config = _versioned ? ModuleRegistry.getARegistry().getConfig( name ) : null;
        _giturl = _config == null || ! USE_GIT ? null : _config.getGitUrl();
        
        if ( ! _root.exists() && _giturl != null )
            _root.mkdirs();
        
        if ( _versioned ){
            String defaultVersion = _config == null ? "master" : _config.getDefaultTag();
            _default = getRootFile( defaultVersion );
        }
        else {
            _default = _root;
        }
        
	if ( ! _default.exists() )
            throw new RuntimeException( "Module root for [" + uriBase + "] does not exist : " + _default + " giturl [" + _giturl + "] config:" + _config);
        
        _lock = ( "Module-LockKey-" + _root.getAbsolutePath() ).intern();
    }
    
    public synchronized JSFileLibrary getLibrary( String version , AppContext context , Scope scope , boolean pull ){
        synchronized ( _lock ){
            File f = getRootFile( version );
            if ( pull && _versioned && GitUtils.onBranch( f ) )
                GitUtils.pull( f , false );
            return new JSFileLibrary( null , f , _uriBase , context , scope , _doInit );
        }
    }
    
    /**
     *  return the base for informational purposes
     * @return
     */
    public static String getBase() {
        return _defaultBaseFile;
    }
    
    private String getSymLink( String version ){
        if ( _config == null )
            return version;
        return _config.followSymLinks( version );
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
            throw new RuntimeException( "don't have a giturl for root:[" + _root + "] uri:[" + _uriBase + "] version:[" + version + "] config [" + _config + "]" );
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

    final File _base;
    final File _root;
    final String _name;
    
    final String _uriBase;
    final boolean _doInit;
    
    final ModuleConfig _config;
    final String _giturl;
    final boolean _versioned;

    final File _default;
    
    final String _lock;
}
