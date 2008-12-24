// RouterTest.java

package ed.net.lb;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.net.httpserver.*;

/**
 * start some appservers
 * mess with them
 * blah
 */
public class RouterTest extends ed.TestCase {

    static final String SITE = "foo.com";
    static final String ENV = "www";
    static final String POOL = "prod1";

    static final int START_PORT = 18231;

    static final Environment DUMMY_ENV = new Environment( SITE , ENV );


    RouterTest(){
        
    }

    public void testServer1(){
        final Server a = new Server( addr( 0 ) , false );
        final Server b = new Server( addr( 1 ) , false );

        final Environment e = new Environment( "foo" , "www" );
        
        assertEquals( a.rating( e ) , b.rating( e ) );
        
        a.error( e , null , null , null , null );
        assertLess( a.rating( e ) , b.rating( e ) );

        b.error( e , null , null , null , null );
        assertEquals( a.rating( e ) , b.rating( e ) );

        a.reset();
        assertLess( b.rating( e ) , a.rating( e ) );

        b.reset();
        assertEquals( a.rating( e ) , b.rating( e ) );

        a.success( e , null , null );
        assertLess( b.rating( e ) , a.rating( e ) );

        b.success( e , null , null );
        assertEquals( b.rating( e ) , a.rating( e ) );
        
    }

    public void test1()
        throws Exception {
        
        final MyRouter r = new MyRouter( 2 );

        Count c = _countPorts( r );
        assertEquals( .5 , c.ratio() , .1 );
        
        r.success( 0 );
        assertEquals( addr(0) , r.next() );
        c = _countPorts( r );
        assertEquals( 1 , c.size() );
        
        r.success( 1 );
        c = _countPorts( r );
        assertEquals( 2 , c.size() );
        assertEquals( .5 , c.ratio() , .1 );
        
        r.error( 0 );
        assertEquals( addr(1) , r.next() );
        c = _countPorts( r );
        assertEquals( 1 , c.size() );
        
        r.success( 0 );
        assertEquals( addr(1) , r.next() );
        c = _countPorts( r );
        assertEquals( 1 , c.size() );
        
        r.reset( 0 );
        r.success( 0 );
        c = _countPorts( r );
        System.out.println( c );
        assertEquals( 2 , c.size() );
        assertEquals( .5 , c.ratio() , .1 );


        r.error( 0 );
        assertEquals( addr(1) , r.next() );
        c = _countPorts( r );
        assertEquals( 1 , c.size() );

        r.reset( 0 );
        assertEquals( addr(1) , r.next() );
        c = _countPorts( r );
        assertEquals( 1 , c.size() );
        
    }
    
    Count _countPorts( MyRouter r ){
        
        Count c = new Count();
        for ( int i=0; i<1000; i++ ){
            c.inc( r.next() );
        }
        return c;
    }

    // ------ INTERNAL -------

    class MyRouter extends Router {
        MyRouter( int num ){
            super( new MyMappingFactory( num ) );
            _random.setSeed( 17123121 );
        }

        Server _createServer( InetSocketAddress addr ){
            return new Server( addr , false );
        }

        InetSocketAddress next(){
            return chooseAddressForPool( DUMMY_ENV , POOL , false );
        }

        void success( int num ){
            success( null , null , addr( num ) , DUMMY_ENV );
        }
        
        void error( int num ){
            getServer( addr(num) ).error( DUMMY_ENV , null , null , null , null );
        }

        void reset( int num ){
            getServer( addr( num ) ).reset();
        }
    }
                      
    class MyMappingFactory implements MappingFactory , Mapping {
        
        MyMappingFactory( int num ){
            _num = num;
            _addrs = new LinkedList<InetSocketAddress>();
            for ( int i=0; i<_num; i++ )
                _addrs.add( addr( i ) );
        }
        
        public Mapping getMapping(){
            return this;
        }

        public long refreshRate(){
            return 1000 * 3600 * 48;
        }

        public Environment getEnvironment( HttpRequest request ){
            return new Environment( SITE , ENV );
        }
        
        public String getPool( Environment e ){
            return POOL;
        }
        
        public String getPool( HttpRequest request ){
            return POOL;
        }
        
        public List<InetSocketAddress> getAddressesForPool( String poolName ){
            return _addrs;
        }
        
        public List<String> getPools(){
            List<String> l = new LinkedList<String>();
            l.add( POOL );
            return l;
        }

        public String toFileConfig(){
            throw new RuntimeException( "blah" );
        }
        
        public boolean reject( HttpRequest request ){
            return false;
        }

        final int _num;
        final List<InetSocketAddress> _addrs;
    }

    InetSocketAddress addr( int num ){
        final int port = START_PORT + num;
        _ensureServer( port );
        return new InetSocketAddress( "localhost" , port );
    }
    
    synchronized void _ensureServer( int port ){
        HttpServer server = _servers.get( port );
        if ( server != null )
            return;
        
        try {
            server = new HttpServer( port );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't open port : " + port , ioe );
        }
        
        MyHandler handler = new MyHandler();

        server.addHandler( handler );
        
        _handlers.put( port , handler );
        _servers.put( port , server );
        
        server.start();
    }

    class MyHandler implements HttpHandler {

        public boolean handles( HttpRequest request , Info info ){
            return true;
        }

        public boolean handle( HttpRequest request , HttpResponse response ){
            return true;
        }

        public double priority(){
            return 0;
        }

    }

    class Count extends TreeMap<Integer,Integer> {

        void inc( InetSocketAddress addr ){
            inc( addr.getPort() - START_PORT );
        }

        void inc( int num ){
            Integer old = get( num );
            put( num , 1 + ( old == null ? 0 : old ) );
        }

        double ratio(){
            if ( size() == 0 )
                throw new RuntimeException( "no data" );
            if ( size() == 1 )
                return 1;
            if ( size() > 2 )
                throw new RuntimeException( "too much data" );
            
            Iterator<Integer> i = keySet().iterator();
            double a = get( i.next() );
            double b = get( i.next() );
            return Math.min(a,b) / (b+a);
        }
    }
    
    final Map<Integer,HttpServer> _servers = new TreeMap<Integer,HttpServer>();
    final Map<Integer,MyHandler> _handlers = new TreeMap<Integer,MyHandler>();

    public static void main( String args[] ){
        (new RouterTest()).runConsole();
    }

}
