// Manager.java

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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.apache.commons.cli.*;

import ed.log.*;
import ed.net.httpserver.*;

/**
   a Manager lives on a machine and makes sure all the processes that should be running are
   also can do log management, etc...
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
        
        Runtime.getRuntime().addShutdownHook( new Thread(){
                public void run(){
                    Manager.this.shutdown();
                }
            } );
    }

    public void run(){
        
        while ( ! _shutdown ){
            
            int running = 0;

            for ( Application app : _factory.getApplications() ){

                try {
                    _logger.info( "manager checking apps" );
                    
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
                Thread.sleep( _factory.timeBetweenRefresh() );
            }
            catch ( InterruptedException e ){
            }
            
        }
        
        // TODO: shut things down
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
    
    final ApplicationFactory _factory;
    final Logger _logger;
    final WebView _webView;
    final HttpServer _server;

    private boolean _shutdown = false;
    private final Map<Application,RunningApplication> _running = new HashMap<Application,RunningApplication>();

    public static void main( String args[] )
        throws Exception {

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
