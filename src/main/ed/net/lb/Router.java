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

    public InetSocketAddress chooseAddress( HttpRequest request ){
        Environment e = _mapping.getEnvironment( request );
        return chooseAddressForPool( e , _mapping.getPool( e ) );
    }
    
    InetSocketAddress chooseAddressForPool( final Environment e , final String pool ){
        Pool p = _pools.get( pool );
        if ( p == null ){
            p = new Pool( pool , _mapping.getAddressesForPool( pool ) );
            _pools.put( pool , p );
        }
        return p.getAddress( e );
    }

    public void error( HttpRequest request , InetSocketAddress addr , NIOClient.ServerErrorType type , Exception what ){
        getServer( addr ).error( request , type , what );
    }

    public void success( HttpRequest request , InetSocketAddress addr ){
        getServer( addr ).success( request );
    }

    Server getServer( InetSocketAddress addr ){
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

    class Server {
        Server( InetSocketAddress addr ){
            _addr = addr;
            reset();
        }
        
        void reset(){
            _environmentsWithTraffic.clear();
            _serverStart = System.currentTimeMillis();
            _inErrorState = false;
        }

        void error( HttpRequest request , NIOClient.ServerErrorType type , Exception what ){
            _inErrorState = true;
        }
        
        void success( HttpRequest request ){
            _environmentsWithTraffic.add( _mapping.getEnvironment( request ) );
        }

        /**
         * < 0 do not send traffic
         * 0 rather not have traffic
         * > 1 the higher the better
         */
        double rating( Environment e ){
            if ( _inErrorState )
                return 0;
            
            if ( _environmentsWithTraffic.contains( e ) )
                return 2;
            
            return 1;
        }
        
        public String toString(){
            return _addr.toString();
        }
        
        final InetSocketAddress _addr;

        final Set<Environment> _environmentsWithTraffic = Collections.synchronizedSet( new HashSet<Environment>() );
        long _serverStart;
        boolean _inErrorState = false;
    }


    class Pool {

        Pool( String name , List<InetSocketAddress> addrs ){
            _name = name;
            _servers = new ArrayList<Server>();
            for ( InetSocketAddress addr : addrs )
                _servers.add( getServer( addr ) );
        }

        InetSocketAddress getAddress( Environment e ){
            final int start = (int)(Math.random()*_servers.size());
            final int size = _servers.size();
            _seen.add( e );
            
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
            
            if ( best == null )
                throw new RuntimeException( "no server available for pool [" + _name + "]" );
            
            return best._addr;
        }
        
        final String _name;
        final List<Server> _servers;
        final Set<Environment> _seen = new HashSet<Environment>();
    }

    void _addMonitors(){
        HttpServer.addGlobalHandler( new HttpMonitor( "lb-pools" ){
                public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){   
                    out.print( "<ul>" );
                    
                    for ( String s : _pools.keySet() ){
                        out.print( "<li>" );
                        out.print( s );

                        out.print( "<ul>" );
                        for ( Server server : _pools.get( s )._servers ){
                            out.print( "<li>" );
                            out.print( server.toString() );
                            out.print( "</li>" );
                        }
                        out.print( "</ul>" );

                        out.print( "</li>" );
                    }

                    out.print( "</ul>" );
                }
            }
            );
    }

    private final MappingFactory _mappingFactory;
    private final Map<String,Pool> _pools = Collections.synchronizedMap( new TreeMap<String,Pool>() );
    private final Map<InetSocketAddress,Server> _addressToServer = Collections.synchronizedMap( new HashMap<InetSocketAddress,Server>() );
    private Mapping _mapping;
}
