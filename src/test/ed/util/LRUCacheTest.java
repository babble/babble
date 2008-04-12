// LRUCacheTest.java

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
