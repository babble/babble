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

import ed.js.*;
import ed.log.*;
import ed.net.*;
import ed.cloud.*;
import ed.net.httpserver.*;

public class LB extends NIOClient {

    static final String LBIDENT = DNSUtil.getLocalHost().getHostName() + " : v0" ;

    enum State { WAITING , IN_HEADER , READY_TO_STREAM , STREAMING };

    public LB( int port , int verbose )
        throws IOException {
        super( "LB" , 15 , verbose );
        
        _port = port;
        _handler = new LBHandler();
        
        _logger = Logger.getLogger( "LB" );

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
        _debug( 1 , msg );
    }
    private void _debug( int level , String msg ){
        _debug( level , msg , null );
    }
    private void _debug( int level , String msg , Object data ){
        _debug( level , msg , data , null );
    }
    private void _debug( int level , String msg , Object data1 , Object data2 ){
        if ( _verbose < level )
            return;
        
        StringBuilder buf = new StringBuilder();
        
        buf.append( msg );
        if ( data1 != null )
            buf.append( " [" + data1 + "]" );
        if ( data2 != null )
            buf.append( " [" + data2 + "]" );
        _logger.info( buf );
    }
    
    class RR extends Call {
        
        RR( HttpRequest req , HttpResponse res ){
            _request = req;
            _response = res;
            _response.clearHeaders();
            _response.setHeader( "X-lb" , LBIDENT );
        }
        
        protected InetSocketAddress where(){
            return new InetSocketAddress( "www.10gen.com" , 80 );
        }
        
        protected void error( Exception e ){
            try {
                _debug( 1 , "backend error" , e );
                _response.getJxpWriter().print( "backend error : " + e );
                _response.done();
            }
            catch ( IOException ioe2 ){
                ioe2.printStackTrace();
            }
        }

        
        void backendError( String msg ){
            backendError( new IOException( msg ) );
        }
        
        void backendError( IOException ioe ){
            _debug( 1 , "backend error" , ioe );
            error( ioe );
        }

        protected WhatToDo handleRead( ByteBuffer buf , Connection conn ){

            
            if ( _state == State.WAITING || _state == State.IN_HEADER ){
                // TODO: should i read this all in at once                
                if ( _line == null )
                    _line = new StringBuilder();
                while ( buf.hasRemaining() ){
                    char c = (char)buf.get();
                    if ( c == '\r' )
                        continue;
                    if ( c == '\n' ){
                        if ( _line.length() == 0 ){
                            _debug( 3 , "end of header" );
                            _state = State.READY_TO_STREAM;
                            break;
                        }

                        String line = _line.toString();                        

                        if ( _state == State.WAITING ){
                            int idx = line.indexOf( " " );
                            if ( idx < 0 ){
                                backendError( "invalid first line [" + line + "]" );
                                return WhatToDo.ERROR;
                            }
                            line = line.substring( idx + 1 ).trim();
                            idx = line.indexOf( " " );
                            if ( idx < 0 )
                                _response.setResponseCode( Integer.parseInt( line ) );
                            else
                                _response.setStatus( Integer.parseInt( line.substring( 0 , idx ) ) , line.substring( idx + 1 ).trim() );
                            
                            _debug( 3 , "got first line " , line );
                            _state = State.IN_HEADER;
                        }
                        else {
                            int idx = line.indexOf( ":" );
                            if ( idx < 0 ){
                                backendError( "invalid line [ " + line + "]" );
                                return WhatToDo.ERROR;
                            }
                            String name = line.substring( 0 , idx );
                            String value = line.substring( idx + 1 ).trim();
                            _debug( 3 , "got header line " , line );
                            
                            if ( name.equalsIgnoreCase( "Connection" ) ){
                                _keepalive = ! value.toLowerCase().contains( "close" );
                            }
                            else {
                                _response.setHeader( name , value );
                            }

                        }
                        _line.setLength( 0 );
                        continue;
                    }
                    _line.append( c );
                }
                
                if ( _state != State.READY_TO_STREAM )
                    return WhatToDo.CONTINUE;

                _debug( 3 , "starting to stream data" );
            }

            if ( _state == State.READY_TO_STREAM ){
                MyChunk chunk = new MyChunk( this , conn , _response.getContentLength() , buf );
                _response.sendFile( new MySender( chunk ) );
                _state = State.STREAMING;
            }
            
            try {
                _response.done();
            }
            catch ( IOException ioe ){
                _debug( 2 , "client error" , ioe );
                return WhatToDo.ERROR;
            }
            return WhatToDo.PAUSE;
        }
        
        protected void fillInRequest( ByteBuffer buf ){
            buf.put( generateRequestHeader().getBytes() );
        }

        String generateRequestHeader(){
            StringBuilder buf = new StringBuilder( _request.getRawHeader().length() + 200 );
            buf.append( _request.getMethod() ).append( " " ).append( _request.getURL() ).append( " HTTP/1.0\r\n" );
            buf.append( "Connection: keep-alive\r\n" );
            
            for ( String n : _request.getHeaderNameKeySet() ){
                String v = _request.getHeader( n );
                buf.append( n ).append( ": " ).append( v ).append( "\r\n" );
            }
            buf.append( "\r\n" );

            _debug( 3 , "request\n" , buf );
            
            return buf.toString();
        }
        
        final HttpRequest _request;
        final HttpResponse _response;

        private boolean _keepalive = false;
        private State _state = State.WAITING;
        private StringBuilder _line;
    }

    class MySender extends JSFile.Sender {
        MySender( MyChunk chunk ){
            super( chunk , chunk._length );
        }
    }

    class MyChunk extends JSFileChunk {
        MyChunk( RR rr , Connection conn , long length , ByteBuffer buf ){
            _rr = rr;
            _conn = conn;
            _length = length;
            _buf = buf;
            
            _data = new MyBinaryData( _buf );
            _sent += _data.length();
        }
        
        public JSBinaryData getData(){
            return _data;
        }
        
        public MyChunk getNext(){
            _debug( 4, "want next chunk of data" );
            if ( _sent == _length ){
                _debug( 4 , "no more data" );
                _conn.done( ! _rr._keepalive );
                return null;
            }
            
            _conn.doRead();
            _sent += _data.length();            
            _last = _data.length();
            _debug( _last == 0 ? 4 : 3 , "sent " + _sent + "/" + _length );
            return this;
        }
        
        long _sent = 0;
        long _last = -1;
        
        final RR _rr;
        final Connection _conn;
        final long _length;
        final ByteBuffer _buf;
        final MyBinaryData _data;
    }

    class MyBinaryData extends JSBinaryData {
        MyBinaryData( ByteBuffer buf ){
            _buf = buf;
        }
        
        public int length(){
            return _buf.remaining();
        }

        public void put( ByteBuffer buf ){
            throw new RuntimeException( "not allowed" );
        }
        
        public void write( OutputStream out ){
            throw new RuntimeException( "not allowed" );
        }

        public ByteBuffer asByteBuffer(){
            return _buf;
        }

        final ByteBuffer _buf;
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
    
    final Logger _logger;

    public static void main( String args[] )
        throws Exception {

        int verbose = 0;

        for ( int i=0; i<args.length; i++ ){
            if ( args[i].matches( "\\-v+" ) ){
                verbose = args[i].length() - 1;
                args[i] = "";
            }
        }

        Options o = new Options();
        o.addOption( "p" , "port" , true , "Port to Listen On" );
        o.addOption( "v" , "verbose" , false , "Verbose" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        final int port = Integer.parseInt( cl.getOptionValue( "p" , "8080" ) );
        
        System.out.println( "10gen load balancer" );
        System.out.println( "\t port \t " + port  );
        System.out.println( "\t verbose \t " + verbose  );

        LB lb = new LB( port , verbose );
        lb.start();
        lb.join();
    }
}
