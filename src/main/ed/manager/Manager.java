// Manager.java

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

package ed.manager;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.apache.commons.cli.*;

import ed.log.*;
import ed.git.*;
import ed.util.*;
import ed.appserver.*;
import ed.net.httpserver.*;

/**
 * a Manager lives on a machine and makes sure all the processes that should be running are
 * also can do log management, etc...
 *
 * See http://www.10gen.com/wiki/cloud.SystemAppManager
 */
public class Manager extends Thread {

    public Manager( ApplicationFactory factory ){
        this( factory , false );
    }
    
    public Manager( ApplicationFactory factory , boolean verbose ){
        super( "ApplicationManager" );
        _factory = factory;
        _logger = Logger.getLogger( "manager" );
        _logger.setLevel( verbose ? Level.DEBUG : Level.INFO );

        _webView = new WebView( this );
        
        if ( HttpServer.numberListeningPorts() == 0 ){
            try {
                _server = new HttpServer( 8000 );
                _server.start();
            }
            catch ( IOException ioe ){
                throw new RuntimeException( "couldn't start web server" , ioe );
            }
        }
        else {
            _server = null;
        }

        _webView.add();

        if ( _factory.runGridApplication() )
            _installGridApp();
        
        Runtime.getRuntime().addShutdownHook( new Thread(){
                public void run(){
                    Manager.this.shutdown();
                }
            } );
    }

    public void run(){
        
        while ( ! _shutdown ){
            
            int running = 0;

            _currentApps = _factory.getApplications();
            for ( Application app : _currentApps ){
                if ( isPaused( app ) ) {
                    running++;
                    continue;
                }

                try {
                    _logger.debug( "manager checking apps" );
                    
                    RunningApplication run = _running.get( app );
                    
                    if ( run != null ){
                        
                        if ( run._app.sameConfig( app ) ){
                            if ( ! run.isDone() )
                                running++;
                            continue;
                        }
                        
                        _logger.info( "config changed" );
                        run.restart( app );
                        continue;
                    }
                    
                    run = new RunningApplication( this , app );
                    _running.put( app , run );
                    run.start();
                    running++;
                }
                catch ( Exception e ){
                    _logger.error( "error in run loop for app [" + app.getFullId() + "]" , e );
                }
            }
            
            if ( running == 0 ){
                _logger.info( "shutting down because nothing running" );
                break;
            }
            
            try {
                _sleeping = true;
                Thread.sleep( _factory.timeBetweenRefresh() );
            }
            catch ( InterruptedException e ){
            }
            finally {
                _sleeping = false;
            }
            
        }
        
        // TODO: shut things down
    }

    public void check(){
        if ( _sleeping )
            interrupt();
    }

    public void shutdown(){
        _shutdown = true;

        if ( _server != null )
            _server.stopServer();

        for ( RunningApplication ra : _running.values() )
            ra.shutdown();
    }

    public boolean isShutDown(){
        return _shutdown;
    }


    public void togglePause( Application app ) {
        RunningApplication ra = _running.get( app );
        if( _paused.contains( app ) ) {
            _paused.remove( app );
            ra = new RunningApplication( this , app );
            _running.put( app , ra );
            ra.start();
        }
        else {
            _paused.add( app );
            ra.shutdown();
        }
    }

    public boolean isPaused( Application r ) {
        return _paused.contains( r );
    }

    public Application findApplication( String fullId ){
        for ( Application app : getApplications() )
            if ( app.getFullId().equals( fullId ) )
                return app;
        return null;
    }

    public List<Application> getApplications(){
        return new ArrayList<Application>( _running.keySet() );
    }

    RunningApplication getRunning( Application app ){
        return _running.get( app );
    }
    
    void _installGridApp(){
        String gridSite = Config.getDataRoot() + "/sites/grid/";

        GitDir gd = new GitDir( new File( gridSite ) );

        if ( ! gd.isValid() )
            gd.clone( "git://github.com/10gen/sites-grid.git" );

        gd.pull();
        
        _server.addHandler( new AppServer( gridSite , null ) );
    }
    
    final ApplicationFactory _factory;
    final Logger _logger;
    final WebView _webView;
    final HttpServer _server;

    private boolean _shutdown = false;
    private boolean _sleeping = false;
    
    List<Application> _currentApps = new ArrayList<Application>();
    private final Map<Application,RunningApplication> _running = new HashMap<Application,RunningApplication>();
    private final ArrayList<Application> _paused = new ArrayList<Application>();

    public static void main( String args[] )
        throws Exception {
        
        HttpMonitor.setApplicationType( "System Manager" );

        Options o = new Options();
        o.addOption( "v" , "verbose" , false , "Verbose" );
        o.addOption( "c" , "config" , true , "config file for TextConfigApplicationFactory" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        ApplicationFactory factory = null;
        if ( cl.hasOption( "config" ) )
            factory = new TextConfigApplicationFactory( new File( cl.getOptionValue( "config" , null ) ) );
        else 
            factory = new GridConfigApplicationFactory();

        Manager m = new Manager( factory , cl.hasOption( "v" ) );
        m.start();
        m.join();
    }
}
