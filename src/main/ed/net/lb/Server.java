// Server.java

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

import ed.net.*;
import ed.net.httpserver.*;
import static ed.net.lb.Mapping.*;

public class Server {
    Server( InetSocketAddress addr ){
        if ( addr == null )
            throw new NullPointerException( "addr can't be null" );
	_addr = addr;
	_monitor = ServerMonitor.register( this );
        _tracker = new HttpLoadTracker( "Server : " + addr );
	reset();
    }
    
    void reset(){
	_environmentsWithTraffic.clear();
	_serverStart = System.currentTimeMillis();
	_inErrorState = false;
    }
    
    void error( Environment env , NIOClient.ServerErrorType type , Exception what , HttpRequest request , HttpResponse response ){
	_inErrorState = true;
        _tracker.hit( request , response );
    }
    
    void success( Environment env , HttpRequest request , HttpResponse response ){
	_environmentsWithTraffic.add( env );
        _tracker.hit( request , response );
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
    
    void update( ServerMonitor.Status status ){
	if ( status == null ){
	    _inErrorState = true;
	    return;
	}
	
	// TODO
	
    }

    public int hashCode(){
	return _addr.hashCode();
    }

    public boolean equals( Object o ){
	if ( ! ( o instanceof Server ) )
	    return false;

	Server s = (Server)o;
	return _addr.equals( s._addr );
    }

    public String toString(){
	return _addr.toString();
    }
    
    final InetSocketAddress _addr;
    final ServerMonitor.Monitor _monitor;
    final HttpLoadTracker _tracker;

    final Set<Environment> _environmentsWithTraffic = Collections.synchronizedSet( new HashSet<Environment>() );
    long _serverStart;
    boolean _inErrorState = false;
}
