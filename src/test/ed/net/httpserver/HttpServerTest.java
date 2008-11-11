// HttpServerTest.java

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

package ed.net.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.testng.annotations.Test;

import ed.*;
import ed.io.*;
import ed.util.*;

public class HttpServerTest extends TestCase {

    public HttpServerTest()
            throws IOException {
        this( 15123 );
    }
    
    public HttpServerTest( int port )
            throws IOException {
        _port = port;
        _server = new HttpServer(_port);
        _server.addHandler(new PingHandler());
        _server.start();
    }

    protected void checkResponse( Response r ){}

    @Test
    public void testBasic1()
            throws IOException {

        Socket s = open();
        s.getOutputStream().write(headers("GET", "", "Connection: Close\r\n").toString().getBytes());
        InputStream in = s.getInputStream();
        Response r = read(in);
        checkResponse( r );
        assertEquals(PingHandler.DATA, r.body);
    }

    @Test
    public void testKeepAlive1()
            throws IOException {

        Socket s = open();

        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();

        out.write(headers("GET", "", "Connection: Keep-Alive\r\n").toString().getBytes());
        Response r = read(in);
        assertEquals(PingHandler.DATA, r.body);

        out.write(headers("GET", "", "Connection: Keep-Alive\r\n").toString().getBytes());
        r = read(in);
        checkResponse( r );
        assertEquals(PingHandler.DATA, r.body);

        out.write(headers("GET", "", "Connection: Keep-Alive\r\n").toString().getBytes());
        r = read(in);
        assertEquals(PingHandler.DATA, r.body);

        out.write(headers("GET", "", "Connection: Keep-Alive\r\n").toString().getBytes());
        r = read(in);
        assertEquals(PingHandler.DATA, r.body);

        out.write(headers("GET", "", "Connection: Keep-Alive\r\n").toString().getBytes());
        r = read(in);
        assertEquals(PingHandler.DATA, r.body);

        out.write(headers("GET", "", "Connection: Close\r\n").toString().getBytes());
        r = read(in);
        assertEquals(PingHandler.DATA, r.body);
        checkResponse( r );

        assert (in.read() == -1);
    }

    @Test
    public void testPipeLine1()
            throws IOException {

        int num = 5;

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < num; i++)
            buf.append(headers("GET", "", "Connection: Keep-Alive\r\n"));

        Socket s = open();
        Response r;

        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();

        out.write(buf.toString().getBytes());
        for (int i = 0; i < num; i++) {
            r = read(in);
            assertEquals(PingHandler.DATA, r.body);
            checkResponse( r );
        }

        out.write(headers("GET", "", "Connection: Close\r\n").toString().getBytes());
        r = read(in);
        assertEquals(PingHandler.DATA, r.body);
        checkResponse( r );
        assert (in.read() == -1);
    }

    @Test
    public void testPost1()
            throws IOException {
        _testPost(100);
    }

    @Test
    public void testPost2()
            throws IOException {
        _testPost(3500);
    }

    @Test
    public void testPost3()
            throws IOException {
        _testPost(50000);
    }

    private void _testPost(int size)
            throws IOException {
        StringBuilder buf = headers("POST", "", "Content-Length: " + size + "\nConnection: Close\n");
        appendRandomData(buf, size);

        Socket s = open();
        s.getOutputStream().write(buf.toString().getBytes());
        InputStream in = s.getInputStream();
        Response r = read(in);
        assertEquals(PingHandler.DATA, r.body);
    }

    protected final Socket open()
            throws IOException {
        Socket s = getSocket();
        s.setSoTimeout( 1000 );
        return s;
    }
    
    protected Socket getSocket()
        throws IOException{
        return new Socket("127.0.0.1", _port);
    }

    public static StringBuilder headers(String method, String params, String headers) {
        StringBuilder buf = new StringBuilder();
        buf.append(method).append(" /~ping?").append(params).append(" HTTP/1.1\r\n");
        buf.append("Host: localhost\r\n");
        buf.append(headers);
        buf.append("\r\n");
        return buf;
    }

    public static Response read(InputStream in)
            throws IOException {

        StringBuilder buf = new StringBuilder();

        int lineCount = 0;
        while (true) {
            int thing = in.read();
            if (thing < 0)
                throw new RuntimeException("ran out of stuff");

            char c = (char) thing;
            if (c == '\r')
                continue;

            if (c != '\n') {
                lineCount++;
                buf.append(c);
                continue;
            }

            if (lineCount == 0)
                break;

            buf.append(c);
            lineCount = 0;
        }

        String firstLine = null;
        Map<String, String> m = new StringMap<String>();

        for (String line : buf.toString().trim().split("\n")) {
            line = line.trim();
            if (line.length() == 0)
                continue;

            if (m.size() == 0) {
                firstLine = line;
                m.put("FIRST_LINE", line);
                continue;
            }

            int idx = line.indexOf(":");
            m.put(line.substring(0, idx).trim(),
                    line.substring(idx + 1).trim());
        }

        byte[] data;
        if (m.get("Content-Length") != null) {
            data = new byte[Integer.parseInt(m.get("Content-Length"))];
            in.read(data);
        } else {
            data = StreamUtil.readBytesFully(in);
        }

        return new Response(firstLine, m, data);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        _server.stopServer();
    }
    
    public static void appendRandomData(StringBuilder buf, int length) {
        for (int i = 0; i < length; i++)
            buf.append("f");
    }

    protected void checkData( int copies , String body ){
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<copies; i++ )
            buf.append( PingHandler.DATA );
        assertEquals( buf.toString() , body );
    }

    public static class Response {

        Response(String fl, Map<String, String> h, byte[] data) {
            firstLine = fl;
            headers = h;
            body = new String(data);
        }

        public String toString() {
            return firstLine + " headers:" + headers + " [" + body + "]";
        }

        public final String firstLine;
        public final String body;
        public final Map<String, String> headers;
    }

    public void testRandom1()
        throws Throwable {
        _testRandom( 10 , 10 , false );
    }

    public void testRandomBigClean()
        throws Throwable {
        if ( _load ){
            _testRandom( 20 , 20 , false );
        }
    }

    public void testRandomBigWithBad()
        throws Throwable {
        if ( _load ){
            _testRandom( 20 , 20 , true );
        }
    }
    
    private void _testRandom( int threads , int seconds , boolean doBad )
        throws Throwable {
    
        final long start = System.currentTimeMillis();
    
        List<MyRandomThread> lst = new ArrayList<MyRandomThread>();
        for ( int i=0; i<threads; i++ )
            lst.add( new MyRandomThread( i , doBad ) );
        
        for ( MyRandomThread t : lst )
            t.start();

        try {
            Thread.sleep( seconds * 1000 );
        }
        catch ( InterruptedException ie ){
        }

        for ( MyRandomThread t : lst )
            t._go = false;

        int total = 0;

        for ( MyRandomThread t : lst ){
            t.join( 5000 );
            if ( t.isAlive() ){
                throw new RuntimeException( "thread not dead" );
            }
            total += t._requests;
            if ( t._ioe != null )
                throw t._ioe;
        }
        
        final long end = System.currentTimeMillis();        
        
        System.out.println( "request/second: " + ( 1000 * total / ( end - start ) ) );
    }
    
    class MyRandomThread extends Thread {

        MyRandomThread( int num , boolean doBad ){
            _rand = new Random( 123123 * num );
            _doBad = doBad;
        }
        
        public void run(){
            
            try {
                while ( _go ){
                    
                    switch ( _rand.nextInt( 6 ) ){
                    case 0:
                        _post(); break;
                    case 1:
                        _drop(); break;
                    case 2:
                    case 3:
                    case 4:
                        _get(); break;
                    case 5:
                        _bad(); break;
                       
                    }
                    
                }
            }
            catch ( Throwable t ){
                _ioe = t;
            }
        }
        
        void _bad()
            throws IOException{
            
            if ( ! _doBad )
                return;

            _what( "b" );

            _check();
            
            final long start = System.currentTimeMillis();
            
            int num = _rand.nextInt( 10 );
            
            _sock.getOutputStream().write(headers("GET", "num=" + num , "Connection: Keep-Alive\r\n" ).toString().getBytes() );
            InputStream in = _sock.getInputStream();
            if ( _rand.nextInt(3) == 0 )
                in.read();
            _sock.close();
            _sock = null;
        }

        void _drop()
            throws IOException {
            
            _what( "d" );

            _check();
            _sock.close();
            _sock = null;
        }
        
        void _get()
            throws IOException{

            _get( _rand.nextDouble() > .5 );
        }
        
        void _get( boolean close )
            throws IOException {
            
            _check();

            _what( "g" );

            final long start = System.currentTimeMillis();
            
            int num = _rand.nextInt( 10 );
            
            _sock.getOutputStream().write(headers("GET", "num=" + num , "Connection: " + ( close ? "Close" : "Keep-Alive" ) + "\r\n" ).toString().getBytes() );
            InputStream in = _sock.getInputStream();
            Response r = read(in);
            checkResponse( r );
            
            final long end = System.currentTimeMillis();
            assertLess( end - start , Math.max( 1000 , num * 1.5 ) );

            assertEquals( num * PingHandler.DATA.length() , r.body.length() );
            //LBFullTest.this.checkData( num , r.body);            
            
            if ( close ){
                _sock.close();
                _sock = null;
            }
            
            _requests++;
        }

        void _post()
            throws IOException {
            _post( _rand.nextDouble() > .5 , _rand.nextInt( 50000 ) );
        }
        
        void _post( boolean close , int size )
            throws IOException {
            
            _what( "p" );
            
            _check();
            
            StringBuilder buf = headers("POST", "", "Content-Length: " + size + "\r\nConnection: " + ( close ? "Close" : "Keep-Alive" ) + "\r\n");
            appendRandomData(buf, size);
            _sock.getOutputStream().write( buf.toString().getBytes() );
    
            InputStream in = _sock.getInputStream();
            Response r = read(in);
            checkResponse( r );
            assertEquals(PingHandler.DATA, r.body);            
            
            if ( close ){
                _sock.close();
                _sock = null;
            }

            _requests++;

        }

        void _check()
            throws IOException {
            if ( _sock == null )
                _sock = open();
        }
        
        void _what( String s ){
            System.out.print( s );
        }
        
        final Random _rand;
        final boolean _doBad;

        boolean _go = true;
        int _requests = 0;
        
        Socket _sock;
        Throwable _ioe;
    }

    HttpServer _server;
    final protected int _port;
    protected boolean _load = false;

    public static void main(String args[])
            throws IOException {
        
        HttpServerTest t = new HttpServerTest();

        if ( Arrays.toString( args ).contains( "load" ) )
            t._load = true;

        t.runConsole();
    }
}
