// MemUtil.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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

    public static final synchronized void checkMemoryAndHalt( String location , OutOfMemoryError oom  ){

        long before = MemUtil.bytesAvailable();
        System.gc();
        System.gc();
        System.gc();
        long after = MemUtil.bytesAvailable();

        if ( after < ( 100 * MemUtil.MBYTE ) ||
             ( after - before ) < ( 100 * MemUtil.MBYTE ) ){

            try {
                System.err.print( "OutOfMemoryError in " + location + " - not enough free, so dying." );
                System.err.println( "before : " + ( before / MemUtil.MBYTE ) );
                System.err.println( "after : " + ( after / MemUtil.MBYTE ) );
                ed.lang.StackTraceHolder.getInstance().fix( oom );
                oom.printStackTrace();
            }
            catch ( Exception e ){
            }

            Runtime.getRuntime().halt( -3 );
        }


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

    public static String getMemInfo( boolean gc ){
        if ( gc )
            System.gc();

        Runtime r = Runtime.getRuntime();

        StringBuilder buf = new StringBuilder();
        buf.append( "max   : " ).append( MemUtil.bytesToMB( r.maxMemory() ) ).append( "\n" );
        buf.append( "total : " ).append( MemUtil.bytesToMB( r.totalMemory() ) ).append( "\n" );
        buf.append( "free  : " ).append( MemUtil.bytesToMB( r.freeMemory() ) ).append( "\n" );
        return buf.toString();
    }

    public static String gc() {
        long before = MemUtil.bytesAvailable();
        System.gc();
        long after = MemUtil.bytesAvailable();
        return "before "+before+", after "+after+", break "+Runtime.getRuntime().totalMemory();
    }

}
