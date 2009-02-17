// ModuleDirectory.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.util.*;
import ed.js.*;
import ed.js.engine.*;

public class ModuleDirectory extends JSObjectLame implements JSLibrary , Sizable {
    
    public ModuleDirectory( String root , String name , AppContext context , Scope scope ){
        this( new File( Module._defaultBase , root ) , name , context , scope );
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
        
        m = new Module( _root , name , _name + "." + name , true );
        _modules.put( name , m );
        return m;
    }
    
    public synchronized JSFileLibrary getJSFileLibrary( String name ){
        JSFileLibrary lib = _libraries.get( name );
        if ( lib != null )
            return lib;

        Module m = getModule( name );
        lib = m.getLibrary( getDesiredVersion( name ) , _context , _scope , true );
        System.err.println( "created JSFileLibrary : " + name + "  auto init:" + m._doInit );
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
        
        if ( _context != null )
            return _context.getVersionForLibrary( name );

        if ( _scope != null )
            return AppContext.getVersionForLibrary( _scope , name );
        
        return null;
    }

    private String _getDesiredVersion( Scope s , String name ){
        if ( _context != null )
            return _context.getVersionForLibrary( name );
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

    public Set<String> keySet( boolean includePrototype ){
        return new HashSet<String>();
    }

    public long approxSize( SeenPath seen ){
        // this should have no real memory
        return 200;
    }
    
    final String _name;
    final File _root;
    final AppContext _context;
    final Scope _scope;

    final Map<String,Module> _modules = new HashMap<String,Module>();
    final Map<String,JSFileLibrary> _libraries = new HashMap<String,JSFileLibrary>();
    
}
