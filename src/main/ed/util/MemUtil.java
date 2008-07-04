// MemUtil.java

package ed.util;

public class MemUtil {

    public static final long BIT = 1;
    public static final long KBIT = 1024 * BIT;
    public static final long MBIT = 1024 * KBIT;
    public static final long GBIT = 1024 * MBIT;

    public static final long BYTE = 1;
    public static final long KBYTE = 1024 * BYTE;
    public static final long MBYTE = 1024 * KBYTE;
    public static final long GBYTE = 1024 * MBYTE;

    public static final void fullGC(){
        System.gc();
        System.gc();
        System.gc();
    }

    public static final long bytesAvailable(){
        final Runtime r = Runtime.getRuntime();
        return ( r.maxMemory() - r.totalMemory() ) + r.freeMemory();
    }    
    
    
}
