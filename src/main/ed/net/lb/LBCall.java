// LBCall.java

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
import java.text.*;

import org.apache.commons.cli.*;

import ed.io.*;
import ed.js.*;
import ed.log.*;
import ed.net.*;
import ed.net.nioclient.*;
import ed.cloud.*;
import ed.net.httpserver.*;
import static ed.net.lb.LB.State;
import static ed.net.lb.Mapping.*;
import static ed.net.nioclient.NIOClient.*;

class LBCall extends Call {

    static final long WAIT_FOR_POOL_TIMEOUT = 10000;

    LBCall( LB lb , HttpRequest req , HttpResponse res ){
        _lb = lb;
        _logger = lb._logger;
        _request = req;
        _response = res;
        reset();
        
        _lb._lastCalls.add( this );
        
        _environemnt = _lb._router.getEnvironment( _request );
        if ( _environemnt == null )
            _lb._loadMonitor.hitSite( "unknown" );
        else
            _lb._loadMonitor.hitSite( _environemnt.site );
    }
        
    void reset(){
        _response.clearHeaders();
        _response.setHeader( "X-lb" , LB.LBIDENT );
        _state = State.WAITING;
        _line = null;
    }

    void success(){
        done();
        _lb._router.success( _request , _response , _lastWent );
        _lb._loadMonitor.hit( _request , _response );
    }

    public void done(){
        super.done();
        _lb.log( this );
    }
        
    protected InetSocketAddress where(){
        _lastWent = _lb._router.chooseAddress( _environemnt , _request , _request.elapsed() > WAIT_FOR_POOL_TIMEOUT );
        return _lastWent;
    }
        
    protected InetSocketAddress lastWent(){
        return _lastWent;
    }
        
    protected void error( ServerErrorType type , Exception e ){
        _logger.debug( 1 , "backend error" , e );
        _lb._loadMonitor._all.networkEvent();
        _lb._router.error( _request , _response , _lastWent , type , e );
	    
        if ( ! _response.isCommitted() && 
             type != ServerErrorType.WEIRD && 
             ( _state == State.WAITING || _state == State.IN_HEADER ) && 
             ++_numFails <= 3 ){
            reset();
            _logger.debug( 1 , "retrying" );
            _lb.add( this );
            return;
        }
            
        try {
            if ( ! _response.isCommitted() ){
                _response.setResponseCode( 500 );
                _response.getJxpWriter().print( "backend error : " + e + "\n\n" );
            }
            _response.done();
            _state = State.ERROR;
            done();
            _lb._loadMonitor.hit( _request , _response );
        }
        catch ( IOException ioe2 ){
            _logger.debug( "error sending error mesasge to client" , ioe2 );
        }
    }
	
        
    void backendError( ServerErrorType type , String msg ){
        backendError( type , new IOException( msg ) );
    }
        
    void backendError( ServerErrorType type , IOException ioe ){
        ioe.printStackTrace();
        System.out.println( "HERE" );
        _logger.error( "backend error" , ioe );
        error( type , ioe );
    }

    protected WhatToDo handleRead( ByteBuffer buf , Connection conn ){

        _logger.debug( 3 , "handleRead  _state:" + _state );
            
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
                        _logger.debug( 3 , "end of header" );
                        _state = State.READY_TO_STREAM;
                        break;
                    }

                    String line = _line.toString();                        

                    if ( _state == State.WAITING ){
                        int idx = line.indexOf( " " );
                        if ( idx < 0 ){
                            backendError( ServerErrorType.INVALID , "invalid first line [" + line + "]" );
                            return WhatToDo.ERROR;
                        }
                        line = line.substring( idx + 1 ).trim();
                        idx = line.indexOf( " " );
                        if ( idx < 0 )
                            _response.setResponseCode( Integer.parseInt( line ) );
                        else
                            _response.setStatus( Integer.parseInt( line.substring( 0 , idx ) ) , line.substring( idx + 1 ).trim() );
                            
                        _logger.debug( 3 , "got first line " , line );
                        _state = State.IN_HEADER;
                    }
                    else {
                        int idx = line.indexOf( ":" );
                        if ( idx < 0 ){
                            backendError( ServerErrorType.INVALID , "invalid line [ " + line + "]" );
                            return WhatToDo.ERROR;
                        }
                        String name = line.substring( 0 , idx );
                        String value = line.substring( idx + 1 ).trim();
                        _logger.debug( 3 , "got header line " , line );
                            
                        if ( name.equalsIgnoreCase( "Connection" ) ){
                            _keepalive = ! value.toLowerCase().contains( "close" );
                        }
                        else {
                            _response.addHeader( name , value );
                        }

                    }
                    _line.setLength( 0 );
                    continue;
                }
                _line.append( c );
            }
                
            if ( _state != State.READY_TO_STREAM )
                return WhatToDo.CONTINUE;

            _logger.debug( 3 , "starting to stream data" );
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
            _logger.debug( 1 , "client error" , ioe );
            return WhatToDo.CLIENT_ERROR;
        }
        return WhatToDo.PAUSE;
    }
        
    protected ByteStream fillInRequest( ByteBuffer buf ){
        buf.put( generateRequestHeader().getBytes() );
        if ( _request.getPostData() != null )
            return _request.getPostData().getByteStream();
        return null;
    }

    String generateRequestHeader(){
        StringBuilder buf = new StringBuilder( _request.getRawHeader().length() + 200 );
        buf.append( _request.getMethod().toUpperCase() ).append( " " ).append( _request.getURL() ).append( " HTTP/1.0\r\n" );
        buf.append( "Connection: keep-alive\r\n" );
        buf.append( HttpRequest.REAL_IP_HEADER ).append( ": " ).append( _request.getRemoteIP() ).append( "\r\n" );
        buf.append( "X-fromlb: " ).append( LB.LBIDENT ).append( "\r\n" );

        for ( String n : _request.getHeaderNameKeySet() ){
                
            if ( n.equalsIgnoreCase( "Connection" ) || 
                 n.equalsIgnoreCase( HttpRequest.REAL_IP_HEADER ) )
                continue;
                
            final String v = _request.getHeader( n );
                
            buf.append( n ).append( ": " ).append( v ).append( "\r\n" );

        }
            
        _environemnt.getExtraHeaderString( buf );

        buf.append( "\r\n" );

        _logger.debug( 3 , "request\n" , buf );
            
        return buf.toString();
    }
        
    public String toString(){
        return _request.getFullURL();
    }

    public String getStateString(){
        if ( isDone() )
            return "DONE";
        return _state.toString();
    }

    final LB _lb;
    final Logger _logger;
    final HttpRequest _request;
    final HttpResponse _response;
    final Environment _environemnt;
        
    int _numFails = 0;

    private InetSocketAddress _lastWent;
    
    private boolean _keepalive;
    private State _state;
    private StringBuilder _line;
    

    // --- internal classes

    class MySender extends JSFile.Sender {
        MySender( MyChunk chunk ){
            super( chunk , chunk._length );
            _chunk = chunk;
        }

        public void cancelled(){
            _chunk.cancelled();
        }

        final MyChunk _chunk;
    }

    class MyChunk extends JSFileChunk {
        MyChunk( LBCall call , Connection conn , long length , ByteBuffer buf ){
            _call = call;
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
            _logger.debug( 4, "want next chunk of data" );
            if ( _sent == _length ){
                _logger.debug( 4 , "no more data" );
                _conn.done( ! _call._keepalive );
                _call._state = State.DONE;
                _call.success();
                return null;
            }
            
            _conn.doRead( _length > 0 );
            _sent += _data.length();            
            _last = _data.length();
            _logger.debug( _last == 0 ? 4 : 3 , "sent " + _sent + "/" + _length );
            return this;
        }

        void cancelled(){
            _conn.userError( "file transfer cancelled" );
        }
        
        long _sent = 0;
        long _last = -1;
        
        final LBCall _call;
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


}
