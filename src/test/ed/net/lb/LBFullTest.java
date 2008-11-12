// LBFullTest.java

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
import ed.io.*;
import ed.util.*;
import ed.net.httpserver.*;

public class LBFullTest extends HttpServerTest {

    public LBFullTest()
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
        return new Socket("127.0.0.1", _lbPort);
    }


    @Test
    public void testLoadSmall()
        throws IOException {
        _load( 1 );
    }

    @Test
    public void testLoadMed()
        throws IOException {
        _load( 5 );
    }

    // explicitly left out of testng
    // i guess we should add a "load" group
    public void testLoadBig()
        throws IOException {
        _load( 100 );
    }

    public void _load( int num )
        throws IOException {
        for ( int i=0; i<num; i++ ){
            if ( i % 5 == 1 )
                System.out.print( "." );
            testBasic1();
            testKeepAlive1();
            testPipeLine1();
            testPost1();
            testPost2();
        }
    }

    // explicitly left out of testng
    public void testDroppedClients()
        throws IOException {
        for ( int i=0; i<200; i++ )
            _drop();
    }
    
    void _drop()
        throws IOException {
        
        int num = 2;
        
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < num; i++)
            buf.append(headers("GET", "", "Connection: Keep-Alive\r\n"));

        Socket s = open();
        Response r;

        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();

        out.write(buf.toString().getBytes());

        // starting at i=1 so that there is one left on the stream
        for (int i = 1; i < num; i++) {
            r = read(in);
            assertEquals(PingHandler.DATA, r.body);
            checkResponse( r );
        }

        s.close();
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
        
        LBFullTest ft = new LBFullTest();
        
        for ( int i=0; i<args.length; i++ )
            if ( args[i].equals( "load" ) )
                ft._load = true;

        ft.runConsole();
        
        
        if ( args.length > 0 ){
            try {
                int sleep = Integer.parseInt( args[0] );
                System.out.println( "sleeping for " + sleep + " seconds" );
                ThreadUtil.sleep( sleep * 1000 );
            }
            catch ( NumberFormatException nfe ){}
        }
        
    }
    
}
