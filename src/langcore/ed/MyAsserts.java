// MyAsserts.java 

package ed;

public class MyAsserts {

    public static class MyAssert extends RuntimeException {
        MyAssert( String s ){
            super( s );
            _s = s;
        }

        public String toString(){
            return _s;
        }

        final String _s;
    }

    public static void assertEquals( int a , int b ){
        if ( a != b )
            throw new MyAssert( "" + a + " != " + b );
    }

    

}
