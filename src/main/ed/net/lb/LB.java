// LB.java

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
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.cli.*;

import ed.net.*;
import ed.cloud.*;
import ed.net.httpserver.*;

public class LB extends NIOClient {

    public LB( int port , boolean verbose )
        throws IOException {
        super( "LB" , verbose );
        
        _port = port;
        _handler = new LBHandler();
        
        _server = new HttpServer( port );
        _server.addGlobalHandler( _handler ) ;
        
        _cloud = Cloud.getInstanceIfOnGrid();

        setDaemon( true );
    }
    
    public void start(){
        _server.start();
        super.start();
    }
    
    public void run(){
        _debug( "Started" );
        super.run();
    }

    private void _debug( String msg ){
        if ( ! _verbose )
            return;
        
        System.out.print( "LB: " );
        System.out.println( msg );
    }

    class RR extends Call {
        RR( HttpRequest req , HttpResponse res ){
            _request = req;
            _response = res;
        }
        
        protected InetSocketAddress where(){
            return new InetSocketAddress( "www.google.com" , 80 );
        }

        protected void errorOpen( IOException ioe ){
            try {
                _response.getJxpWriter().print( "couldn't open connection : " +  ioe );
                _response.done();
            }
            catch ( IOException ioe2 ){
                ioe2.printStackTrace();
            }
        }

        protected void handleRead(){}
        protected void handleConnect(){}

        final HttpRequest _request;
        final HttpResponse _response;
    }
    
    class LBHandler implements HttpHandler {
        public boolean handles( HttpRequest request , Info info ){
            info.admin = false;
            info.fork = false;
            info.doneAfterHandles = false;
            return true;
        }
        
        public void handle( HttpRequest request , HttpResponse response ){
            if ( add( new RR( request , response ) ) )
                return;
            
            JxpWriter out = response.getJxpWriter();
            out.print( "new request queue full  (lb 1)" );
            try {
                response.done();
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
            }
        }
        
        public double priority(){
            return Double.MAX_VALUE;
        }
    }

    final int _port;
    final LBHandler _handler;
    final HttpServer _server;
    final Cloud _cloud;

    public static void main( String args[] )
        throws Exception {
        
        Options o = new Options();
        o.addOption( "p" , "port" , true , "Port to Listen On" );
        o.addOption( "v" , "verbose" , false , "Verbose" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        final int port = Integer.parseInt( cl.getOptionValue( "p" , "8080" ) );
        final boolean verbose = cl.hasOption( "v" );

        System.out.println( "10gen load balancer" );
        System.out.println( "\t port \t " + port  );
        System.out.println( "\t verbose \t " + verbose  );

        LB lb = new LB( port , verbose );
        lb.start();
        lb.join();
    }
}
