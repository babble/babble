// GridMapping.java

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

import java.io.*;
import java.net.*;
import java.util.*;

import ed.js.*;
import ed.db.*;
import ed.log.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.cloud.*;
import static ed.appserver.AppContextHolder.*;

public class GridMapping extends MappingBase {

    public static class Factory implements MappingFactory {
        Factory(){
            _cloud = Cloud.getInstanceIfOnGrid();
            _logger = Logger.getLogger( "lb.gridmapping" );
            _cacheFile = new File( "logs/gridmapcache" );
        }
        
        public Mapping getMapping(){
            try {
                GridMapping gm = new GridMapping( _cloud );
                _prev = gm;
                writeToCache( gm );
                return gm;
            }
            catch ( RuntimeException e ){
                _logger.error( "couldn't load new grid config" , e );

                if ( _prev != null ){
                    _logger.info( "using previously loaded config" );
                    return _prev;
                }
                
                if ( _cacheFile.exists() ){
                    _logger.info( "trying old cache from disk [" + _cacheFile + "]" );
                    try {
                        return new TextMapping( _cacheFile );
                    }
                    catch ( IOException ioe ){
                        _logger.error( "couldn't read old cache file [" + _cacheFile + "]" , ioe );
                    }
                }
                
                throw e;
            }
        }
        
        public long refreshRate(){
            return Cloud.CLOUD_REFRESH_RATE;
        }
        
        void writeToCache( GridMapping mapping ){
            String s = mapping.toFileConfig();

            int hash = s.hashCode();
            if ( hash == _prevHash )
                return;
            _prevHash = hash;

            try {
                byte[] data = s.getBytes( "utf8" );
                FileOutputStream out = new FileOutputStream( _cacheFile );
                out.write( data );
                out.close();
            }
            catch ( IOException ioe ){
                _logger.error( "couldn't write gcache file" , ioe );
            }
        }
        
        final Cloud _cloud;
        final Logger _logger;
        final File _cacheFile;

        private GridMapping _prev;
        private int _prevHash = 0;
    }

    GridMapping( Cloud c ){
        super( "GridMapping" );

        _cloud = c;
        if ( _cloud == null )
            throw new RuntimeException( "can't have a GridMapping when not running on a grid!" );

        DBBase db = (DBBase)_cloud.getScope().get("db");
        
        for ( Iterator<JSObject> i = db.getCollection( "sites" ).find(); i.hasNext();  ){
            final JSObject site = i.next();
            final String name = site.get("name").toString().toLowerCase();

            if ( site.get( "environments" ) == null )
                continue;
            
            for ( Object eo : ((JSArray)site.get( "environments" ) ) ){
                final JSObject e = (JSObject)eo;

                final String env = e.get( "name" ).toString().toLowerCase();
                final String pool = e.get( "pool" ).toString().toLowerCase();
                
                addSiteMapping( name , env , pool );

                if ( e.get( "aliases" ) instanceof List )
                    for ( Object a : (List)(e.get( "aliases" )) )
                        addSiteAlias( name , a.toString().toLowerCase() , env );
            }
            
        }
        
        checkGridSite();

        String defaultPool = null;

        for ( Iterator<JSObject> i = db.getCollection( "pools" ).find(); i.hasNext();  ){
            final JSObject pool = i.next();
            final String name = pool.get( "name" ).toString().toLowerCase();
            
            for ( Object mo : ((JSArray)pool.get( "machines" ) ) ){
                String m = mo.toString().toLowerCase();
                addAddressToPool( name , m );
            }

            if ( name.startsWith( "prod" ) ){
                if ( defaultPool == null || name.compareTo( defaultPool ) > 0 )
                    defaultPool = name;
            }
        }
        
        setDefaultPool( defaultPool );


	Iterator<JSObject> i = db.getCollection( "blocked_ips" ).find();
	if ( i != null )
	    while ( i.hasNext() )
		blockIp( i.next().get( "ip" ).toString() );

	i = db.getCollection( "blocked_urls" ).find();
	if ( i != null )
	    while ( i.hasNext() )
		blockUrl( i.next().get( "url" ).toString() );
        
    }

    void checkGridSite(){

        if ( getSiteInfo( "grid" , false ) != null )
            return;
        
        _logger.info( "no grid site so adding to : " + _cloud.getGridLocation() );
        for ( String s : _cloud.getGridLocation() ){
            if ( s.indexOf( ":" ) < 0 )
                s += ":8000";
            else
                s = s.replaceAll( ":.*$" , ":8000" );
            addAddressToPool( "gridauto" , s );
        }

        addSiteMapping( "grid" , "www" , "gridauto" );
    }

    final Cloud _cloud;

    public static void main( String args[] ){
        System.out.println( (new Factory()).getMapping() );
    }
}
