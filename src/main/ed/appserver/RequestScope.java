// RequestScope.java

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

import ed.js.engine.*;
import ed.net.httpserver.*;

public class RequestScope extends Scope {

    RequestScope( AppContext context , AppRequest ar ){
        super( "request-scope" , context.getScope() , null , context.getScope().getLanguage() , null );
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
    
    void done(){
        _done = true;
    }

    final AppContext _context;
    final AppRequest _appRequest;
    final long _created = System.currentTimeMillis();
    
    private boolean _done = true;
}
