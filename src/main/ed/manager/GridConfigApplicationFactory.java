// GridConfigApplicationFactory.java

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

import ed.js.*;
import ed.db.*;
import ed.log.*;
import ed.util.*;
import ed.cloud.*;

public class GridConfigApplicationFactory extends ConfigurableApplicationFactory {

    static final File EXTRA_GRID_CONFIG = new File( "conf/extraGridApps" );

    GridConfigApplicationFactory(){
        this( Cloud.getInstanceIfOnGrid() );
    }

    GridConfigApplicationFactory( Cloud c ){
        super( Cloud.CLOUD_REFRESH_RATE );

        _cloud = c;
        if ( _cloud == null )
            throw new RuntimeException( "can't get cloud" );

        _db = _cloud.getDB();
        _serverName = _cloud.getServerName();
    }
    
    protected SimpleConfig getConfig()
        throws IOException {
        
        final TextSimpleConfig config;
        if ( EXTRA_GRID_CONFIG.exists() )
            config = TextSimpleConfig.read( EXTRA_GRID_CONFIG );
        else 
            config = new TextSimpleConfig();
        
        _addDefaultsIfAny( config );
        
        try {
            _updateSystemConfig();
            _doDBs( config );
            _doAppServers( config );
            _doLoadBalancers( config );
        }
        catch ( Exception e ){
            _logger.error( "couldn't load config from grid" , e );
            
            if ( _lastConfig != null ){
                _logger.info( "falling back to last config" );
                return _lastConfig;
            }
            
            if ( _cacheFile.exists() ){
                _logger.info( "have old cache file [" + _cacheFile + "] using that" );
                return TextSimpleConfig.read( _cacheFile );
            }
            
            if ( config.getTypes().size() == 0 )
                throw new RuntimeException( "can't load config from grid or cache file [" + _cacheFile + "], and no defaults, so can't do anything" );
        }
        
    
        _lastConfig = config;
        return config;
    }
    
    void _doDBs( SimpleConfig config ){ 
        
        boolean added = false;

        for ( final JSObject db : _find( "dbs" ) ){

            if ( db.get( "name" ) == null )
                throw new IllegalArgumentException( "db has to have a name" );
            
            final String name = db.get( "name" ).toString();
            
            boolean addedThisNode = false;

            if ( db.get( "machine" ) != null && _cloud.isMyServerName( db.get( "machine" ).toString() ) ){
                
                if ( added )
                    throw new RuntimeException( "have multiple databases configured for this node!" );
                
                config.addEntry( "db" , name , "ACTIVE" , "true" );
                config.addEntry( "db" , name , "master" , "true" );
                
                addedThisNode = true;
            }
            
            List slaves = JS.getArray( db , "slaves" );
            if ( slaves != null ){
                for ( Object slaveName : slaves ){

                    if ( ! _cloud.isMyServerName( slaveName.toString() ) )
                        continue;
                    
                    if ( added )
                        throw new RuntimeException( "have multiple databases configured for this node!" );
                    
                    config.addEntry( "db" , name , "ACTIVE" , "true" );
                    config.addEntry( "db" , name , "slave" , "true" );
                    config.addEntry( "db" , name , "source" , db.get( "machine" ).toString() );
                    
                    addedThisNode = true;
                }
            }

            List pairs = JS.getList( db , "pairs" );
            if ( pairs != null ){
                if ( pairs.size() != 2 )
                    throw new RuntimeException( "invalid pair config for [" + name + "] need exactly 2 pairs" );
                
                for ( int i=0; i<pairs.size(); i++ ){
                    
                    Object mySide = pairs.get(i);
                    
                    if ( ! _cloud.isMyServerName( mySide.toString() ) )
                        continue;
                    
                    if ( added )
                        throw new RuntimeException( "have multiple databases configured for this node!" );
                    
                    addedThisNode = true;

                    config.addEntry( "db" , name , "ACTIVE" , "true" );
                    config.addEntry( "db" , name , "pairwith" , pairs.get(1-i).toString() );
                }
            }
            
            added = added || addedThisNode;

            if ( addedThisNode && JS.bool( _systemConfig.get( "dbquota" ) ) ){
                config.addEntry( "db" , name , "quota" , "true" );
            }
        }
        
    }

    void _doAppServers( SimpleConfig config ){ 
        for ( final JSObject pool : _find( "pools" ) ){
            final String name = pool.get( "name" ).toString();
            
            for ( Object machine : (List)(pool.get( "machines" )) ){
                
                if ( ! _cloud.isMyServerName( machine.toString() ) )
                    continue;
                
                config.addEntry( "appserver" , name , "ACTIVE" , "true" );
                return; // only 1 appserver per machine
            }
        }
    }
    
    void _doLoadBalancers( SimpleConfig config ){ 
        for ( final JSObject lb : _find( "lbs" ) ){
            final String machine = lb.get( "machine" ).toString();
            
            if ( ! _cloud.isMyServerName( machine ) )
                continue;
                
            config.addEntry( "lb" , machine , "ACTIVE" , "true" );
        }
    }


    private void _updateSystemConfig(){
        JSObject sys = null;
        for ( final JSObject o : _find( "options" ) ){
            if ( sys != null )
                throw new RuntimeException( "options should only have 1 thing" );
            sys = o;
        }

        if ( sys == null ){
            sys = new JSObjectBase();
            System.out.println( "default options" );
        }

        _systemConfig = sys;
    }
    
    protected Iterable<JSObject> _find( String type ){
        final Iterator<JSObject> i = _db.getCollection( type ).find();
        if ( i == null )
            return new LinkedList<JSObject>();
        
        return new Iterable<JSObject>(){
            public Iterator<JSObject> iterator(){
                return i;
            }
        };
    }

    void _addDefaultsIfAny( SimpleConfig config ){
        
        JSObject me = _cloud.getMe();

        if ( JS.bool( JS.eval( me , "isGridServer" ) ) ){
            config.addEntry( "db" , "grid" , "ACTIVE" , "true" );
            config.addEntry( "db" , "grid" , "port" , String.valueOf( _cloud.getGridDBPort() ) );
            config.addEntry( "db" , "grid" , "dbpath" , "/data/db-grid/" );

            if ( JS.bool( JS.eval( me , "isMyGridDomainPaired" ) ) ){
                List l = (List)JS.eval( me , "getGridLocation" );
                assert( l.size() == 2 );
                config.addEntry( "db" , "grid" , "pairwith" , JS.eval( me , "getOtherGridPair" ).toString() + ":" + Cloud.getGridDBPort() );
            }
            else {
                config.addEntry( "db" , "grid" , "master" , "true" );
            }

        }
    }

    public boolean runGridApplication(){
        JSObject me = _cloud.getMe();
        return JS.bool( JS.eval( me , "isGridServer" ) );
    }

    final Cloud _cloud;
    final DBBase _db;
    final String _serverName;

    private JSObject _systemConfig;
    private SimpleConfig _lastConfig;

    private static File _cacheFile = new File( "logs/gridappcache" );
    private static Logger _logger = Logger.getLogger( "gridappconfig" );
    
    public static void main( String args[] )
        throws Exception {
        
        System.out.println( (new GridConfigApplicationFactory()).getConfig() );

    }
}
