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
}
