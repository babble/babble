// ThreadUtil.java

package ed.util;

public class ThreadUtil {

    public static void printStackTrace(){
        Exception e = new Exception();
        e.fillInStackTrace();
        e.printStackTrace();
    }

    public static void sleep( long time ){
        try {
            Thread.sleep( time );
        }
        catch ( InterruptedException e ){
        }
    }
}
