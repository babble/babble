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
import ed.log.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.cloud.*;
import static ed.appserver.AppContextHolder.*;

public class GridMapping extends MappingBase {

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
    }

    final Cloud _cloud;

    public static void main( String args[] ){
        System.out.println( (new Factory()).getMapping() );
    }
}
