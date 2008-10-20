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

import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.net.httpserver.*;
import static ed.net.lb.Mapping.*;

public final class Router {
    
    public Router( MappingFactory mappingFactory ){
	_logger = Logger.getLogger( "LB" ).getChild( "router" );
        
        _mappingFactory = mappingFactory;
        _mapping = _mappingFactory.getMapping();

	_mappingUpdater = new MappingUpdater();
        _initCheck();
    }

    public Environment getEnvironment( HttpRequest request  ){
        final Environment e = _mapping.getEnvironment( request );
        if ( e == null )
            throw new IllegalArgumentException( "can't find env for [" + request.getFullURL() + "]" );
        return e;
    }
    
    public InetSocketAddress chooseAddress( HttpRequest request , boolean doOrDie ){
        final Environment e = _mapping.getEnvironment( request );
        if ( e == null )
            throw new IllegalArgumentException( "can't find pool for [" + request.getFullURL() + "]" );
        return chooseAddress( e , request , doOrDie );
    }
    
    public InetSocketAddress chooseAddress( final Environment e , final HttpRequest request , final boolean doOrDie ){
        if ( e == null )
            throw new NullPointerException( "can't call chooseAddress with a null environment" );

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
	final String pool = e == null ? null :_mapping.getPool( e );
	
	if ( pool != null )
	    getPool( pool )._tracker.networkEvent();
	
        if ( addr != null )
            getServer( addr ).error( e , type , what , request , response  );
        
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
    
    List<Server> getServers(){
	return new ArrayList<Server>( _addressToServer.values() );
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
    
    void updateMapping(){
        Mapping m = _mappingFactory.getMapping();
        _mapping = m;
        
        for ( Pool p : _pools.values() )
            p.update( _mapping.getAddressesForPool( p._name ) );

        _initCheck();
    }
    
    void _initCheck(){
        for ( String pool : _mapping.getPools() )
            getPool( pool );
    }

    boolean reject( HttpRequest request ){
        return _mapping.reject( request );
    }

    class Pool {

        Pool( String name , List<InetSocketAddress> addrs ){

            if ( addrs == null || addrs.size() == 0 )
                throw new NullPointerException( "can't create a Pool with no addresses" );
	    
            _name = name;
	    _tracker = new HttpLoadTracker( name );
            _servers = new ArrayList<Server>();
            for ( InetSocketAddress addr : addrs )
                _servers.add( getServer( addr ) );
        }

        void update( List<InetSocketAddress> addrs ){

            Set<Server> newServers = new HashSet<Server>();

            for ( InetSocketAddress addr : addrs ){
                Server s = getServer( addr );
                newServers.add( s );
                
                if ( _servers.contains( s ) )
                    continue;
                
                _servers.add( s );
            }

            for ( Iterator<Server> i = _servers.iterator(); i.hasNext(); ){
                Server s = i.next();
                if ( newServers.contains( s ) )
                    continue;
                i.remove();
            }
                
        }

        InetSocketAddress getAddress( Environment e , boolean doOrDie ){
            final int start = (int)(Math.random()*_servers.size());
            final int size = _servers.size();
            _seen.add( e );
            
	    if ( size == 1 ){
		return _servers.get(0)._addr;
	    }

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
                if ( doOrDie ){
                    String msg = "no server available for pool [" + _name + "]";
                    _logger.fatal( msg );
                    throw new RuntimeException( msg );
                }
		_logger.debug( "no viable server for pool [" + _name + "] waiting" );
                return null;
            }
            
            if ( score == 0 && ! doOrDie ){
		_logger.info( "no good server (score > 0) for pool [" + _name + "] waiting" );
                return null;
	    }

            return best._addr;
        }
        
        final String _name;
        final List<Server> _servers;
        final Set<Environment> _seen = new HashSet<Environment>();
	final HttpLoadTracker _tracker;
    }

    class MappingUpdater extends Thread {
        MappingUpdater(){
            super( "Router-MappingUpdater" );
            setDaemon( true );
            start();
        }
        
        public void run(){
            while ( true ){
                ThreadUtil.sleep( _mappingFactory.refreshRate() );
                try {
                    updateMapping();
                }
                catch ( Exception e ){
                    e.printStackTrace();
                }
            }
        }
    }

    final Logger _logger;

    private final MappingFactory _mappingFactory;
    private final MappingUpdater _mappingUpdater;
    
    private final Map<String,Pool> _pools = Collections.synchronizedMap( new TreeMap<String,Pool>() );
    private final Map<InetSocketAddress,Server> _addressToServer = Collections.synchronizedMap( new HashMap<InetSocketAddress,Server>() );
    Mapping _mapping;
}
