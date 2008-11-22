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
        _javaApps = new JavaApps();
    }

    protected boolean uriOK( String uri ){ 
        return uri.equals( "/" );
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
                             _action( "restart" , app )
                             );
        }
        
        request.endData();
    }

    String _action( String name , Application app ){
        StringBuilder buf = new StringBuilder( "<form action='" + getURI() + "' method='POST'>" );
        buf.append( "<input type='hidden' name='" + name + "' value='" + app.getFullId() + "' >" );
        buf.append( "<input type='submit' name='action' value='" + name + "'>" );
        buf.append( "</form>" );
        return buf.toString();
    }
    
    void add(){
        HttpServer.addGlobalHandler( this );
        HttpServer.addGlobalHandler( _detail );
        HttpServer.addGlobalHandler( _javaApps );
    }
    
    abstract class ProcessViewer extends HttpMonitor {
        ProcessViewer( String name ){
            super( BASE_NAME + "-" + name );
        }
        
        public void handle( MonitorRequest request ){
            String fullId = request.getRequest().getParameter( "id" );
            if ( fullId == null ){
                printList( request );
            }
            else {
                printApp( request , fullId );
            }
        }
        
        void printApp( MonitorRequest request , String fullId ){
            Application app = _manager.findApplication( fullId );
            if ( app == null ){
                request.print( "can't find app [" + fullId + "]" );
                return;
            }
            
            RunningApplication ra = _manager.getRunning( app );
            if ( ra == null ){
                request.print( "can't find running app for [" + fullId + "]" );
                return;
            }
            
            print( request , app , ra );
        }

        void printList( MonitorRequest request ){
            request.print( "<ul>" );
            for ( Application app : getApps() ){
                request.print( "<li>" );
                request.print( "<a href='" + getURI() + "?id=" + app.getFullId() + "'>" + 
                               app.getType() + "." + app.getId() + 
                               "</a>" );
                request.print( "</li>" );
            }  
            request.print( "</ul>" );
        }
        
        abstract List<? extends Application> getApps();
        abstract void print( MonitorRequest request , Application app , RunningApplication ra );
    }
    
    class Detail extends ProcessViewer {
        Detail(){
            super( "detail" );
        }
        
        List<Application> getApps(){
            return _manager.getApplications();
        }
        
        void print( MonitorRequest request , Application app , RunningApplication ra ){
            request.print( "Command: <b>" + Arrays.toString( app.getCommand() ) + "</b><br>" );
            
            request.print( "<pre>\n" );
            for ( int i=0; i<ra._lastOutput.size(); i++ ){
                request.print( ra._lastOutput.get(i) );
                request.print( "\n" );
            }
            request.print( "\n</pre>\n" );            
        }
        
    }

    class JavaApps extends ProcessViewer {
        JavaApps(){
            super( "javaApps" );
        }

        void print( MonitorRequest request , Application app , RunningApplication ra ){
            JavaApplication j = (JavaApplication)app;
            request.print( "<pre>\n" );

            int size = j._gcs.size();
            for ( int i=0; i<size; i++ )
                request.print( j._gcs.get(i) + "\n" );

            request.print( "</pre>\n" );
        }

        List<JavaApplication> getApps(){
            List<JavaApplication> j = new ArrayList<JavaApplication>();
            for ( Application app : _manager.getApplications() ){
                if ( app instanceof JavaApplication )
                    j.add( (JavaApplication)app );
            }
            return j;
        }
    }

    final Manager _manager;
    final Detail _detail;
    final JavaApps _javaApps;
}
