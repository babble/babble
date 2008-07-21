// ReplayTest.java

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

import org.testng.annotations.Test;

import ed.*;

public class ReplayTest extends TestCase {

    @Test(groups = {"basic"})
    public void testHeaders(){
        Replay r = new Replay( "foo" , 0 , "www.shopwiki.com" );
        
        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\nX-Replay: y\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\nHost: asdasd\n\n" ) );

        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\nA: B\nX-Replay: y\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\nHost: asdasd\nA: B\n\n" ) );

        assertEquals( "GET /\nConnection: close\nHost: www.shopwiki.com\nX-Replay: y\n\n" ,
                      r.fixHeaders( "GET /\nConnection: asd\n\n" ) );
    }

    public static void main( String args[] ){
        (new ReplayTest()).runConsole();
    }
}
