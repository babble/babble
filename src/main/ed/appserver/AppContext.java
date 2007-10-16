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
        return _publicScope;
    }
    
    public String getRoot(){
        return _root;
    }

    public AppRequest createRequest( HttpRequest request ){
        return new AppRequest( this , request  );
    }
        

    final String _root;
    final Scope _realScope;
    final Scope _publicScope;
}
