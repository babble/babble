// SeenPathTest.java

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


package ed.util;

import ed.*;

import org.testng.annotations.Test;

public class SeenPathTest extends TestCase {


    public void testSanity1(){
        SeenPath p = new SeenPath();
        
        
        Object from = new Object();
        Object to = new Object();

        assertTrue( p.shouldVisit( to , from ) );
        assertFalse( p.shouldVisit( to , from ) );
        assertEquals( 1 , p.get( to ).size() );

        assertFalse( p.shouldVisit( to , new Object() ) );
        assertEquals( 2 , p.get( to ).size() );

        assertTrue( p.shouldVisit( new Object(), from ) );
    }

    public static void main( String args[] ){
        (new SeenPathTest()).runConsole();
    }
    

}
