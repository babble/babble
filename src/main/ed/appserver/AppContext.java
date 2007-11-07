// AppContext.java

package ed.appserver;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.appserver.jxp.*;

public class AppContext {

    public AppContext( File f ){
        this( f.toString() );
    }

    public AppContext( String root ){
        this( root , guessName( root ) );
    }

    public AppContext( String root , String name ){
        _name = name;
        _root = root;
        _rootFile = new File( _root );
        _jxpObject = new JxpObject( _rootFile );

        _scope = new Scope( "AppContext:" + root , Scope.GLOBAL );
        
        _scope.put( "jxp" , _jxpObject , true );
        _scope.put( "db" , new ed.db.DBJni( _name , System.getenv("db_ip") ) , true );

        _scope.setGlobal( true );
    }

    private static String guessName( String root ){
        String pcs[] = root.split("/");

        if ( pcs.length == 0 )
            throw new RuntimeException( "no root for : " + root );
        
        for ( int i=pcs.length-1; i>0; i-- ){
            String s = pcs[i];

            if ( s.equals("master" ) || 
                 s.equals("test") || 
                 s.equals("staging") || 
                 s.equals("dev" ) )
                continue;
            
            return s;
        }
        
        return pcs[0];
    }
    
    public String getName(){
        return _name;
    }

    Scope scopeChild(){
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

    File tryNoJXP( File f ){
        if ( f.exists() )
            return f;

        if ( f.toString().indexOf( "." ) >= 0 )
            return f;
        
        File temp = new File( f.toString() + ".jxp" );
        return temp.exists() ? temp : f;
    }

    File tryServlet( File f ){
        if ( f.exists() )
            return f;
        
        String uri = f.toString().substring( _rootFile.toString().length() );
        while ( uri.startsWith( "/" ) )
            uri = uri.substring( 1 );
        
        int start = 0;
        while ( true ){

            int idx = uri.indexOf( "/" , start );
            if ( idx < 0 )
                break; 
            String foo = uri.substring( 0 , idx );
            File temp = getFile( foo + ".jxp" );

            if ( temp.exists() )
                f = temp;
            
            start = idx + 1;
        }

        return f;
    }
    
    public JxpSource getSource( File f )
        throws IOException {
    
        f = tryNoJXP( f );
        f = tryServlet( f );

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
            if ( f.exists() ){
                _initFlies.add( f );
                JxpSource s = getSource( f );
                JSFunction func = s.getFunction();
                func.call( _scope );
            }

            _lastScopeInitTime = _getScopeTime();
        }
        catch ( RuntimeException re ){
            throw re;
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
            if ( f.exists() )
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

    public String toString(){
        return _rootFile.toString();
    }

    final String _name;
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
