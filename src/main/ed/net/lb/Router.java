// Router.java

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

import ed.net.*;
import ed.net.httpserver.*;
import static ed.net.lb.Mapping.*;

public class Router {
    
    public Router( MappingFactory mappingFactory ){
        _mappingFactory = mappingFactory;
        _mapping = _mappingFactory.getMapping();
    }

    public InetSocketAddress chooseAddress( HttpRequest request , boolean doOrDie ){
        final Environment e = _mapping.getEnvironment( request );
        if ( e == null )
            throw new IllegalArgumentException( "can't find pool for [" + request.getFullURL() + "]" );
        
        final String p = _mapping.getPool( e );
        if ( p == null )
            throw new IllegalArgumentException( "can't find pool for " + e + " from [" + request.getFullURL() + "]" );
        
        return chooseAddressForPool( e , p , doOrDie );
    }

    
    /**
     * @param doOrDie if this is false, this function will return nul if it doesn't like any of the appservers
     *                if it is true, it'll return its best option, and failing that will throw an exception
     */
    InetSocketAddress chooseAddressForPool( final Environment e , final String pool , boolean doOrDie ){
	return getPool( pool ).getAddress( e , doOrDie );
    }

    public void error( HttpRequest request , HttpResponse response , InetSocketAddress addr , NIOClient.ServerErrorType type , Exception what ){
        final Environment e = _mapping.getEnvironment( request );
        if ( addr != null )
            getServer( addr ).error( e , type , what , request , response  );
        
        // TODO: something with env?
    }

    public void success( HttpRequest request , HttpResponse response , InetSocketAddress addr ){
        getServer( addr ).success( _mapping.getEnvironment( request ) , request , response );
    }

    Pool getPool( HttpRequest request ){
	return getPool( _mapping.getPool( request ) );
    }

    Pool getPool( String name ){
        if ( name == null )
            return null;

        Pool p = _pools.get( name );
        if ( p != null )
	    return p;
	
	synchronized( _pools ){
	    p = _pools.get( name );
	    if ( p != null )
		return p;
	    
            p = new Pool( name , _mapping.getAddressesForPool( name ) );
            _pools.put( name , p );
        }
        return p;
    }
    
    Server getServer( InetSocketAddress addr ){
        if ( addr == null )
            throw new NullPointerException( "addr can't be null" );

        Server s = _addressToServer.get( addr );
        if ( s != null )
            return s;
        
        synchronized( _addressToServer ){

            s = _addressToServer.get( addr );
            if ( s != null )
                return s;
            
            s = new Server( addr );
            _addressToServer.put( addr , s );
        }
        return s;
    }

    Set<String> getPoolNames(){
        return _pools.keySet();
    }

    class Pool {

        Pool( String name , List<InetSocketAddress> addrs ){

            if ( addrs == null || addrs.size() == 0 )
                throw new NullPointerException( "can't create a Pool with no addresses" );
	    
            _name = name;
	    _tracker = new HttpLoadTracker( name , 2 , 60 );
            _servers = new ArrayList<Server>();
            for ( InetSocketAddress addr : addrs )
                _servers.add( getServer( addr ) );
        }

        InetSocketAddress getAddress( Environment e , boolean doOrDie ){
            final int start = (int)(Math.random()*_servers.size());
            final int size = _servers.size();
            _seen.add( e );
            
	    if ( size == 1 )
		return _servers.get(0)._addr;

            Server best = null;
            double score = Double.MIN_VALUE;
            
            for ( int i=0; i<size ; i++ ){
                Server s = _servers.get( ( i + start ) % size );
                
                double myScore = s.rating( e );
                if ( myScore < 0 )
                    continue;
                
                if ( myScore < score )
                    continue;
                
                score = myScore;
                best = s;
            }
            
            if ( best == null ){
                if ( doOrDie )
                    throw new RuntimeException( "no server available for pool [" + _name + "]" );
                return null;
            }
            
            if ( score == 0 && ! doOrDie )
                return null;

            return best._addr;
        }
        
        final String _name;
        final List<Server> _servers;
        final Set<Environment> _seen = new HashSet<Environment>();
	final HttpLoadTracker _tracker;
    }


    private final MappingFactory _mappingFactory;
    private final Map<String,Pool> _pools = Collections.synchronizedMap( new TreeMap<String,Pool>() );
    private final Map<InetSocketAddress,Server> _addressToServer = Collections.synchronizedMap( new HashMap<InetSocketAddress,Server>() );
    private Mapping _mapping;
}
