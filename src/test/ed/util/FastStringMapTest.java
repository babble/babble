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

import java.util.*;

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
        assertEquals( 1 , m.size() );

        m.put( "b" , 6 );
        assertEquals( 6 , m.get( "b" ) );
        assertEquals( 5 , m.get( "a" ) );
        assertEquals( 2 , m.keySet().size() );
        assertEquals( 2 , m.size() );        

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
        assertEquals( 1 , m.size() );

        m.remove( "a" );
        assertNull( m.get( "a" ) );
        assertFalse( m.containsKey( "a" ) );
        assertFalse( m.keySet().contains( "a" ) );
        assertEquals( 0 , m.size() );

        m.put( "a" , 5 );
        assertEquals( 5 , m.get( "a" ) );
        assertTrue( m.containsKey( "a" ) );        
        assertFalse( m.containsKey( "b" ) );
        assertEquals( 1 , m.keySet().size() );
        assertTrue( m.keySet().contains( "a" ) );
        assertEquals( 1 , m.size() );
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

    public void testSize1(){
        _testSize( 1000 );
    }

    public void testSize2(){
        _testSize( 10000 );
    }

    public void testSize3(){
        _testSize( 100000 );
    }

    public void testSize4(){
        _testSize( 500000 );
    }

    private void _testSize( int num ){

        System.gc();
        System.gc();
        System.gc();
        long before = MemUtil.bytesAvailable();
        long start = System.currentTimeMillis();
        
        Map<String,Object> m = new HashMap<String,Object>();
        
        for ( int i=0; i<num; i++ )
            m.put( String.valueOf( Math.random() ) , "asdsad" );
        
        long javaTime = System.currentTimeMillis() - start;
        long javaSize = before - MemUtil.bytesAvailable();


        m = new FastStringMap();
        System.gc();
        System.gc();
        System.gc();
        before = MemUtil.bytesAvailable();        
        start = System.currentTimeMillis();
        
        for ( int i=0; i<num; i++ )
            m.put( String.valueOf( Math.random() ) , "asdsad" );
        
        long ourTime = System.currentTimeMillis() - start;
        long ourSize = before - MemUtil.bytesAvailable();

        assertLess( ourSize , javaSize * 1.5 );
        assertLess( ourTime , javaTime * 1.5 );
    }

    @Test(groups = {"basic"})
    public void testPutAll(){
        FastStringMap m = new FastStringMap();
        
        m.put( "a" , 5 );
        assertEquals( 5 , m.get( "a" ) );
        
        Map<String,Object> other = new HashMap<String,Object>();
        other.put( "b" , 6 );
        m.putAll( other );

        assertEquals( 5 , m.get( "a" ) );
        assertEquals( 6 , m.get( "b" ) );
        assertEquals( 2 , m.size() );

    }

    @Test(groups = {"basic"})
    public void testRandom(){
        Random r = new Random( 123123 );
        rand( r , 10 , 10 );
        rand( r , 10 , 100 );
        rand( r , 100 , 1000 );
        rand( r , 100 , 10000 );
        rand( r , 100 , 100000 );
        rand( r , 10000 , 10 );
        rand( r , 1000 , 10000 );
        rand( r , 1000 , 50000 );
    }
    


    void rand( Random r , int space , int time ){

        Map<String,Object> m = new HashMap<String,Object>();
        FastStringMap f = new FastStringMap();

        for ( int i=0; i<time; i++){
            assertEquals( m.size() , f.size() );
            
            String what = String.valueOf( r.nextInt( space ) );
            
            switch( r.nextInt(2) ){
            case 0: 
                assertEquals( m.get( what ) , f.get( what ) );
                //System.out.println( "Adding " + what + "  size before: " + m.size() );
                f.put( what , what );
                m.put( what , what );
                assertEquals( m.get( what ) , f.get( what ) );

                break;
            case 1:
                //System.out.println( "Removing " + what + "  size before: " + m.size() );
                f.remove( what );
                m.remove( what );
                assertEquals( m.get( what ) , f.get( what ) );
                break;
            }

            for ( String s : m.keySet() ){
                assertEquals( m.get( s ) , f.get( s ) );
            }

        }

        assertEquals( m.keySet().size() , f.keySet().size() );

        assertEquals( m.size() , f.size() );


        for ( String s : m.keySet() ){
            assertEquals( m.get( s ) , f.get( s ) );
        }
        
    }


    public static void main( String args[] ){
        (new FastStringMapTest()).runConsole();
    }
    
}
