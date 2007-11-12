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

    public static void assertTrue( boolean b ){
        if ( ! b )
            throw new MyAssert( "false" );
    }

    public static void assertEquals( int a , int b ){
        if ( a != b )
            throw new MyAssert( "" + a + " != " + b );
    }

    public static void assertEquals( long a , long b ){
        if ( a != b )
            throw new MyAssert( "" + a + " != " + b );
    }

    public static void assertEquals( char a , char b ){
        if ( a != b )
            throw new MyAssert( "" + a + " != " + b );
    }
    
    public static void assertEquals( short a , short b ){
        if ( a != b )
            throw new MyAssert( "" + a + " != " + b );
    }
    
    public static void assertEquals( double a , double b , double diff ){
        if ( Math.abs( a - b ) > diff )
            throw new MyAssert( "" + a + " != " + b );
    }

    public static void assertEquals( Object a , Object b ){
        if ( a == null ){
            if ( b == null )
                return;
            throw new MyAssert( "left null, right not" );
        }
        
        if ( a.equals( b ) )
            return;
        
        throw new MyAssert( "[" + a + "] != [" + b + "] " );
    }

    public static void assertClose( String a , String b ){

        a = a.trim().replaceAll( "\\s+" , " " );
        b = b.trim().replaceAll( "\\s+" , " " );

        if ( a.equalsIgnoreCase( b ) )
            return;
        
        throw new MyAssert( "[" + a + "] != [" + b + "]" );
    }

}
