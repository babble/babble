// DNSServer.java

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

package ed.net.dnsserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import org.xbill.DNS.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;
import ed.net.*;
import ed.log.*;


public class DNSServer extends Thread {
    
    static boolean D = Boolean.getBoolean( "DEBUG.DNS" );
    static Logger LOGGER = Logger.getLogger( "dnsserver" );

    public DNSServer( int port )
        throws IOException {

        _port = port;
        _tcp = new TcpDNSServer( port );
        
        _scope = Scope.newGlobal().child();
        _scope.setGlobal( true );

        _scope.evalFromPath( "ed/net/dnsserver/dns.js" , "dns.js" );
        _function = _scope.getFunction( "eval" );
        
        _scope.put( "add" , 
                    new JSFunctionCalls4(){
                        public Object call( Scope s , Object fromObject , Object typeObject , Object ttlObject ,  Object toObject , Object foo[] ){
                            
                            try {
                                final Message result = (Message)(s.get( "result" ));
                                final Message query = (Message)(s.get( "query" ));

                                final String from = fromObject.toString();
                                final String type = typeObject.toString();
                                final int ttl = ((Number)ttlObject).intValue();
                                final String to = toObject == null ? null : toObject.toString();
                                                                    
                                final Name base = new Name( from );
                                
                                if ( type.equalsIgnoreCase( "C" ) || type.equalsIgnoreCase( "CNAME" ) ){
                                    result.addRecord( new CNAMERecord( base , DClass.IN , ttl , new Name( to , base ) ) , Section.ANSWER );
                                }
                                else if ( type.equalsIgnoreCase( "A" ) ){
                                    result.addRecord( new ARecord( base , DClass.IN , ttl , InetAddress.getByName( to ) ) , Section.ANSWER );
                                }
                                else if ( type.equalsIgnoreCase( "NS" ) ){
                                    if ( query.getQuestion().getType() == Type.NS )
                                        result.addRecord( new NSRecord( base , DClass.IN , ttl , new Name( to , base ) ) , Section.ANSWER );
                                    else
                                        result.addRecord( new NSRecord( base , DClass.IN , ttl , new Name( to , base ) ) , Section.AUTHORITY );
                                }
                                else {
                                    throw new RuntimeException( "can't handle type : " + type );
                                }
                                    
                            }
                            catch ( Exception e ){
                                throw new RuntimeException( e );
                            }

                            return null;
                        }
                    } ,
                    true );

        {
            List<InetAddress> lst = DNSUtil.getPublicAddresses();
            if ( lst == null || lst.size() == 0 )
                _scope.put( "local" , "127.0.0.1" , true );
            else
                _scope.put( "local" , lst.get(0).getHostAddress() , true );
        }
    }
    
    Message process( Message query ){

        final Record question = query.getQuestion();
        final Name n = question.getName();

        final Message result = new Message( query.getHeader().getID()  );
        result.getHeader().setOpcode( Opcode.QUERY );
        result.getHeader().setFlag( Flags.AA );
        result.getHeader().setFlag( Flags.QR );
        result.getHeader().setFlag( Flags.RD );
        result.addRecord( question , Section.QUESTION );
        
        Scope temp = _scope.child();
        temp.setGlobal( true );
        temp.put( "query" , query , true );
        temp.put( "result" , result , true );
        
        String host = n.toString();
        _function.call( temp , host , Type.string( question.getType() ) , DNSUtil.getDomain( host ) );
        
        return result;
    }

    public void start(){
        _tcp.start();
        super.start();
    }

    public void run(){
        while ( true ){
            DatagramChannel dc = null;
            try {
                dc = DatagramChannel.open();
                dc.socket().bind( new InetSocketAddress( _port ) );

                ByteBuffer recv = ByteBuffer.allocateDirect( 1024 );
                ByteBuffer out = ByteBuffer.allocateDirect( 1024 );

                while ( true ){
                    try {
                        recv.clear();
                        final SocketAddress remote = dc.receive( recv );
                        recv.flip();
                        
                        final byte bb[] = new byte[recv.remaining()];
                        recv.get( bb );
                        final Message query = new Message( bb );
                        
                        if ( D ) System.out.println( query );

                        final Message response = process( query );
                        out.clear();
                        out.put( response.toWire() );
                        out.flip();
                        dc.send( out , remote );
                    }
                    catch ( Exception e ){
                        LOGGER.error( "error handling request" , e );
                    }
                }
            }
            catch ( Exception e ){
                LOGGER.error( "error in inf. loop" , e );
            }
            finally {
                if ( dc != null ){
                    try {
                        dc.close();
                    }
                    catch ( IOException ioe ){
                        // couldn'e close - don't think we care
                    }
                    dc = null;
                }
            }
        }
    }

    class TcpDNSServer extends NIOServer {
        TcpDNSServer( int port )
            throws IOException {
            super( port );
        }
        

        protected SocketHandler accept( SocketChannel sc ){
            return new DNSSocketHandler( sc );
        }
        
        class DNSSocketHandler extends SocketHandler {
            DNSSocketHandler( SocketChannel sc ){
                super( sc );
            }
            
            protected boolean gotData( ByteBuffer inBuf )
                throws IOException {
                
                final byte sizeBuf[] = new byte[2];
                inBuf.get( sizeBuf );
            
                final int size = ((sizeBuf[0] & 0x000000ff) << 8) + (sizeBuf[1] & 0x000000ff);
                final byte inBytes[] = new byte[size];
                inBuf.get( inBytes );
            
                final Message query = new Message( inBytes );
            
                if ( D ) System.out.println( query );

                final Message response = process( query );
                final byte outBytes[] = response.toWire();

                _writeBuf.clear();
                _writeBuf.put( outBytes );
                _writeBuf.flip();
                _channel.write( _writeBuf );

                _done = true;
                registerForWrites(); // get ready for close
                return false;
            }

            protected boolean shouldClose(){
                return _done;
            }
        
            protected void writeMoreIfWant() 
                throws IOException {
                // NO-OP
            }
        
            boolean _done = false;
            
        }

        final ByteBuffer _writeBuf = ByteBuffer.allocateDirect( 1024 );
    }

    TcpDNSServer _tcp;
    final int _port;

    final Scope _scope;
    final JSFunction _function;
    
    public static void main( String args[] )
        throws Exception {
        DNSServer s = new DNSServer( 7777 );
        
        if ( false ){
            Message q = Message.newQuery( Record.newRecord( new Name( "www.shopwiki.com." ) , Type.A , DClass.IN ) );
            System.out.println( s.process( q ) );
            return;
        }
        
        s.start();
        s.join();

    }

}
