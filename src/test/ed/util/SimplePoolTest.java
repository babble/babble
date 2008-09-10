// SimplePoolTest.java

package ed.util;

public class SimplePoolTest extends ed.TestCase {

    class MyPool extends SimplePool<Integer> {

	MyPool( int maxToKeep , int maxTotal ){
	    super( "blah" , maxToKeep , maxTotal );
	}

	public Integer createNew(){
	    return _num++;
	}

	int _num = 0;
    }
    
    public void testBasic1(){
	MyPool p = new MyPool( 10 , 10 );
	
	int a = p.get();
	assertEquals( 0 , a );
	
	int b = p.get();
	assertEquals( 1 , b );
	
	p.done( a );
	a = p.get();
	assertEquals( 0 , a );
    }

    public void testBasic2(){
	MyPool p = new MyPool( 0 , 0 );
	
	int a = p.get();
	assertEquals( 0 , a );
	
	int b = p.get();
	assertEquals( 1 , b );
	
	p.done( a );
	a = p.get();
	assertEquals( 2 , a );
    }

    public void testMax1(){
	MyPool p = new MyPool( 10 , 2 );
	
	int a = p.get();
	assertEquals( 0 , a );
	
	int b = p.get();
	assertEquals( 1 , b );
	
	assertNull( p.get( 0 ) );
    }

    public void testMax2(){
	MyPool p = new MyPool( 10 , 3 );
	
	int a = p.get();
	assertEquals( 0 , a );
	
	int b = p.get();
	assertEquals( 1 , b );
	
	assertEquals( 2 , (int)p.get( -1 ) );
    }

    public void testMax3(){
	MyPool p = new MyPool( 10 , 3 );
	
	int a = p.get();
	assertEquals( 0 , a );
	
	int b = p.get();
	assertEquals( 1 , b );
	
	assertEquals( 2 , (int)p.get( 1 ) );
    }
    

    public static void main( String args[] ){
	SimplePoolTest t = new SimplePoolTest();
	t.runConsole();
    }
}
