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

    public static void assertFalse( boolean b ){
        if ( b )
            throw new MyAssert( "true" );
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

    public static void assertEquals( String a , Object b ){
	_assertEquals( a , b == null ? null : b.toString() );
    }

    public static void assertEquals( Object a , Object b ){
	_assertEquals( a , b );
    }
    
    public static void _assertEquals( Object a , Object b ){
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
        assertClose(a, b, "");
    }

    public static void assertClose( String a , String b, String tag ){

        if (isClose(a, b)) {
            return;
        }

        throw new MyAssert( tag +  "[" + a + "] != [" + b + "]" );
    }

    public static boolean isClose(String a, String b) { 

        a = a.trim().replaceAll( "\\s+" , " " );
        b = b.trim().replaceAll( "\\s+" , " " );

        return a.equalsIgnoreCase(b);
    }
    
    public static void assertNull( Object foo ){
        if ( foo == null )
            return;
        
        throw new MyAssert( "not null [" + foo + "]" );
    }

    public static void assertNotNull( Object foo ){
        if ( foo != null )
            return;
        
        throw new MyAssert( "null" );
    }

    public static void assertLess( long lower , long higher ){
        if ( lower < higher )
            return;

        throw new MyAssert( lower + " is higher than " + higher );
    }

    public static void assertLess( double lower , double higher ){
        if ( lower < higher )
            return;

        throw new MyAssert( lower + " is higher than " + higher );
    }

}
