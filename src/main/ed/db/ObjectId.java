// ObjectId.java

package ed.db;

import ed.util.*;

/**
 * 12 bytes
 * ---
 *  0 time
 *  1
 *  2
 *  3
 *  4 machine
 *  5
 *  6 pid
 *  7
 *  8 inc
 *  9
 * 10
 * 11
 */
public class ObjectId {
    
    public static ObjectId get(){
        return new ObjectId();
    }
    
    ObjectId( long base , int inc ){
        _base = base;
        _inc = inc;
    }
    
    private ObjectId(){
        _base = ( ((long)_time) << 32) | _machine;

        synchronized ( _incLock ){
            _inc = _next++;
        }
    }

    public int hashCode(){
        return _inc;
    }

    public boolean equals( Object o ){
        
        if ( this == o )
            return true;

        if  ( ! ( o instanceof ObjectId ) )
            return false;
        
        ObjectId other = (ObjectId)o;
        return 
            _base == other._base && 
            _inc == other._inc;
    }

    public String toString(){
        return Long.toHexString( _base ) + Integer.toHexString( _inc );
    }

    final long _base;
    final int _inc;

    private static int _next = (new java.util.Random()).nextInt();
    private static final String _incLock = new String( "ObjectId._incLock" );

    private static int _time = (int)(System.currentTimeMillis()/1000);
    
    static final Thread _timeFixer;
    private static final long _machine;
    static {
        try {
            int startTime = (int)( java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime() & 0xFFFF );
            int machinePiece = ( java.net.InetAddress.getLocalHost().getHostName().hashCode() & 0xFFFF ) << 16;
            _machine = ( startTime | machinePiece ) & 0x7FFFFFFF;
        }
        catch ( java.io.IOException ioe ){
            throw new RuntimeException( ioe );
        }

        _timeFixer = new Thread("ObjectId-TimeFixer"){
                public void run(){
                    while ( true ){
                        ThreadUtil.sleep( 999 );
                        _time = (int)(System.currentTimeMillis()/1000);
                    }
                }
            };
        _timeFixer.setDaemon( true );
        _timeFixer.start();
    }

}
