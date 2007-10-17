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

        _realScope = new MyScope( "AppContext:" + root , Scope.GLOBAL );
        
        _publicScope = _realScope.child();
        _publicScope.lock();
    }

    public Scope scope(){
        if ( _scopeInited )
            return _publicScope;
        
        synchronized ( _realScope ){
            if ( _scopeInited )
                return _publicScope;
            
            _initScope();
            
            _scopeInited = true;
        }
        return _publicScope;
    }
    
    public File getFile( String uri ){
        File f = _files.get( uri );
        if ( f == null ) {
            f = new File( getRoot() + uri );
            _files.put( uri , f );
        }
        return f;
    }
    
    public void resetScope(){
        _scopeInited = false;
        _realScope.reset();
    }
    
    public String getRoot(){
        return _root;
    }

    public AppRequest createRequest( HttpRequest request ){
        return new AppRequest( this , request  );
    }
    
    public JxpSource getSource( File f )
        throws IOException {
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
        
    }
    
    class MyScope extends Scope {
        MyScope( String name , Scope s ){
            super( name , s );
        }

        public Object get( String name ){
            if ( name.startsWith( "jxp_" ) ){
                String jxp = name.substring( 3 ).replace( '_' , '/' ) + ".jxp";
                File f = getFile( jxp );

                if ( f.exists() ){
                    try {
                        return getSource( f ).getFunction();
                    }
                    catch ( IOException ioe ){
                        throw new RuntimeException( jxp + ioe );
                    }
                }
            }
            return super.get( name );
        }
    }

    final String _root;
    final Scope _realScope;
    final Scope _publicScope;
    
    private Map<File,JxpSource> _sources = new HashMap<File,JxpSource>();
    private Map<String,File> _files = new HashMap<String,File>();
    
    boolean _scopeInited = false;
}
