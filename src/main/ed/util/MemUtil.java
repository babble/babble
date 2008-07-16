// MemUtil.java

package ed.util;

/** @expose */
public class MemUtil {

    /** Value is 1 */
    public static final long BIT = 1;
    /** Value is 1024 */
    public static final long KBIT = 1024 * BIT;
    /** Value is 1024^2 */
    public static final long MBIT = 1024 * KBIT;
    /** Value is 1024^3 */
    public static final long GBIT = 1024 * MBIT;

    /** Value is 1 */
    public static final long BYTE = 1;
    /** Value is 1024 */
    public static final long KBYTE = 1024 * BYTE;
    /** Value is 1024^2 */
    public static final long MBYTE = 1024 * KBYTE;
    /** Value is 1024^3 */
    public static final long GBYTE = 1024 * MBYTE;

    /** Collects all garbage. */
    public static final void fullGC(){
        System.gc();
        System.gc();
        System.gc();
    }

    /** Converts a given number of bytes to megabytes.
     * @param bytes the number of bytes
     * @return Equivalent number of megabytes
     */
    public static final int bytesToMB( long bytes ){
        bytes = bytes / MBYTE;
        return (int)bytes;
    }

    /** Number of bytes free.
     * @return the number of bytes available
     */
    public static final long bytesAvailable(){
        final Runtime r = Runtime.getRuntime();
        return ( r.maxMemory() - r.totalMemory() ) + r.freeMemory();
    }


}
