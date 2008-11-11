package ed.js;

import static org.testng.AssertJUnit.*;

import java.net.*;

import javax.servlet.http.Cookie;

import org.testng.annotations.Test;

public class JSCookieJarTest {
    public JSCookieJarTest() {
    }
    
    @Test
    public void testSimple() throws MalformedURLException {
        JSCookieJar jar = new JSCookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( "10gen.com" );
        cookie.setPath( "/" );

        jar.addCookie( new URL( "http://10gen.com/" ), cookie );

        //make sure it was actually saved
        assertSame( cookie, jar.get( "myname" ) );

        //make sure it would resent to the same url
        assertSame( cookie, jar.getActiveCookies( new URL("http://10gen.com/") ).get( 0 ) );
        
        //make sure it won't be sent elsewhere
        URL otherUrl = new URL( "http://someotherhost.com/with/random/path");
        assertEquals( 0, jar.getActiveCookies( otherUrl ).size() );       
    }
    
    @Test
    public void testSecure() throws MalformedURLException {
        JSCookieJar jar = new JSCookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/" );
        cookie.setSecure( true );
        
        jar.addCookie( new URL("https://www.10gen.com") , cookie );
        
        assertSame( cookie, jar.getActiveCookies( new URL( "https://10gen.com/" ) ).get(0) );
        assertSame( 0, jar.getActiveCookies( new URL( "http://10gen.com/" ) ).size() );
    }
    
    @Test
    public void testPath() throws MalformedURLException {
        JSCookieJar jar = new JSCookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/subdir" );
        
        jar.addCookie( new URL( "http://www.10gen.com/subdir" ), cookie );
        
        assertSame( cookie, jar.getActiveCookies( new URL( "http://10gen.com/subdir/moo/baa.html" ) ).get(0) );
        assertSame( 0, jar.getActiveCookies( new URL( "http://10gen.com/otherdir/" ) ).size() );
    }
    
    @Test
    public void testInvalidDomain() throws MalformedURLException {
        JSCookieJar jar = new JSCookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/subdir" );
        
        jar.addCookie( new URL( "http://othersite.com/" ) , cookie );
        
        assertSame( 0, jar.keySet().size() );
    }
}
