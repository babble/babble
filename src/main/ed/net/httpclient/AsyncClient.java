// AsyncClient.java

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

package ed.net.httpclient;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.io.*;
import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.net.nioclient.*;

public class AsyncClient extends NIOClient {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.HTTPC" );    
    
    public AsyncClient(){
        super( "http-asyncclient" , 1000 , 0 );
        _logger = Logger.getLogger( "httpclient-async" );
        setDaemon( true );
        start();
    }
    
    protected void serverError( InetSocketAddress addr , ServerErrorType type , Exception why ){
        _logger.error( "error talking to : " + addr + " : " + type , why );
    }

    public DelayedHttpResponse send( String server , String method , String path , String host , String extraHeaders ){
        
        StringBuilder headers = new StringBuilder();
        headers.append( method ).append( " " ).append( path ).append( " HTTP/1.0\r\n" );
        headers.append( "Host: " ).append( host ).append( "\r\n" );

        if ( extraHeaders != null ){
            if ( extraHeaders.toLowerCase().contains( "connection:" ) )
                throw new RuntimeException( "can't control connection" );
            headers.append( extraHeaders );
        }

        headers.append( "Connection: Close\r\n" );
        headers.append( "\r\n" );
        
        DelayedHttpResponse response = new DelayedHttpResponse();
        add( new MyCall( server , headers.toString() , response ) );
        return response;
    }
    
    class MyCall extends Call {
        
        MyCall( String server , String header , DelayedHttpResponse response ){
        
            if ( server.contains( ":" ) ){
                _server = server.substring( 0 , server.indexOf( ":" ) );
                _port = Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) );
            }
            else {
                _server = server;
                _port = 80;
            }
            
            _header = header;
            _response = response;
        }
        
        protected InetSocketAddress where(){
            final InetSocketAddress where = new InetSocketAddress( _server , _port );
            if ( DEBUG ) System.out.println( where );
            return where;
        }
        
        protected void error( ServerErrorType type , Exception e ){
            _response.gotError( e );
        }
    
        protected ByteStream fillInRequest( ByteBuffer buf ){
            if ( DEBUG ) System.out.println( _header );
            buf.put( _header.getBytes() );
            return null;
        }
        
        protected WhatToDo handleRead( ByteBuffer buf , Connection conn ){
            if ( DEBUG ) System.out.println( buf );

            if ( _finishedHeader ){
                _response._data.put( buf );
                if ( _response.hasAllData() ){
                    _response.done();
                    return WhatToDo.DONE_AND_CLOSE;
                }
                return WhatToDo.CONTINUE;
            }

            while ( buf.hasRemaining() ){
                char c = (char)buf.get();
                if ( c == '\r' )
                    continue;
                
                if ( c != '\n' ){
                    _line.append( c );
                    continue;
                }
                
                final String l = _line.toString().trim();
                _line.setLength( 0 );

                if ( DEBUG ) System.out.println( "[" + l + "]" );

                if ( _response._statusLine == null ){
                    _response._statusLine = _line.toString();
                    continue;
                }

                if ( l.length() == 0 ){
                    _finishedHeader = true;
                    return handleRead( buf , conn );
                }

                int idx = l.indexOf( ":" );
                if ( idx < 0 ){
                    _response._error = new IOException( "invalid status line [" + l + "]" );
                    return WhatToDo.ERROR;
                }
                
                _response._headers.put( l.substring( 0 , idx ).trim() , 
                                        l.substring( idx + 1 ).trim() );
            }

            return WhatToDo.CONTINUE;
        }
        
        public void done(){
            _response.done();
            super.done();
        }

        final String _server;
        final int _port;
        final String _header;
        
        final DelayedHttpResponse _response;
        
        private boolean _finishedHeader = false;
        private StringBuilder _line = new StringBuilder();
    }

    public class DelayedHttpResponse {
        
        public boolean isDone(){
            return _done;
        }
        
        public void finish()
            throws IOException {
            final Thread t = Thread.currentThread();
            
            try {
                while ( ! _done ){
                    _waiters.add( t );
                    ThreadUtil.sleep( 100 );
                }           
            }
            finally {
                _waiters.remove( t );
            }
            
            if ( _error == null )
                return;

            if ( _error instanceof IOException )
                throw (IOException)_error;

            if ( _error instanceof RuntimeException )
                throw (RuntimeException)_error;
            
            throw new RuntimeException( "error in client" , _error );
        }

        public Exception getError(){
            return _error;
        }
        
        boolean hasAllData(){
            String cl = _headers.get( "Content-Length" );
            if ( cl == null )
                return false;
            
            if ( DEBUG ) System.out.println( "cl:" + cl + " pos:" + _data.position() );
            
            return _data.position() >= Integer.parseInt( cl );
        }
        
        void gotError( Exception e ){
            e.printStackTrace();
            _error = e;
            done();
        }
        
        void done(){
            _done = true;
            for ( Thread t : _waiters )
                t.interrupt();
        }
        
        public String toString(){
            return _statusLine + "\n" + _headers;
        }
        
        Exception _error = null;
        boolean _done = false;
        Set<Thread> _waiters = new HashSet<Thread>();
        
        String _statusLine;
        Map<String,String> _headers = new StringMap<String>();
        
        ByteBufferHolder _data = new ByteBufferHolder();
        

    }

    final Logger _logger;
}
