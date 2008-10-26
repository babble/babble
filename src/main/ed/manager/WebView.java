// WebView.java

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

package ed.manager;

import ed.net.httpserver.*;

public class WebView extends HttpMonitor {
    public WebView( Manager manager ){
        super( "appManager" );
        _manager = manager;
    }

    public void handle( MonitorRequest request ){
        
        request.startData( "applications" , "type" , "id" , "uptime" , "timesStarted" );

        for ( Application app : _manager.getApplications() ){
            RunningApplication ra = _manager.getRunning( app );
            request.addData( app.getType() + "." + app.getId() , 
                             app.getType() , app.getId() , ra.getUptimeMinutes() , ra.timesStarted() );
        }

        request.endData();
    }

    final Manager _manager;
}
