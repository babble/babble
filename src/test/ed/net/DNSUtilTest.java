// DNSUtilTest.java

package ed.net;

import org.testng.annotations.Test;

import ed.*;

public class DNSUtilTest extends TestCase {

    @Test(groups = {"basic"})
    public void testTLD(){
        assertEquals( "com" , DNSUtil.getTLD( "shopwiki.com" ) );
        assertEquals( "com" , DNSUtil.getTLD( "www.shopwiki.com" ) );
        assertEquals( "com" , DNSUtil.getTLD( "asd.shopwiki.com" ) );
        assertEquals( "net" , DNSUtil.getTLD( "asd.shopwiki.net" ) );
    }

    @Test(groups = {"basic"})
    public void testDomain(){
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "shopwiki.com" ) );
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "www.shopwiki.com" ) );
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "asd.shopwiki.com" ) );
        assertEquals( "shopwiki.net" , DNSUtil.getDomain( "asd.shopwiki.net" ) );

        assertEquals( "a.a" , DNSUtil.getDomain( "a.a.a.a.a" ) );
    }

    @Test(groups = {"basic"})
    public void testSubdomain(){
        assertEquals( "foo" , DNSUtil.getSubdomain( "foo.shopwiki.com" ) );
        assertEquals( "foo.bar" , DNSUtil.getSubdomain( "foo.bar.shopwiki.com" ) );
        assertEquals( "www" , DNSUtil.getSubdomain( "shopwiki.com" ) );
        assertEquals( "www" , DNSUtil.getSubdomain( "www.shopwiki.com" ) );
        assertEquals( "www" , DNSUtil.getSubdomain( ".shopwiki.com" ) );
    }

    public static void main( String args[] ){
        (new DNSUtilTest()).runConsole();
    }
}
