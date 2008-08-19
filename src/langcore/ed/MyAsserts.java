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
	assertTrue( b , "false" );
    }

    public static void assertTrue( boolean b , String msg ){
        if ( ! b )
            throw new MyAssert( msg );
    }

    public static void assertFalse( boolean b ){
	assertFalse( b , "true" );
    }

    public static void assertFalse( boolean b , String msg ){
        if ( b )
            throw new MyAssert( msg );
    }

    public static void assertEquals( int a , int b ){
	assertEquals( a , b , "" + a + " != " + b );
    }

    public static void assertEquals( int a , int b , String msg ){
        if ( a != b )
            throw new MyAssert( msg );
    }

    public static void assertEquals( long a , long b ){
	assertEquals( a , b , "" + a + " != " + b );
    }

    public static void assertEquals( long a , long b , String msg ){
        if ( a != b )
            throw new MyAssert( msg );
    }

    public static void assertEquals( char a , char b ){
	assertEquals( a , b , "" + a + " != " + b );
    }

    public static void assertEquals( char a , char b , String msg ){
        if ( a != b )
            throw new MyAssert( msg );
    }

    public static void assertEquals( short a , short b ){
	assertEquals( a , b , "" + a + " != " + b );
    }

    public static void assertEquals( short a , short b , String msg ){
        if ( a != b )
            throw new MyAssert( msg );
    }

    public static void assertEquals( double a , double b , double diff ){
	assertEquals( a , b , "" + a + " != " + b );
    }

    public static void assertEquals( double a , double b , double diff , String msg ){
        if ( Math.abs( a - b ) > diff )
            throw new MyAssert( msg );
    }

    public static void assertEquals( Object a , Object b ){
	assertEquals( a , b , "[" + a + "] != [" + b + "]" );
    }

    public static void assertEquals( Object a , Object b , String msg ){
        if ( a == null ){
            if ( b == null )
                return;
            throw new MyAssert( msg + (msg != null && msg.length() > 0 ? ": " : "") + "left null, right not" );
        }

        if ( a.equals( b ) )
            return;

        throw new MyAssert( msg );
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
	assertNull( foo , "not null [" + foo + "]" );
    }

    public static void assertNull( Object foo , String msg ){
        if ( foo == null )
            return;

        throw new MyAssert( msg );
    }

    public static void assertNotNull( Object foo ){
	assertNotNull( foo , "null" );
    }

    public static void assertNotNull( Object foo , String msg ){
        if ( foo != null )
            return;

        throw new MyAssert( msg );
    }

    public static void assertLess( long lower , long higher ){
	assertLess( lower , higher , lower + " is higher than " + higher );
    }

    public static void assertLess( long lower , long higher , String msg ){
        if ( lower < higher )
            return;

        throw new MyAssert( msg );
    }

    public static void assertLess( double lower , double higher ){
	assertLess( lower , higher , lower + " is higher than " + higher );
    }

    public static void assertLess( double lower , double higher , String msg ){
        if ( lower < higher )
            return;

        throw new MyAssert( msg );
    }

    public static void fail( String msg ){
	throw new MyAssert( msg == null ? "fail" : msg );
    }
}
