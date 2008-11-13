// Horrible.java

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

import org.testng.annotations.Test;

import ed.*;
import ed.net.*;
import ed.io.*;
import ed.util.*;
import ed.net.httpserver.*;

public class StrangeRequestTest extends HttpServerTest {

    public StrangeRequestTest()
        throws IOException {
        super( 10003 );

        _lb = new LB( _lbPort , new MyMappingFactory() , 0 );
        _lb.start();
    }

    
    protected void finalize() throws Throwable {
        super.finalize();
        _lb.shutdown();
    }
    
    protected void checkResponse( Response r ){
        assert( r.headers.containsKey( "x-lb" ) );
    }

    protected Socket getSocket()
        throws IOException {
        return new Socket(  DNSUtil.getMyAddresses().get(0) , _lbPort);
    }

    @Test
    public void testContentLen() 
        throws IOException {
        // unescaped content
        sendItOff( headers("POST", " ", "\n") );
        // incorrect content length
        sendItOff( headers("POST", "", "Content-Length: 20\n") );
        // incorrect, non-int length
        sendItOff( headers("POST", "", "Content-Length: 40000000000\n") );
    }

    @Test
    public void testPost() 
        throws IOException {
        // crazy long get with repeated fields
        String s = "x=abc&";
        for( int i=0; i<10; i++) {
            s = s+s;
        }
        sendItOff( headers("GET", s, "") );
    }

    @Test
    public void testMethod() 
        throws IOException {
        // fake method
        sendItOff( headers("BLORT", "", "") );
        sendItOff( "DELETE http://shopwiki.local.10gen.com\nAccept: text/html\n" );
        sendItOff( "PUT http://shopwiki.local.10gen.com\nAccept: text/html\n" );
        sendItOff( "PUT http://shopwiki.local.10gen.com\nAccept: text/html\n\nhey=there" );
        sendItOff( "HEAD http://shopwiki.local.10gen.com\n" );
    }

    @Test
    public void testMalformedMethod() 
        throws IOException {
        // malformed
        sendItOff( "POST \nhttp://shopwiki.local.10gen.com\n" );
        sendItOff( "POST htt://shopwiki.local.10gen.com\n" );
        sendItOff( "POST http://shopwiki.local.10gen.com" );
        sendItOff( "POST foo" );
        sendItOff( "POST google\n\n.c\no\nm" );
        sendItOff( "POST &g&oog&le\n" );
        sendItOff( "POST \uF32A\n" );
    }

    // ---parameters---
    @Test
    public void testAccept() 
        throws IOException {
        // accept
        sendItOff( headers("GET", "", "Accept: audio/*, q=0,1, audio/basic\n" ) );
        sendItOff( headers("GET", "", "Accept:text/plain,\rtext/html\n" ) );
        sendItOff( headers("GET", "", "Accept: foop/dedoop\n" ) );
    }

    @Test
    public void testParams() 
        throws IOException {
        sendItOff( "GET http://shopwiki.local.10gen.com\nAccept-Charset: x-mac-arabic, q=1" );
        sendItOff( "GET http://shopwiki.local.10gen.com\nAccept-Encoding:" );
        sendItOff( "GET http://shopwiki.local.10gen.com\nAccept-Encoding: blort;q=0.5" );
        sendItOff( "GET http://shopwiki.local.10gen.com\nAccept-Language: da, en-gb;q=0.8, en;q=0.7" );
        sendItOff( "GET http://shopwiki.local.10gen.com\nContent-Encoding: exe" );
        sendItOff( "GET http://shopwiki.local.10gen.com\nContent-Encoding: blort" );
    }

    @Test
    public void testLongParameter() 
        throws IOException {
        String s = "abcde";
        for( int i=0; i<10; i++) {
            s = s+s;
        }
        sendItOff( headers("GET", "", s+"\n" ) );
    }
     
    private void sendItOff( StringBuilder buf ) throws IOException {
        sendItOff( buf.toString() );
    }

    private void sendItOff( String buf ) throws IOException {
        System.out.print(".");
        
        Socket s = open();
        s.getOutputStream().write(buf.getBytes());
        InputStream in = s.getInputStream();
        Response r = read(in);
        assertEquals(PingHandler.DATA, r.body);
    }

    class MyMappingFactory implements MappingFactory {
        
        MyMappingFactory()
            throws IOException {
            _pools.add( "prod1" );
            _addrs.add( new InetSocketAddress( "local.10gen.cc" , _port ) );
        }
        
        public long refreshRate(){
            return 1000 * 1000;
        }
        
        public Mapping getMapping(){
            return new Mapping(){

                public Environment getEnvironment( HttpRequest request ){
                    return new Environment( "shopwiki" , "www" , null );
                }

                public String getPool( Environment e ){
                    return _pools.get(0);
                }

                public String getPool( HttpRequest request ){
                    return _pools.get(0);
                }

                public List<InetSocketAddress> getAddressesForPool( String poolName ){
                    return _addrs;
                }
                
                public List<String> getPools(){
                    return _pools;
                }
                
                public String toFileConfig(){
                    throw new RuntimeException( "blah" );
                }
                
                public boolean reject( HttpRequest request ){
                    return false;
                }

            };
        }
        
        List<InetSocketAddress> _addrs = new ArrayList<InetSocketAddress>();
        List<String> _pools = new ArrayList<String>();
        
    }
    
    final LB _lb;
    final int _lbPort = 10002;

    public static void main(String args[])
            throws IOException {
        
        StrangeRequestTest h = new StrangeRequestTest();
        h.runConsole();
    }
    
}
