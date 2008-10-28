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

import java.util.*;

import ed.net.httpserver.*;

public class WebView extends HttpMonitor {
    
    
    static final String BASE_NAME = "appManager";

    public WebView( Manager manager ){
        super( BASE_NAME );
        _manager = manager;
        _detail = new Detail();
    }
    
    public void handle( MonitorRequest request ){
        
        final String restartId = request.getRequest().getParameter( "restart" );
        if ( restartId != null ){
            Application app = _manager.findApplication( restartId );
            if ( app == null ){
                request.addMessage( "can't find application [" + restartId + "]" );
            }
            else {
                request.addMessage( "restarted [" + restartId + "]" );
                _manager.getRunning( app ).restart();
            }
            
        }
        
        request.startData( "applications" , "type" , "id" , "started" , "uptime" , "timesStarted" );
        
        for ( Application app : _manager.getApplications() ){
            RunningApplication ra = _manager.getRunning( app );
            request.addData( app.getType() + "." + app.getId() , 
                             app.getType() , app.getId() , new Date( ra.getLastStart() ) , ra.getUptimeMinutes() , ra.timesStarted() , 
                             "<a href='" + _detail.getURI() + "?id=" + app.getFullId() + "'>detail</a> | " + 
                             "<a href='" + getURI() + "?restart=" + app.getFullId() + "'>restart</a>"
                             );
        }

        request.endData();
    }
    
    void add(){
        HttpServer.addGlobalHandler( this );
        HttpServer.addGlobalHandler( _detail );
    }
    
    class Detail extends HttpMonitor {
        Detail(){
            super( BASE_NAME + "-detail" );
        }
        
        public void handle( MonitorRequest request ){
            String fullId = request.getRequest().getParameter( "id" );
            if ( fullId == null ){
                request.print( "<ul>" );
                for ( Application app : _manager.getApplications() ){
                    request.print( "<li>" );
                    request.print( "<a href='" + getURI() + "?id=" + app.getFullId() + "'>" + 
                                   app.getType() + "." + app.getId() + 
                                   "</a>" );
                    request.print( "</li>" );
                }  
                request.print( "</ul>" );
            }
            else {
                
                Application app = _manager.findApplication( fullId );
                
                if ( app == null )
                    request.print( "can't find app [" + fullId + "]" );
                else {

                    request.print( "Command: <b>" + Arrays.toString( app.getCommand() ) + "</b><br>" );

                    request.print( "<pre>\n" );
                    RunningApplication ra = _manager.getRunning( app );
                    for ( int i=0; i<ra._lastOutput.size(); i++ ){
                        request.print( ra._lastOutput.get(i) );
                        request.print( "\n" );
                    }
                    request.print( "\n</pre>\n" );
                }

            }
        }
    }

    final Manager _manager;
    final Detail _detail;
}
