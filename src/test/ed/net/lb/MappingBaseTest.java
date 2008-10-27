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

    void assertEquals( Environment e , String site , String env , String host ){
        assertEquals( site , e.site );
        assertEquals( env , e.env );
        assertEquals( host , e.host );
    }
    
    @Test(groups = {"basic"})
    public void testEnv(){
        Environment e = new Environment( "a" , "b" , "c" );
        assertEquals( "c" , e.replaceHeaderValue( "host" , null ) );
        assertEquals( "c" , e.replaceHeaderValue( "host" , "c" ) );
        assertEquals( "c:8080" , e.replaceHeaderValue( "host" , "c:8080" ) );
    }
    
    TextMapping create( String content )
        throws IOException {
        return new TextMapping( new LineReader( new ByteArrayInputStream( content.getBytes() ) ) );
    }

    public static void main( String args[] ){
        (new MappingBaseTest()).runConsole();
    }
}
