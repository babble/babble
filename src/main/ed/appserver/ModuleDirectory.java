// ModuleDirectory.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;

public class ModuleDirectory extends JSObjectLame implements JSLibrary {
    
    public ModuleDirectory( String root , String name , AppContext context , Scope scope ){
        this( new File( Module._base , root ) , name , context , scope );
    }

    public ModuleDirectory( File root , String name , AppContext context , Scope scope ){
        _root = root;
        _name = name;
        _context = context;
        _scope = scope;

        if ( ! _root.exists() ){
            _root.mkdirs();
            if ( ! _root.exists() )
                throw new RuntimeException( "modules directory [" + _root + "] does not exist" );
        }
    }
    
    public File getRoot(){
        return _root;
    }
    
    public synchronized Module getModule( String name ){
        Module m = _modules.get( name );
        if ( m != null )
            return m;
        
        final File moddir = new File( _root , name );
        
        m = new Module( moddir , _name + "." + name , true );
        _modules.put( name , m );
        return m;
    }
    
    public synchronized JSFileLibrary getJSFileLibrary( String name ){
        JSFileLibrary lib = _libraries.get( name );
        if ( lib != null )
            return lib;

        Module m = getModule( name );
        lib = m.getLibrary( getDesiredVersion( name ) , _context , _scope , true );
        System.err.println( "created JSFileLibrary : " + name );
        _libraries.put( name , lib );
        return lib;
    }
    
    public Object get( Object n ){
        String s = n.toString();
        if ( s.equals( "isLoaded" ) )
            return null;
        return getJSFileLibrary( s );
    }

    public boolean isLoaded( String name ){
        return _modules.containsKey( name );
    }
    
    public String getDesiredVersion( String name ){

        if ( _scope != null )
            return _getDesiredVersion( _scope , name );

        if ( _context != null )
            return _getDesiredVersion( _context._scope , name ); // its very important this not call getScope().  that would cause an inf. loop
        
        return null;
    }

    String _getDesiredVersion( Scope s , String name ){
        return AppContext.getVersionForLibrary( s , name );
    }

    public Object getFromPath( String path , boolean evalToFunction ){
        while ( path.startsWith( "/" ) )
            path = path.substring(1);

        int idx = path.indexOf( "/" );
        
        final String libName;
        final String next;

        if ( idx > 0 ){
            libName = path.substring( 0 , idx );
            next = path.substring( idx + 1 );
        }
        else {
            libName = path;
            next = null;
        }
        
        JSFileLibrary lib = getJSFileLibrary( libName );

        if ( next == null )
            return lib;
        return lib.getFromPath( next , evalToFunction );
    }
    
    final String _name;
    final File _root;
    final AppContext _context;
    final Scope _scope;

    final Map<String,Module> _modules = new HashMap<String,Module>();
    final Map<String,JSFileLibrary> _libraries = new HashMap<String,JSFileLibrary>();
    
}
