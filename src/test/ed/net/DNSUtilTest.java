// DNSUtilTest.java

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

import org.testng.annotations.Test;

import ed.*;

public class DNSUtilTest extends TestCase {

    @Test(groups = {"basic"})
    public void testTLD(){
        assertEquals( "com" , DNSUtil.getTLD( "shopwiki.com" ) );
        assertEquals( "com" , DNSUtil.getTLD( "www.shopwiki.com" ) );
        assertEquals( "com" , DNSUtil.getTLD( "asd.shopwiki.com" ) );
        assertEquals( "net" , DNSUtil.getTLD( "asd.shopwiki.net" ) );
        assertEquals( "co.uk" , DNSUtil.getTLD( "asd.shopwiki.co.uk" ) );
        assertEquals( "edu" , DNSUtil.getTLD( "asd.shopwiki.edu" ) );
        assertEquals( "" , DNSUtil.getTLD( "localhost" ) );
        assertEquals( "" , DNSUtil.getTLD( "foo.bar" ) );
    }
    
    @Test(groups = {"basic"})
    public void testDomain(){
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "shopwiki.com" ) );
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "www.shopwiki.com" ) );
        assertEquals( "shopwiki.com" , DNSUtil.getDomain( "asd.shopwiki.com" ) );
        assertEquals( "shopwiki.net" , DNSUtil.getDomain( "asd.shopwiki.net" ) );

        assertEquals( "foo.co.uk" , DNSUtil.getDomain( "foo.foo.co.uk" ) );

        assertEquals( "localhost" , DNSUtil.getDomain( "localhost" ) );
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
