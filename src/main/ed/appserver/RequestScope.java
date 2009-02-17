// RequestScope.java

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

import ed.js.*;
import ed.js.engine.*;
import ed.net.httpserver.*;
import ed.util.*;

public class RequestScope extends Scope {

    RequestScope( AppContext context , AppRequest ar ){
        super( "request-scope:" + ar.getRequest().getFullURL() , context.getScope() , null , context.getScope().getLanguage() , null );
        setGlobal( true );
        _context = context;
        _appRequest = ar;
    }
    
    public AppRequest getAppRequest(){
        return _appRequest;
    }
    
    public AppContext getContext(){
        return _context;
    }

    public HttpRequest getRequest(){
        return _appRequest.getRequest();
    }

    public HttpResponse getResponse(){
        return _appRequest.getResponse();
    }
    
    protected boolean skipGoingDown(){
        AppRequest other = AppRequest.getThreadLocal();
        return other != null && _appRequest != null && other != _appRequest;
    }

    protected Object _geti( final int nameHash , final String name , Scope alt , JSObject with[] , boolean noThis , int depth ){
        if ( name.equals( "__apprequest__" ) )
            return _appRequest;
        return super._geti( nameHash , name , alt , with , noThis , depth );
    }

    public long approxSize( SeenPath seen , boolean includeChildren , boolean includeParents ){
        return 
            super.approxSize( seen , includeChildren , includeParents ) + 
            JSObjectSize.size( _context , seen , this );
    }

    void done(){
        _done = true;
    }

    final AppContext _context;
    final AppRequest _appRequest;
    final long _created = System.currentTimeMillis();
    
    private boolean _done = true;
}
