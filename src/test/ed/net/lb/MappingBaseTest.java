// MappingBaseTest.java

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

import org.testng.annotations.Test;

import ed.*;
import ed.io.*;
import ed.net.httpserver.*;

public class MappingBaseTest extends TestCase {

    @Test(groups = {"basic"})
    public void testBasic1()
        throws IOException {
        
        String s = "site a\n" + 
            "  dev : prod2\n" + 
            "  www : prod1\n" + 
            "pool prod1\n" + 
            "   n1\n" + 
            "pool prod2\n" + 
            "   n2\n" +
            "pool prod3\n" + 
            "   n3:80\n";

        TextMapping tm = create( s );
        assertClose( s , tm.toFileConfig() );
        
        assertEquals( "n1" , tm.getAddressesForPool( "prod1" ).get( 0 ).getHostName() );
        assertEquals( "n2" , tm.getAddressesForPool( "prod2" ).get( 0 ).getHostName() );
    }
    
    @Test(groups = {"basic"})
    public void testBasic2()
        throws IOException {
        
        String s = "site a\n" + 
            "  dev : prod2\n" + 
            "  www : prod1\n" + 
            "pool prod1\n" + 
            "   n1\n" + 
            "pool prod2\n" + 
            "   n2\n" + 
            "block ip 1.2.3.4\n" + 
            "block url www.alleyinsider.com/blah"
            ;
        
        TextMapping tm = create( s );
        assertClose( s , tm.toFileConfig() );
        
        assert( tm.rejectIp( "1.2.3.4" ) );
        assert( ! tm.rejectIp( "1.2.3.5" ) );

        assert( tm.rejectUrl( "www.alleyinsider.com/blah" ) );
        assert( ! tm.rejectUrl( "www.alleyinsider.com/bla" ) );
        assert( ! tm.rejectUrl( "www.alleyinside.com/blah" ) );

        assertEquals( "prod1" , tm.getPool( new Environment( "a" , "www" ) ) );
        assertEquals( "prod2" , tm.getPool( new Environment( "a" , "dev" ) ) );
    }

    @Test(groups = {"basic"})
    public void testBasic3()
        throws IOException {
        
        String s = 
            "site a\n" + 
            "  dev : prod2\n" + 
            "  www : prod1\n" +
            "site-alias a\n" + 
            "  bar : dev\n" + 
            "  foo : www\n" + 
            "pool prod1\n" + 
            "   n1\n" + 
            "pool prod2\n" + 
            "   n2\n" + 
            "default pool prod1\n" + 
            "block ip 1.2.3.4\n" + 
            "block url www.alleyinsider.com/blah"
            ;
        
        TextMapping tm = create( s );
        assertClose( s , tm.toFileConfig() );
        
        assert( tm.rejectIp( "1.2.3.4" ) );
        assert( ! tm.rejectIp( "1.2.3.5" ) );

        assertEquals( "prod1" , tm.getPool( new Environment( "a" , "www" ) ) );
        assertEquals( "prod2" , tm.getPool( new Environment( "a" , "dev" ) ) );
        
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.a.com" ) ) , "a" , "www" , "www.a.com" );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: dev.a.com" ) ) , "a" , "dev" , "dev.a.com" );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: foo.a.com" ) ) , "a" , "www" , "www.a.com" );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: bar.a.com" ) ) , "a" , "dev" , "dev.a.com" );
        
        assertEquals( "prod1" , tm.getPool( HttpRequest.getDummy( "/" , "Host: foo.a.com" ) ) );
        assertEquals( "prod2" , tm.getPool( HttpRequest.getDummy( "/" , "Host: bar.a.com" ) ) );
        assertEquals( "prod1" , tm.getPool( HttpRequest.getDummy( "/" , "Host: www.a.com" ) ) );
        assertEquals( "prod2" , tm.getPool( HttpRequest.getDummy( "/" , "Host: dev.a.com" ) ) );

    }

    @Test(groups = {"basic"})
    public void testAlias()
        throws IOException {
        String s = 
            "site foo\n" + 
            "   www : prod1\n" + 
            "   dev : prod2\n" + 

            "site-alias foo\n" + 
            "   real : www\n" +
            "   play : dev\n" + 
            "   me.play : dev\n" + 

            "pool prod1\n" + 
            "   n1\n" + 
            "pool prod2\n" + 
            "   n2\n";       
        
        TextMapping tm = create( s );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: real.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: real.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: dev.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: play.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: me.play.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );

    }


    @Test(groups = {"basic"})
    public void testAliasWildCard()
        throws IOException {
        String s = 
            "site foo\n" + 
            "   www : prod1\n" + 
            "   dev : prod2\n" + 

            "site-alias foo\n" + 
            "   real : www\n" +
            "   * : dev\n" +
            "   play : dev\n" + 
            "   me.play : dev\n" + 

            "pool prod1\n" + 
            "   n1\n" + 
            "pool prod2\n" + 
            "   n2\n";       
        
        TextMapping tm = create( s );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: real.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: real.foo.com" ) ) , "foo" , "www" , "www.foo.com" , "www.foo" + Environment._internalDomain );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: dev.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: play.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: me.play.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: measdjasd.play.foo.com" ) ) , "foo" , "dev" , "dev.foo.com" , "dev.foo" + Environment._internalDomain );

    }

    @Test(groups = {"basic"})
    public void testAlias2()
        throws IOException {

        String s = 
            
            "site alleyinsider\n" +
            "    backup : backup1\n" +
            "    dev : test1\n" +
            "    stage : stage1\n" + 
            "    test : test1\n" +
            "    www : prod1\n" +
            "    x : dev1\n" +

            "site-alias alleyinsider\n" + 
            "    businesssheet : www\n" +
            "    businesssheet.stage : stage\n" +
            "    businesssheet.test : test\n" +
            "    clusterstock : www\n" +
            "    clusterstock.stage : stage\n" +
            "    clusterstock.test : test\n" + 
            "    www.test : test\n" +

            "pool backup1\n" + 
            "   sat-sb-n1\n" + 
            "pool dev1\n" + 
            "   iad-sb-n1\n" + 
            "pool prod1\n" + 
            "   iad-sb-n4\n" +
            "   iad-sb-n5\n" +
            "pool prod2\n" +
            "   iad-sb-n9\n" +
            "pool stage1\n" +
            "   iad-sb-n4\n" +
            "pool test1\n" +
            "   iad-sb-n7\n";


        
        TextMapping tm = create( s );

        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: www.alleyinsider.com" ) ) , "alleyinsider" , "www" , "www.alleyinsider.com" , "www.alleyinsider" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: businesssheet.alleyinsider.com" ) ) , "alleyinsider" , "www" , "www.alleyinsider.com" , "www.alleyinsider" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/" , "Host: clusterstock.alleyinsider.com" ) ) , "alleyinsider" , "www" , "www.alleyinsider.com" , "www.alleyinsider" + Environment._internalDomain );
        
        // TODO: should useHost for these 2, be env.name ??
        //       or should it rewrite the url
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/www.alleyinsider.com/foo.jpg" , "Host: origin.10gen.com" ) ) , "alleyinsider" , "www" , "origin.10gen.com" , "www.alleyinsider" + Environment._internalDomain );
        assertEquals( tm.getEnvironment( HttpRequest.getDummy( "/clusterstock.alleyinsider.com/foo.jpg" , "Host: origin.10gen.com" ) ) , "alleyinsider" , "www" , "origin.10gen.com" , "www.alleyinsider" + Environment._internalDomain );
    }

    void assertEquals( Environment e , String site , String env , String host , String useHost ){
        assertEquals( e , site , env , host );
        assertEquals( e.getExtraHeaderString().trim() , "X-Host: " + useHost );
    }

    void assertEquals( Environment e , String site , String env , String host ){
        assertEquals( site , e.site , "site wrong" );
        assertEquals( env , e.env , "environment wrong" );
        assertEquals( host , e.host , "host wrong" );
    }
    
    TextMapping create( String content )
        throws IOException {
        return new TextMapping( new LineReader( new ByteArrayInputStream( content.getBytes() ) ) );
    }

    public static void main( String args[] ){
        (new MappingBaseTest()).runConsole();
    }
}
