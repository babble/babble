// RollingCounterTest.java

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

import org.testng.annotations.Test;

public class RollingCounterTest extends ed.TestCase {

    @Test
    public void test1(){
        RollingCounter c = new RollingCounter( 10000 , 10 );
        
        c.hit( "eliot" );
        assertEquals( 1 , c.get( "eliot" , 0 ) );

        c.hit( "eliot" );
        assertEquals( 2 , c.get( "eliot" , 0 ) );
    }
    
    @Test
    public void test2(){
        RollingCounter c = new RollingCounter( 10000 , 20 );
        c.hit( "a" , 5 );
        c.hit( "b" , 1 );

        assertEquals( "[a, b]" , c.sorted( 0 ).toString() );

        c.hit( "c" , 10 );
        assertEquals( "[c, a, b]" , c.sorted( 0 ).toString() );

        c.hit( "b" , 11 );
        assertEquals( "[b, c, a]" , c.sorted( 0 ).toString() );
        
    }

    public static void main( String args[] ){
        (new RollingCounterTest()).runConsole();
    }
}
