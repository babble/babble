// ServerMonitor.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.net.lb;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.js.*;
import ed.util.*;
import ed.log.*;
import ed.net.httpserver.*;


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
                try {
                    m.check();
                }
                catch ( Exception e ){
                    _logger.error( "error checking [" + m._server + "]" , e );
                }
	    }
	    ThreadUtil.sleep( 100 );
	}
    }

    class Monitor {
	Monitor( Server s ){
	    _server = s;
	    String host = s._addr.getHostName();
            
            if ( host.equalsIgnoreCase( "localhost" ) ){
                _internalAppServer = true;
            }
	    else if ( host.indexOf( "." ) < 0  ){
		host += "." + Config.getInternalDomain();
                _internalAppServer = true;
            }
	    else if ( ! host.contains( Config.getInternalDomain()  ) ){
                _internalAppServer = false;
                _logger.error( "non internal appserver [" + host + "]" );
            }
            else {
                _internalAppServer = true;
            }
            
	    try {
		_base = new java.net.URL( "http://" + host + ":" + s._addr.getPort() + "/" );
	    }
	    catch ( MalformedURLException bad ){
		throw new RuntimeException( "how is this possible with host [" + host + "]" );
	    }
	}
	
	void check(){
	    
	    long now = System.currentTimeMillis();
	    if ( now - _lastCheck < MS_BETWEEN_CHECKS )
		return;
            
            Status s = null;
	    try {
		s = new Status( _base , _internalAppServer );
                _failsInARow = 0;
	    }
	    catch ( CantParse cp ){
		_logger.error( cp.toString() + " PLEASE CHECK SECURITY" );
		now += ( 1000 * 90 );
	    }
	    catch ( IOException ioe ){
                
                if ( _failsInARow % 50 == 0 )
                    _logger.error( "error checking host [" + _base + "]" , ioe );
                else if ( _failsInARow % 2 == 0 )
                    _logger.error( "still having errors checking host [" + _base + "] : " + ioe );

                _failsInARow++;
		_lastStatus = null;

                if ( _failsInARow > 10 )
                    now += MS_BETWEEN_CHECKS;
                if ( _failsInARow > 100 )
                    now += MS_BETWEEN_CHECKS;
	    }
            finally {
                _lastCheck = now;
                _lastStatus = s;
                _server.update( s );
                _logger.debug( 2 , "server check" , _server , s );
            }
	}
        
	final Server _server;
        final boolean _internalAppServer;
	final URL _base;
        
	long _lastCheck = 0;
	Status _lastStatus;
        int _failsInARow = 0;
    }

    class Status {
	
	Status( URL url , boolean internalAppServer )
	    throws IOException {
	    
	    _whenChecked = System.currentTimeMillis();

            if ( internalAppServer ){
                // ~mem
                JSObject mem = fetch( buildURL( url , "mem" ) );
                JSObject before = (JSObject)mem.get( "before" );
                _memMax = StringParseUtil.parseInt( before.get( "max" ).toString() , -1 );
                _memFree = StringParseUtil.parseInt( before.get( "free" ).toString() , -1 );
                
                
                // ~stats
                JSObject stats = fetch( buildURL( url , "stats" ) );
                _uptimeMinutes = StringParseUtil.parseInt( stats.get( "uptime" ).toString() , -1 );
            }
            else {
                XMLHttpRequest x = new XMLHttpRequest( url );
                if ( x.send() == null )
                    throw x.getError();

                _uptimeMinutes = -1;
                _memFree = -1;
                _memMax = -1;
            }
            
            
            // -- DONE
            
	    _timeToCheck = System.currentTimeMillis() - _whenChecked;
	}
	
        public String toString(){
            StringBuilder buf = new StringBuilder();
            buf.append( "_whenChecked: " ).append( new java.util.Date( _whenChecked ) ).append( " " );
            buf.append( "_timeToCheck: " ).append( _timeToCheck ).append( " " );
            buf.append( "_uptimeMinutes: " ).append( _uptimeMinutes ).append( " " );
            buf.append( "_memMax: " ).append( _memMax ).append( " " );
            buf.append( "_memFree: " ).append( _memFree ).append( " " );
            return buf.toString();
        }
        
	final long _whenChecked;
	final long _timeToCheck;
        
	final int _uptimeMinutes;
	final int _memMax;
	final int _memFree;

    }
    
    URL buildURL( URL base , String page )
        throws MalformedURLException {
        return buildURL( base , page , null );
    }
    
    URL buildURL( URL base , String page , String extra )
        throws MalformedURLException {
        return new URL( base , "/~" + page + "?json=true&auth=" + HttpMonitor.AUTH_COOKIE + "&" + ( extra == null ? "" : extra ) );
    }

    JSObject fetch( URL url )
        throws IOException {
        _logger.debug( 4 , url );

        XMLHttpRequest x = new XMLHttpRequest( url );
        if ( x.send() == null )
            throw x.getError();
	try {
            JSObject ret = (JSObject)(x.getJSON());
            if ( ret == null )
                throw new RuntimeException( "no data : " + x.getResponseText() );
	    return ret;
	}
	catch ( Exception e ){
	    throw new CantParse( url.toString() );
	}
    }
    
    static class CantParse extends IOException {
	CantParse( String url ){
	    super( "couldn't parse json from [" + url + "]" );
	}

	public String toString(){
	    return getMessage();
	}
    }

    private Map<Server,Monitor> _monitors = Collections.synchronizedMap( new HashMap<Server,Monitor>() );
}
