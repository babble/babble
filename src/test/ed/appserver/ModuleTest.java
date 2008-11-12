// ModuleTest.java

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
*
*/

package ed.appserver;

import org.testng.annotations.Test;

import ed.*;
import static ed.appserver.Module.*;

public class ModuleTest extends TestCase {
    
    @Test
    public void testVersionParse(){
        assertTrue( parseVersion( "r63" ) > 0 );
        assertTrue( parseVersion( "r63.63" ) > 0 );

        assertTrue( parseVersion( "r63.63.63.63.63.63.63.63.63.63" ) > 0 );
    }

    @Test
    public void testVersionCompare(){
        assertLess( parseVersion( "r0" ) , parseVersion( "r1" ) );
        assertLess( parseVersion( "r63" ) , parseVersion( "r63.63" ) );

        assertLess( parseVersion( "r63.63.63.63.63.63.63.63.63.62" ) , parseVersion( "r63.63.63.63.63.63.63.63.63.63" ) );
        assertLess( parseVersion( "r63.63.63.63.63.63.63.63.63" ) , parseVersion( "r63.63.63.63.63.63.63.63.63.63" ) );

        assertEquals( parseVersion( "r5.5" ) , parseVersion( "version5-5" ) );
        assertEquals( parseVersion( "r5.5_rc4" ) , parseVersion( "version5-5.4" ) );

        assertLess( compareVersions( "r10" , "r20" ) , 0 );
        assertEquals( compareVersions( "r10" , "r10" ) , 0 );
        assertLess( 0 , compareVersions( "r21" , "r10" ) );
    }
    
    public static void main( String args[] ){
        (new ModuleTest()).runConsole();
    }
    
}
