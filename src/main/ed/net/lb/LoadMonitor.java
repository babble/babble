// LoadMonitor.java

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

    ThingsPerTimeTracker[] getSortedTrackers( int num ){
        List<String> all = sortedSites();
        int max = Math.min( all.size() , num );
        
        ThingsPerTimeTracker[] trackers = new ThingsPerTimeTracker[max];
        for ( int i=0; i<max; i++ )
            trackers[i] = getTrackerForSite( all.get(i) );

        return trackers;
    }

    final Router _router;
    final RollingCounter _sites = new RollingCounter( "sites" , 30 * 1000 , 60 );
    final HttpLoadTracker.Rolling _all = new HttpLoadTracker.Rolling( "lb traffic" );
}
