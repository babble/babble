// ObjectId.java

package ed.db;

import java.util.*;

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
 *  6 
 *  7 pid
 *  8 
 *  9 inc
 * 10
 * 11
 */
public class ObjectId {

    static final boolean D = false;
    
    public static ObjectId get(){
        return new ObjectId();
    }
    
    public ObjectId( String s ){
        String baseString = s.substring( 0 , 16 );
        String incString = s.substring( 16 );

        _base = Long.parseLong( baseString , 16 );
        _inc = Integer.parseInt( incString , 16 );

        _new = false;
    }
    
    ObjectId( long base , int inc ){
        _base = base;
        _inc = inc;
        
        _new = false;
    }
    
    private ObjectId(){
        _base = ( ((long)_time) << 32) | _machine;
        
        if ( D ) System.out.println( "base : " + Long.toHexString( _base ) );
        
        synchronized ( _incLock ){
            if ( _nextShort == Short.MAX_VALUE )
                _nextByte++;

            int myb = ( ((int)_nextByte) << 16 ) & 0xFF0000;
            int myi = ( _nextShort++ ) & 0xFFFF;
            
            _inc = myb | myi;
        }
        
        _new = true;
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
    
    boolean _new;
    
    private static byte _nextByte = (byte)(new java.util.Random()).nextInt();
    private static short _nextShort = (short)(new java.util.Random()).nextInt();
    private static final String _incLock = new String( "ObjectId._incLock" );

    private static int _time = (int)(System.currentTimeMillis()/1000);
    
    static final Thread _timeFixer;
    private static final long _machine;
    private static final int _bottomTop;
    static {
        try {
            int startTime = (int)( java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime() & 0xFFFF );
            _bottomTop = ( startTime & 0xFF ) << 24;
            if ( D ) System.out.println( "top of last piece : " + Integer.toHexString( _bottomTop ) );
            int machinePiece = ( java.net.InetAddress.getLocalHost().getHostName().hashCode() & 0xFFFFFF ) << 8;
            _machine = ( ( startTime >> 8 ) | machinePiece ) & 0x7FFFFFFF;
            if ( D ) System.out.println( "machine piece : " + Long.toHexString( _machine ) );
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

    public static void main( String args[] ){
        Set<ObjectId> s = new HashSet<ObjectId>();
        while ( true ){
            ObjectId i = get();
            if ( s.contains( i ) )
                throw new RuntimeException( "fuck" );
            s.add( i );

            ObjectId o = new ObjectId( i.toString() );
            if ( ! i.equals( o ) )
                throw new RuntimeException( o.toString() + " != " + i.toString() );
        }

    }

}
