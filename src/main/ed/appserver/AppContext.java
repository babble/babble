// AppContext.java

package ed.appserver;

import ed.js.engine.*;
import ed.net.httpserver.*;

public class AppContext {

    public AppContext( String root ){
        _root = root;

        _realScope = Scope.GLOBAL.child();
        
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

    private void _initScope(){
        
    }

    final String _root;
    final Scope _realScope;
    final Scope _publicScope;

    boolean _scopeInited = false;
}
