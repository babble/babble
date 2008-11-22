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

import java.util.regex.*;

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

    public static interface MemHaltDisplay {
        public void printMemInfo();
    }

    public static final synchronized void checkMemoryAndHalt( String location , OutOfMemoryError oom , MemHaltDisplay display ){

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
                if ( display != null )
                    display.printMemInfo();
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
	buf.append( "used  : " ).append( MemUtil.bytesToMB( r.totalMemory() - r.freeMemory() ) ).append( "\n" );
        return buf.toString();
    }

    public static String gc() {
        long before = MemUtil.bytesAvailable();
        System.gc();
        long after = MemUtil.bytesAvailable();
        return "before "+before+", after "+after+", break "+Runtime.getRuntime().totalMemory();
    }

    public static class GCLine {
        
        private GCLine( String line ){
            _line = line;
            _full = line.indexOf( "[Full GC " ) >= 0;

            if ( Character.isDigit( line.charAt( 0 ) ) ){
                StringBuilder num = new StringBuilder();
                while ( true ){
                    char c = line.charAt(0);
                    line = line.substring(1);
                    if ( c == ':' || c == ' ' )
                        break;
                    num.append( c );
                }
                
                _when = (long)(Double.parseDouble( num.toString() ) * 1000);
                line = line.trim();
            }
            else {
                _when = -1;
            }

            String howLongString = null;

            for ( int i=0; i< _howLongPatterns.length; i++ ){
                Matcher m = _howLongPatterns[i].matcher( line );
                if ( ! m.find() )
                    continue;
                howLongString = m.group(1);
                break;
            }

            if ( howLongString == null )
                _howLong = -1;
            else
                _howLong = (long)(Double.parseDouble( howLongString ) * 1000);
            
        }

        public GCLine( long when , boolean full , long howLong ){
            _line = null;
            _when = when;
            _full = full;
            _howLong = howLong;
        }

        public static boolean isGCLine( String line ){
            int idx = line.indexOf( "[GC " );
            if ( idx < 0 )
                idx = line.indexOf("[Full GC " );
            
            if ( idx < 0 || idx > 50 )
                return false;
            
            return true;
        }

        public static GCLine parse( String line ){
            if ( ! isGCLine( line ) )
                return null;
            
            return new GCLine( line );
        }
        
        public long when(){
            return _when;
        }

        public long howLong(){
            return _howLong;
        }

        public boolean full(){
            return _full;
        }
        
        final String _line;
        final long _when;
        final boolean _full;
        final long _howLong;

        static final Pattern[] _howLongPatterns = new Pattern[]{
            Pattern.compile( "real=(\\d+\\.\\d+)\\s*secs" ) ,
            Pattern.compile( "(\\d+\\.\\d+)\\s*secs" ) ,
        };
    }
    
    public static class GCStream {
    
        /**
         * @return true if line is a real gc line
         */
        public boolean add( String line ){
            GCLine l = GCLine.parse( line );
            if ( l == null )
                return false;
            
            add( l );
            return true;
        }

        public void reset(){
            _last.clear();
        }

        public void add( GCLine line ){
            _last.add( line );
        }

        public double fullGCPercentage(){
            final int size = _last.size();
            
            if ( size < 6)
                return 0;

            final double end = _last.get( 0 ).when();
            double start = end;
            
            double time = 0;
            
            for ( int i=0; i<size; i++ ){
                GCLine l = _last.get( i );
                
                assert( l.when() <= end );
                if ( end - l.when() > 27000 )
                    break;

                start = l.when();
                
                if ( l.full() )
                    time += l.howLong();
            }

            return time / ( end - start );
        }

        final CircularList<GCLine> _last = new CircularList<GCLine>( 20 , true );
    }
}
