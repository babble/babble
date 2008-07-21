// EchoServer.java

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

package ed.net;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class EchoServer extends NIOServer {

    public EchoServer( int port )
        throws IOException {
        super( port );
        start();
    }
    
    protected EchoSocketHandler accept( SocketChannel sc ){
        return new EchoSocketHandler( sc );
    }
    
    class EchoSocketHandler extends NIOServer.SocketHandler {
        EchoSocketHandler( SocketChannel sc ){
            super( sc );
        }
        
        protected boolean shouldClose(){
            return false;
        }
        
        protected void writeMoreIfWant(){
        }

        protected boolean gotData( ByteBuffer inBuf )
            throws IOException {
            
            byte bb[] = new byte[inBuf.remaining()];
            inBuf.get( bb );
            
            _writeBuf.clear();
            _writeBuf.put( ( new String( bb ) ).getBytes() );
            _writeBuf.flip();
            _channel.write( _writeBuf );
            
            return false;
        }
        
    }    
    
    final ByteBuffer _writeBuf = ByteBuffer.allocateDirect( 2048 );

    public static void main( String args[] )
        throws Exception {
        EchoServer es = new EchoServer( 9999 );
        es.join();
    }
}
