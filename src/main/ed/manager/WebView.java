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
    
    
    static final String BASE_NAME = "appManager";

    public WebView( Manager manager ){
        super( BASE_NAME );
        _manager = manager;
        _tail = new Tail();
    }
    
    public void handle( MonitorRequest request ){

        request.startData( "applications" , "type" , "id" , "uptime" , "timesStarted" );
        
        for ( Application app : _manager.getApplications() ){
            RunningApplication ra = _manager.getRunning( app );
            request.addData( app.getType() + "." + app.getId() , 
                             app.getType() , app.getId() , ra.getUptimeMinutes() , ra.timesStarted() , 
                             "<a href='" + _tail.getURI() + "?id=" + app.getFullId() + "'>tail</a> | "
                             );
        }

        request.endData();
    }
    
    void add(){
        HttpServer.addGlobalHandler( this );
        HttpServer.addGlobalHandler( _tail );
    }
    
    class Tail extends HttpMonitor {
        Tail(){
            super( BASE_NAME + "-tail" );
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
                
                Application app = null;
                
                for ( Application temp : _manager.getApplications() ){
                    if ( ! fullId.equals( temp.getFullId() ) )
                        continue;
                    app = temp;
                    break;
                }         
                
                if ( app == null )
                    request.print( "can't find app [" + fullId + "]" );
                else {
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
    final Tail _tail;
}
