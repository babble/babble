// LRUCacheTest.java

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

public class LRUCacheTest extends ed.TestCase {

    @Test(groups = {"basic"})
    public void testBasic()
        throws Exception {
        
        LRUCache<String,String> c = new LRUCache<String,String>( 10 );
        c.put( "a" , "b" );
        assertEquals( "b" , c.get( "a" ) );
        Thread.sleep( 5 );
        assertEquals( "b" , c.get( "a" ) );
        Thread.sleep( 6 );
        assertNull( c.get( "a" ) );


        c.put( "a" , "c" , 1 );
        assertEquals( "c" , c.get( "a" ) );
        Thread.sleep( 2 );
        assertNull( c.get( "a" ) );
    }

    public void testRemove(){
        LRUCache<String,String> c = new LRUCache<String,String>( 10 , 2 );
        c.put( "a" , "a" );
        c.put( "b" , "a" );
        assertEquals( 2 , c.size() );
        c.put( "c" , "a" );
        assertEquals( 2 , c.size() );
        assertNull( c.get( "a" ) );
    }
    
    public static void main( String args[] ){
        (new LRUCacheTest()).runConsole();
    }
    
}
