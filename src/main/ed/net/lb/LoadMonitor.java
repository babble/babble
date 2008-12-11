// LoadMonitor.java

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

package ed.net.lb;

import java.util.*;

import ed.net.*;
import ed.net.httpserver.*;
import ed.util.*;

public class LoadMonitor {

    LoadMonitor( Router r ){
        _router = r;
    }
    
    public void hit( HttpRequest request , HttpResponse response ){
        _all.hit( request , response );
        Router.Pool p = _router.getPool( request );
        if ( p == null )
            return;
        p._tracker.hit( request , response );
    }

    void hitSite( String siteName ){
        _sites.hit( siteName );
    }

    HttpLoadTracker getTrackerForPool( String name ){
	return _router.getPool( name )._tracker;
    }
    
    ThingsPerTimeTracker getTrackerForSite( String sitename ){
        return _sites.getTimeTrackerForOne( sitename );
    }

    List<String> sortedSites(){
        return _sites.sorted( .95 );
    }

    final Router _router;
    final RollingCounter _sites = new RollingCounter( "sites" , 30 * 1000 , 60 );
    final HttpLoadTracker.Rolling _all = new HttpLoadTracker.Rolling( "lb traffic" );
}
