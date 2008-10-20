// GridMapping.java

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
            return 1000 * 30;
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
                JSObject e = (JSObject)eo;
                addSiteMapping( name , e.get( "name" ).toString().toLowerCase() , e.get( "pool" ).toString().toLowerCase() );
            }
            
        }

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


        for ( Iterator<JSObject> i = db.getCollection( "blocked_ips" ).find(); i.hasNext();  )
            blockIp( i.next().get( "ip" ).toString() );

        for ( Iterator<JSObject> i = db.getCollection( "blocked_urls" ).find(); i.hasNext();  )
            blockUrl( i.next().get( "url" ).toString() );
        
    }

    final Cloud _cloud;

    public static void main( String args[] ){
        System.out.println( (new Factory()).getMapping() );
    }
}
