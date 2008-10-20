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
            "   n2\n";

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
    }
    
    TextMapping create( String content )
        throws IOException {
        return new TextMapping( new LineReader( new ByteArrayInputStream( content.getBytes() ) ) );
    }

    public static void main( String args[] ){
        (new MappingBaseTest()).runConsole();
    }
}
