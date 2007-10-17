// DNSServer.java

package ed.net.dnsserver;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import org.xbill.DNS.*;

import ed.net.*;

public class DNSServer extends Thread {
    
    static boolean D = true;
    
    public DNSServer( int port )
        throws IOException {
        _port = port;
        _tcp = new TcpDNSServer( port );
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
                        e.printStackTrace();
                    }
                }
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
            finally {
                if ( dc != null ){
                    try {
                        dc.close();
                    }
                    catch ( IOException ioe ){
                        ioe.printStackTrace();
                    }
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
    
    public static void main( String args[] )
        throws Exception {
        DNSServer s = new DNSServer( 7777 );
        s.start();
        s.join();
    }

}
