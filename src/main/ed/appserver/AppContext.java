// AppContext.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppContext {

    public AppContext( String root ){
        _root = root;
        _rootFile = new File( _root );
        _jxpObject = new JxpObject( _rootFile );

        _scope = new Scope( "AppContext:" + root , Scope.GLOBAL );
        _scope.put( "jxp" , _jxpObject , true );
        _scope.setGlobal( true );
    }

    public Scope scopeChild(){
        Scope s = _scope().child();
        s.setGlobal( true );
        return s;
    }
    
    private Scope _scope(){
        
        if ( _getScopeTime() > _lastScopeInitTime )
            _scopeInited = false;

        if ( _scopeInited )
            return _scope;
        
        synchronized ( _scope ){
            if ( _scopeInited )
                return _scope;
            
            _initScope();
            
            _scopeInited = true;
        }
        return _scope;
    }
    
    public File getFile( String uri ){
        File f = _files.get( uri );
        if ( f == null ) {
            f = new File( _rootFile , uri );
            _files.put( uri , f );
        }
        return f;
    }
    
    public void resetScope(){
        _scopeInited = false;
        _scope.reset();
    }
    
    public String getRoot(){
        return _root;
    }

    public AppRequest createRequest( HttpRequest request ){
        return new AppRequest( this , request  );
    }
    
    public JxpSource getSource( File f )
        throws IOException {

        if ( _inScopeInit )
            _initFlies.add( f );

        JxpSource source = _sources.get( f );
        if ( source == null ){
            source = JxpSource.getSource( f );
            _sources.put( f , source );
        }
        return source;
    }

    public JxpServlet getServlet( File f )
        throws IOException {
        return getSource( f ).getServlet();
    }

    private void _initScope(){
        _inScopeInit = true;
        
        try {
            File f = getFile( "_init.js" );
            JxpSource s = getSource( f );
            JSFunction func = s.getFunction();
            func.call( _scope );

            _lastScopeInitTime = _getScopeTime();
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
        finally {
            _inScopeInit = false;
        }
        
    }
    
    long _getScopeTime(){
        long last = 0;
        for ( File f : _initFlies )
            last = Math.max( last , f.lastModified() );
        return last;
    }
    
    class JxpObject extends JSObjectBase {
        
        JxpObject( File base ){
            _base = base;
        }
        
        public Object get( final Object n ){
            Object foo = _get( n );
            if ( foo instanceof JxpSource ){
                try {
                    foo = ((JxpSource)foo).getFunction();
                }
                catch ( IOException ioe ){
                    throw new RuntimeException( ioe );
                }
            }
            return foo;
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
                return set( n , new JxpObject( dir ) );
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

            throw new RuntimeException( n + " not found " );
        }
        
        final File _base;
        
    }

    final String _root;
    final File _rootFile;
    final JxpObject _jxpObject;

    final Scope _scope;
    
    private final Map<File,JxpSource> _sources = new HashMap<File,JxpSource>();
    private final Map<String,File> _files = new HashMap<String,File>();
    private final Set<File> _initFlies = new HashSet<File>();

    boolean _scopeInited = false;
    boolean _inScopeInit = false;
    long _lastScopeInitTime = 0;
}
