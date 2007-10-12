// ThreadUtil.java

package ed.util;

public class ThreadUtil {

    public static void sleep( long time ){
        try {
            Thread.sleep( time );
        }
        catch ( InterruptedException e ){
        }
    }
}
