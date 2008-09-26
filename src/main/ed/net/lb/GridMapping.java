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

import java.net.*;
import java.util.*;

import ed.js.*;
import ed.db.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.cloud.*;
import static ed.appserver.AppContextHolder.*;

public class GridMapping implements Mapping {

    public static class Factory implements MappingFactory {
        Factory(){
            _cloud = Cloud.getInstanceIfOnGrid();
        }
        
        public Mapping getMapping(){
            return new GridMapping( _cloud );
        }

        final Cloud _cloud;
    }

    GridMapping( Cloud c ){
        _cloud = c;
        if ( _cloud == null )
            throw new RuntimeException( "can't have a GridMapping when not running on a grid!" );
        
        DBBase db = (DBBase)_cloud.getScope().get("db");

        for ( Iterator<JSObject> i = db.getCollection( "sites" ).find(); i.hasNext();  ){
            final JSObject site = i.next();
            final String name = site.get("name").toString().toLowerCase();

            if ( site.get( "environments" ) == null )
                continue;

            final Map<String,String> envs = new TreeMap<String,String>();

            for ( Object eo : ((JSArray)site.get( "environments" ) ) ){
                JSObject e = (JSObject)eo;
                envs.put( e.get( "name" ).toString().toLowerCase() , e.get( "pool" ).toString().toLowerCase() );
            }

            _sites.put( name , envs );
        }


        for ( Iterator<JSObject> i = db.getCollection( "pools" ).find(); i.hasNext();  ){
            final JSObject pool = i.next();
            final String name = pool.get( "name" ).toString().toLowerCase();

            List<InetSocketAddress> all = new ArrayList<InetSocketAddress>();
            
            for ( Object mo : ((JSArray)pool.get( "machines" ) ) ){
                String m = mo.toString().toLowerCase();
                int port = 8080;
                
                int idx = m.indexOf( ":" );
                if ( idx > 0 ){
                    port = Integer.parseInt( m.substring( idx + 1 ) );
                    m = m.substring( 0 , idx );
                }

                InetSocketAddress addr = new InetSocketAddress( m , port );
                all.add( addr );
            }
            _pools.put( name , all );
        }
        
    }

    public String getPool( HttpRequest request ){
        Info info = fixBase( request.getHost() , request.getURI() );
        Info site = info;

        if ( ! _sites.containsKey( site.getHost() ) ){
            for ( Info i : getPossibleSiteNames( info ) ){
                if ( _sites.containsKey( i.getHost() ) ){
                    site = i;
                    break;
                }
            }
        }

        final String name = site.getHost();
        final String env = site.getEnvironment( info.getHost() );
        final String pool = _sites.get( name ).get( env );
        
        System.out.println( "site: " + name + " env: " + env + " -->> " + pool );

        if ( pool == null )
            throw new RuntimeException( "something is misconfigured.  site [" + name + "] environment [" + env + "]" );

        return pool;
    }
    
    public List<InetSocketAddress> getAddressesForPool( String pool ){
        return _pools.get( pool );
    }
    
    public String toString(){
        return "GridMapping";
    }

    final Cloud _cloud;
    final Map<String,Map<String,String>> _sites = new HashMap<String,Map<String,String>>();
    final Map<String,List<InetSocketAddress>> _pools = new HashMap<String,List<InetSocketAddress>>();

    public static void main( String args[] ){
        System.out.println( (new Factory()).getMapping() );
    }
}
