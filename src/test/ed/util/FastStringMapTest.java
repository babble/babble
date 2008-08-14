// FastStringMapTest.java

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

public class FastStringMapTest extends ed.TestCase {

    @Test(groups = {"basic"})
    public void testBasic(){
        
        FastStringMap m = new FastStringMap();
        m.put( "a" , 5 );
        assertEquals( 5 , m.get( "a" ) );
        assertTrue( m.containsKey( "a" ) );        
        assertFalse( m.containsKey( "b" ) );
        assertEquals( 1 , m.keySet().size() );
        assertTrue( m.keySet().contains( "a" ) );

        m.put( "b" , 6 );
        assertEquals( 6 , m.get( "b" ) );
        assertEquals( 5 , m.get( "a" ) );
        assertEquals( 2 , m.keySet().size() );
        
        assertTrue( m.containsKey( "a" ) );
        assertTrue( m.containsKey( "b" ) );
        assertFalse( m.containsKey( "c" ) );

        assertEquals( 6 , m.put( "b" , 7 ) );
        assertEquals( 7 , m.get( "b" ) );
        assertEquals( 2 , m.keySet().size() );
        assertTrue( m.keySet().contains( "a" ) );
        assertTrue( m.keySet().contains( "b" ) );
        assertFalse( m.keySet().contains( "c" ) );
    }

    
    @Test(groups = {"basic"})
    public void testRemove(){
        FastStringMap m = new FastStringMap();
        m.put( "a" , 5 );
        assertEquals( 5 , m.get( "a" ) );
        assertTrue( m.containsKey( "a" ) );        
        assertFalse( m.containsKey( "b" ) );
        assertEquals( 1 , m.keySet().size() );
        assertTrue( m.keySet().contains( "a" ) );

        m.remove( "a" );
        assertNull( m.get( "a" ) );
        assertFalse( m.containsKey( "a" ) );
        assertFalse( m.keySet().contains( "a" ) );

        m.put( "a" , 5 );
        assertEquals( 5 , m.get( "a" ) );
        assertTrue( m.containsKey( "a" ) );        
        assertFalse( m.containsKey( "b" ) );
        assertEquals( 1 , m.keySet().size() );
        assertTrue( m.keySet().contains( "a" ) );
    }

    @Test(groups = {"basic"})
    public void testMedSize(){
        FastStringMap m = new FastStringMap();
        
        for ( int i=0; i<100; i++ )
            m.put( String.valueOf( i ) , String.valueOf( i + 1000 ) );
        
                
        for ( int i=0; i<100; i++ ){
            assertEquals( String.valueOf( i + 1000 ) , m.get( String.valueOf( i ) ) );
            assertTrue( m.containsKey( String.valueOf( i ) ) );
            assertTrue( m.keySet().contains( String.valueOf( i ) ) );
        }
        
        
    }


    public static void main( String args[] ){
        (new FastStringMapTest()).runConsole();
    }
    
}
