// GridConfigApplicationFactory.java

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

import ed.js.*;
import ed.db.*;
import ed.log.*;
import ed.util.*;
import ed.cloud.*;

public class GridConfigApplicationFactory extends ConfigurableApplicationFactory {

    static final File EXTRA_GRID_CONFIG = new File( "conf/extraGridApps" );

    GridConfigApplicationFactory(){
        super( Cloud.CLOUD_REFRESH_RATE );

        _cloud = Cloud.getInstanceIfOnGrid();
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

        for ( Iterator<JSObject> i = _find( "dbs" ); i.hasNext(); ){
            final JSObject db = i.next();
            final String name = db.get( "name" ).toString();
            
            if ( _cloud.isMyServerName( db.get( "machine" ).toString() ) ){
                
                if ( added )
                    throw new RuntimeException( "have multiple databases configured for this node!" );
                
                config.addEntry( "db" , name , "ACTIVE" , "true" );
                config.addEntry( "db" , name , "master" , "true" );
                
                added = true;
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
                    
                    System.out.println( (new DBApp( name , config.getMap( "db" , name ) ) ) );

                    added = true;
                }
            }
        }
        
    }

    void _doAppServers( SimpleConfig config ){ 
        for ( Iterator<JSObject> i = _find( "pools" ); i.hasNext(); ){
            final JSObject pool = i.next();
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
        for ( Iterator<JSObject> i = _find( "lbs" ); i.hasNext(); ){
            final JSObject lb = i.next();
            final String machine = lb.get( "machine" ).toString();
            
            if ( ! _cloud.isMyServerName( machine ) )
                continue;
                
            config.addEntry( "lb" , machine , "ACTIVE" , "true" );
        }
    }

    Iterator<JSObject> _find( String type ){
        Iterator<JSObject> i = _db.getCollection( type ).find();
        if ( i == null )
            i = (new LinkedList<JSObject>()).iterator();
        return i;
    }

    void _addDefaultsIfAny( SimpleConfig config ){

        if ( JSInternalFunctions.JS_evalToBool( _cloud.getMe().get( "isGridServer" ) ) ){
            config.addEntry( "db" , "grid" , "ACTIVE" , "true" );
            config.addEntry( "db" , "grid" , "master" , "true" );
            config.addEntry( "db" , "grid" , "port" , String.valueOf( _cloud.getGridDBPort() ) );
            config.addEntry( "db" , "grid" , "dbpath" , "/data/db-grid/" );
        }
    }

    final Cloud _cloud;
    final DBBase _db;
    final String _serverName;

    private SimpleConfig _lastConfig;

    private static File _cacheFile = new File( "logs/gridappcache" );
    private static Logger _logger = Logger.getLogger( "gridappconfig" );
    
    public static void main( String args[] )
        throws Exception {
        
        System.out.println( (new GridConfigApplicationFactory()).getConfig() );

    }
}
