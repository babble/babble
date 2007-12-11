// JSFileLibrary.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.jxp.*;

public class JSFileLibrary extends JSObjectBase {
    
    public JSFileLibrary( File base , String uriBase ){
        _base = base;
        _uriBase = uriBase;
    }
    
    public Object get( final Object n ){
        
        Object foo = _get( n );
        if ( foo instanceof JxpSource ){
            try {
                JSFunction func = ((JxpSource)foo).getFunction();
                func.setName( _uriBase + "." + n.toString() );
                foo = func;
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
        }
        return foo;
    }

    public boolean isIn( File f ){
        // TODO make less slow
        return f.toString().startsWith( _base.toString() );
    }

    JxpSource getSource( File f )
        throws IOException {
        
        JxpSource source = _sources.get( f );
        if ( source == null ){
            source = JxpSource.getSource( f );
            _sources.put( f , source );
        }
        return source;

    }
    
    Object _get( final Object n ){
        Object v = super.get( n );
        if ( v != null )
            return v;
        
        if ( ! ( n instanceof JSString ) && 
             ! ( n instanceof String ) )
            return null;
        
        File dir = new File( _base , n.toString() );
        File js = new File( _base , n + ".js" );
        File jxp = new File( _base , n + ".jxp" );
        
        if ( dir.exists() && js.exists() )
            throw new RuntimeException( "can't have directory and .js with same name" );
        
        if ( dir.exists() && jxp.exists() )
            throw new RuntimeException( "can't have directory and .jxp with same name.  " + dir + "  " + jxp  );
        
        if ( js.exists() && jxp.exists() )
            throw new RuntimeException( "can't have .js and .jxp with same name" );
        
        if ( dir.exists() ){
            return set( n , new JSFileLibrary( dir , _uriBase + "." + n.toString() ) );
        }
        
        if ( jxp.exists() ){
            try {
                return set( n , getSource( jxp ) );
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
            
        }
        
        if ( js.exists() ){
            try {
                return set( n , getSource( js ) );
            }
            catch ( IOException ioe ){
                throw new RuntimeException( ioe );
            }
            
        }
        
        //throw new RuntimeException( n + " not found " );
        return null;
    }
    
    final File _base;
    final String _uriBase;
    private final Map<File,JxpSource> _sources = new HashMap<File,JxpSource>();
    
}
