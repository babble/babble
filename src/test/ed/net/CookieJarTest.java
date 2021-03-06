package ed.net;

import static org.testng.AssertJUnit.*;

import java.net.*;
import java.util.List;

import javax.servlet.http.Cookie;

import org.testng.annotations.Test;


public class CookieJarTest {
    public CookieJarTest() {
    }
    
    @Test
    public void testSimple() throws MalformedURLException {
        CookieJar jar = new CookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( "10gen.com" );
        cookie.setPath( "/" );

        jar.addCookie( new URL( "http://10gen.com/" ), cookie );

        //make sure it was actually saved
        assertSame( cookie, jar.getAll().get( "myname" ) );

        //make sure it would resent to the same url
        assertSame( cookie, jar.getActiveCookies( new URL("http://10gen.com/") ).get( "myname" ) );
        
        //make sure it won't be sent elsewhere
        URL otherUrl = new URL( "http://someotherhost.com/with/random/path");
        assertEquals( 0, jar.getActiveCookies( otherUrl ).size() );       
    }
    
    @Test
    public void testSecure() throws MalformedURLException {
        CookieJar jar = new CookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/" );
        cookie.setSecure( true );
        
        jar.addCookie( new URL("https://www.10gen.com") , cookie );
        
        assertSame( cookie, jar.getActiveCookies( new URL( "https://10gen.com/" ) ).get("myname") );
        assertSame( 0, jar.getActiveCookies( new URL( "http://10gen.com/" ) ).size() );
    }
    
    @Test
    public void testPath() throws MalformedURLException {
        CookieJar jar = new CookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/subdir" );
        
        jar.addCookie( new URL( "http://www.10gen.com/subdir" ), cookie );
        
        assertSame( cookie, jar.getActiveCookies( new URL( "http://10gen.com/subdir/moo/baa.html" ) ).get("myname") );
        assertSame( 0, jar.getActiveCookies( new URL( "http://10gen.com/otherdir/" ) ).size() );
    }
    
    @Test
    public void testInvalidDomain() throws MalformedURLException {
        CookieJar jar = new CookieJar();
        Cookie cookie = new Cookie( "myname", "myvalue" );
        cookie.setDomain( ".10gen.com" );
        cookie.setPath( "/subdir" );
        
        jar.addCookie( new URL( "http://othersite.com/" ) , cookie );
        
        assertSame( 0, jar.getAll().size() );
    }
    
    @Test
    public void testClean() throws MalformedURLException, InterruptedException {
        CookieJar jar = new CookieJar();

        //Normal cookie
        Cookie normalCookie = new Cookie( "normal", "mynorm");
        normalCookie.setDomain( ".10gen.com" );
        normalCookie.setPath( "/" );
        normalCookie.setMaxAge( 9999 );
        
        jar.addCookie( new URL( "http://www.10gen.com/" ) , normalCookie );
        
        //Expired Cookie
        Cookie expiredCookie = new Cookie( "expired", "myvalue" );
        expiredCookie.setDomain( ".10gen.com" );
        expiredCookie.setPath( "/" );
        expiredCookie.setMaxAge( 1 );
        
        jar.addCookie( new URL( "http://www.10gen.com/" ) , expiredCookie );
        
        
        //Nonpersistent cookie
        Cookie nonpresistCookie = new Cookie( "nonpersist", "myval2" );
        nonpresistCookie.setDomain( ".10gen.com" );
        nonpresistCookie.setPath( "/" );
        
        jar.addCookie( new URL( "http://www.10gen.com/" ) , nonpresistCookie );
        
        
        assertEquals( 3 , jar.getAll().size() );
        
        Thread.sleep( 2000 );
        
        List<Cookie> removedCookies = jar.clean(false);
        assertEquals( 1 , removedCookies.size() );
        assertEquals( "expired" , removedCookies.get( 0 ).getName() );
        
        
        removedCookies = jar.clean(true);
        assertEquals( 1 , removedCookies.size() );
        assertEquals( "nonpersist" , removedCookies.get( 0 ).getName() );
    }
}
