// NativeHelp1.java

package ed.js;

public class NativeHelp1 {

    public static int count( NativeHelp1[] arr ){
        return arr.length;
    }

    public static int sum( int[] all ){
        int total = 0;
        for ( int i=0; i<all.length; i++ )
            total += all[i];
        return total;
    }

    public static int count2( Object ... foo ){
	if ( foo == null )
	    return 0;
        return foo.length;
    }
    
    public static Object varArgWhich( int which , Object ... args ){
        return args[which];
    }

    public static Number num1( int a ){
        return a;
    }

    public static Number num2( Integer a ){
        return a;
    }

    public static Number num3( Number a ){
        return a;
    }

}
