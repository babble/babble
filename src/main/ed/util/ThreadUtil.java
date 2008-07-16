// ThreadUtil.java

package ed.util;

/** @expose */
public class ThreadUtil {

    /** Creates an prints a stack trace */
    public static void printStackTrace(){
        Exception e = new Exception();
        e.fillInStackTrace();
        e.printStackTrace();
    }

    /** Pauses for a given number of milliseconds
     * @param time number of milliseconds for which to pause
     */
    public static void sleep( long time ){
        try {
            Thread.sleep( time );
        }
        catch ( InterruptedException e ){
        }
    }
}
