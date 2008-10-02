// JavaJSObjectTest.java

package ed.js;

import org.testng.annotations.Test;

import ed.*;

public class JavaJSObjectTest extends TestCase {

    static class Foo extends JavaJSObject {
        public int getFoo(){
            return _foo;
        }

        public void setFoo( int i ){
            _foo = i;
        }

        int _foo;
    }

    @Test(groups = {"basic"})
    public void test1(){
        Foo f = new Foo();
        f.setFoo( 5 );

        assertEquals( 5 , f.get( "Foo" ) );
        f.set( "Foo" , 6 );
        assertEquals( 6 , f.get( "Foo" ) );

        assertTrue( f.containsKey( "Foo" ) );
        assertFalse( f.containsKey( "Food" ) );

        System.out.println( f.keySet() );
        assertEquals( 2 , f.keySet().size() );
        assertTrue( f.keySet().contains("Foo") );
        assertTrue( f.keySet().contains("_id") );
    }

    public static void main( String args[] ){
        (new JavaJSObjectTest()).runConsole();
    }
}
