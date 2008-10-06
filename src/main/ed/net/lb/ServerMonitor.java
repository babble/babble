// ServerMonitor.java

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

import ed.util.*;
import ed.log.*;


public class ServerMonitor extends Thread {
    
    static final long MS_BETWEEN_CHECKS = 2000;

    private static final ServerMonitor INSTANCE = new ServerMonitor();
    static final Logger _logger = Logger.getLogger( "lb-monitor" );
    
    static Monitor register( Server s ){
	return INSTANCE.getMonitor( s , true );
    }

    ServerMonitor(){
	super( "ServerMonitor" );
	setDaemon( true );
	start();
    }

    Monitor getMonitor( Server s ){
	return getMonitor( s , false );
    }

    Monitor getMonitor( Server s , boolean create ){
	Monitor m = _monitors.get( s );
	if ( m != null || ! create )
	    return m;

	synchronized ( _monitors ){
	    m = _monitors.get( s );
	    if ( m != null )
		return m;
	    
	    m = new Monitor( s );
	    _monitors.put( s , m );
	}
	return m;
    }
    
    public void run(){
	while ( true ){
	    for ( Server s : new HashSet<Server>( _monitors.keySet() ) ){
		Monitor m = getMonitor( s );
		m.check();
	    }
	    ThreadUtil.sleep( 100 );
	}
    }

    class Monitor {
	Monitor( Server s ){
	    _server = s;
	    String host = s._addr.getHostName();
	    if ( host.indexOf( "." ) < 0 )
		host += "." + Config.getInternalDomain();
	    else if ( ! host.contains( Config.getInternalDomain()  ) )
		throw new RuntimeException( "invalid host [" + host + "]" );
	    
	    try {
		_base = new java.net.URL( "http://" + host + "/" );
	    }
	    catch ( MalformedURLException bad ){
		throw new RuntimeException( "how is this possible with host [" + host + "]" );
	    }
	}
	
	void check(){
	    
	    final long now = System.currentTimeMillis();
	    if ( now - _lastCheck < MS_BETWEEN_CHECKS )
		return;

	    try {
		_lastStatus = new Status( _base );
	    }
	    catch ( IOException ioe ){
		_lastStatus = null;
		_logger.error( "error checking host [" + _base + "]" , ioe );
	    }
	    
	    _lastCheck = now;
	    _server.update( _lastStatus );
	}
	
	final Server _server;
	final URL _base;

	long _lastCheck = 0;
	Status _lastStatus;
    }

    class Status {
	
	Status( URL url )
	    throws IOException {
	    
	    System.out.println( url );
	    System.out.println( new URL( url , "/~mem" ) );

	    _whenChecked = System.currentTimeMillis();
	    
	    _uptimeMinutes = 0;
	    _memMax = 0;
	    _memFree = 0;
	    
	    _timeToCheck = System.currentTimeMillis() - _whenChecked;
	}
	
	final long _whenChecked;
	final long _timeToCheck;

	final int _uptimeMinutes;
	final int _memMax;
	final int _memFree;

    }

    private Map<Server,Monitor> _monitors = Collections.synchronizedMap( new HashMap<Server,Monitor>() );
}
