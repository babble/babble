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

public class Router {
    
    public Router( MappingFactory mappingFactory ){
        _mappingFactory = mappingFactory;
        _mapping = _mappingFactory.getMapping();
    }

    public InetSocketAddress chooseAddress( HttpRequest request ){
        return chooseAddressForPool( request , _mapping.getPool( request ) );
    }

    InetSocketAddress chooseAddressForPool( final HttpRequest request , final String pool ){
        Pool p = _pools.get( pool );
        if ( p == null ){
            p = new Pool( _mapping.getAddressesForPool( pool ) );
            _pools.put( pool , p );
        }
        return p.getAddress( request );
    }
    
    public void error( InetSocketAddress addr , NIOClient.ServerErrorType type , Exception what ){
        
    }

    class Pool {

        Pool( List<InetSocketAddress> addrs ){
            _addrs = addrs;
        }

        InetSocketAddress getAddress( HttpRequest request ){
            return _addrs.get( (int)(Math.random()*_addrs.size()) );
        }
        
        final List<InetSocketAddress> _addrs;
    }

    void _addMonitors(){
        HttpServer.addGlobalHandler( new HttpMonitor( "lb-pools" ){
                public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){   
                    out.print( "<ul>" );
                    
                    for ( String s : _pools.keySet() ){
                        out.print( "<li>" );
                        out.print( s );

                        out.print( "<ul>" );
                        for ( InetSocketAddress addr : _pools.get( s )._addrs ){
                            out.print( "<li>" );
                            out.print( addr.toString() );
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
    private Mapping _mapping;
}
