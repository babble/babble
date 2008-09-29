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

public abstract class MappingBase implements Mapping {

    protected MappingBase( String name ){
        _name = name;
        _logger = Logger.getLogger( name );
    }
    
    public Environment getEnvironment( HttpRequest request ){
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
        
        return new Environment( name , env );
    }
    
    public String getPool( Environment e ){

        Map<String,String> m = _sites.get( e.site );
        if ( m == null ){
            _logger.error( "no site for [" + e.site + "]" );
            return _defaultPool;
        }
        
        final String pool = m.get( e.env );
        
        if ( pool == null ){
            _logger.error( "no env [" + e.env + "] for site [" + e.site + "]" );
            return _defaultPool;
        }

        return pool;
    }
    
    public List<InetSocketAddress> getAddressesForPool( String pool ){
        return _pools.get( pool );
    }

    public String toString(){
        return _name;
    }

    protected void addAddressToPool( String pool , String addr ){
        int port = 8080;
        int idx = addr.indexOf( ":" );
        if ( idx > 0 ){
            port = Integer.parseInt( addr.substring( idx + 1 ) );
            addr = addr.substring(0,idx);
        }
        addAddressToPool( pool , new InetSocketAddress( addr , port ) );
    }
    
    protected void addAddressToPool( String pool , InetSocketAddress addr ){
        _logger.debug( "Adding to pool [" + pool + "] address [" + addr + "]" );
        List<InetSocketAddress> lst = _pools.get( pool );
        if ( lst == null ){
            lst = new ArrayList<InetSocketAddress>();
            _pools.put( pool , lst );
        }
        lst.add( addr );
    }
    
    protected void addSiteMapping( String site , String env , String pool ){
        _logger.debug( "Adding mapping for site [" + site + "] [" + env + "] -> [" + pool + "]" );
        Map<String,String> m = _sites.get( site );
        if ( m == null ){
            m = new TreeMap<String,String>();
            _sites.put( site , m );
        }
        m.put( env , pool );
    }

    protected void setDefaultPool( String pool ){
        _defaultPool = pool;
    }

    protected final String _name;
    final Logger _logger;


    private String _defaultPool;
    final private Map<String,Map<String,String>> _sites = new HashMap<String,Map<String,String>>();
    final private Map<String,List<InetSocketAddress>> _pools = new HashMap<String,List<InetSocketAddress>>();
}


