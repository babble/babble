// DBJni.java

package ed.db;

import java.nio.*;
import java.util.*;

import ed.js.*;

public class DBJni extends DBBase {

    static final boolean D = false;

    public DBJni( String root ){
        this( root , null );
    }
    
    public DBJni( String root , String ip ){
        if ( ip == null || ip.length() == 0 )
            ip = "127.0.0.1";
        _ip = ip;
        _root = root;
        _sock = getSockAddr( _ip );
    }
    
    public MyCollection getCollection( String name ){
        MyCollection c = _collections.get( name );
        if ( c != null )
            return c;

        synchronized ( _collections ){
            c = _collections.get( name );
            if ( c != null )
                return c;
            
            c = new MyCollection( name );
            _collections.put( name , c );
        }
        
        return c;
    }

    
    public Collection<String> getCollectionNames(){
        throw new RuntimeException( "not implemented yet" );
    }
    
    class MyCollection extends DBCollection {
        MyCollection( String name ){
            super( name );
            _fullNameSpace = _root + "." + name;
        }

        public ObjectId apply( JSObject o ){
            ObjectId id = (ObjectId)o.get( "_id" );
            
            if ( id == null ){
                id = ObjectId.get();
                o.set( "_id" , id );
            }
            
            return id;
        }

        public JSObject find( ObjectId id ){
            JSObject lookup = new JSObjectBase();
            lookup.set( "_id" , id );
            
            List<JSObject> res = find( lookup );
            if ( res == null || res.size() == 0 )
                return null;
            
            if ( res.size() > 1 )
                throw new RuntimeException( "something is wrong" );
            
            return res.get( 0 );
        }

        public JSObject save( JSObject o ){
            apply( o );

            ByteEncoder encoder = new ByteEncoder();
            
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );
            
            encoder.putObject( null , o );
            encoder.flip();
            
            insert( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() );
            
            return o;
        }
        
        public int remove( JSObject o ){
            ByteEncoder encoder = new ByteEncoder();
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );            
            
            if ( o.keySet().size() == 1 && 
                 o.get( o.keySet().iterator().next() ) instanceof ObjectId )
                encoder._buf.putInt( 1 );
            else
                encoder._buf.putInt( 0 );
            
            encoder.putObject( null , o );
            encoder.flip();
            
            doDelete( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() );

            return -1;
        }

        public List<JSObject> find( JSObject ref ){

            ByteEncoder encoder = new ByteEncoder();
            
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );
            
            encoder._buf.putInt( 0 ); // num to return
            encoder.putObject( null , ref );
            encoder.flip();
            
            ByteDecoder decoder = new ByteDecoder();
            
            int len = query( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() , decoder._buf );
            decoder.doneReading( len );
            
            Result res = new Result( decoder );
            
            if ( res._lst.size() == 0 )
                return null;
            
            return res._lst;
        }

        public JSObject update( JSObject query , JSObject o , boolean upsert ){
            apply( o );
            
            ByteEncoder encoder = new ByteEncoder();
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );            
            
            encoder._buf.putInt( upsert ? 1 : 0 );
            
            encoder.putObject( null , query );
            encoder.putObject( null , o );
            
            encoder.flip();
            
            doUpdate( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() );
            
            return o;
        }

        final String _fullNameSpace;
    }
    
    public String toString(){
        return "DBConnection " + _ip + ":" + _root;
    }

    final Map<String,MyCollection> _collections = Collections.synchronizedMap( new HashMap<String,MyCollection>() );
    final String _ip;
    final String _root;
    final long _sock;

    class Result {

        Result( ByteDecoder decoder ){
            
            _reserved = decoder.getInt();
            _cursor = decoder.getLong();
            _startingFrom = decoder.getInt();
            _num = decoder.getInt();
            
            if ( _num == 0 )
                _lst = EMPTY;
            else if ( _num < 3 )
                _lst = new LinkedList<JSObject>();
            else 
                _lst = new ArrayList<JSObject>();
            
            if ( _num > 0 ){    
                int num = 0;
                
                while( decoder.more() && num < _num ){
                    final JSObject o = decoder.readObject();
                    _lst.add( o );
                    num++;

                    if ( D ) {
                        System.out.println( "-- : " + o.keySet().size() );
                        for ( String s : o.keySet() )
                            System.out.println( "\t " + s + " : " + o.get( s ) );
                    }
                }
            }
        }

        public String toString(){
            return "reserved:" + _reserved + " _cursor:" + _cursor + " _startingFrom:" + _startingFrom + " _num:" + _num ;
        }
        

        final int _reserved;
        final long _cursor;
        final int _startingFrom;
        final int _num;
        
        final List<JSObject> _lst;
    }

    // library init

    static {
        String ext = "so";
        String os = System.getenv("OSTYPE" );
        if ( "darwin".equals( os ) )
            ext = "jnilib";
        System.load( ( new java.io.File( "build/libdb." + ext ) ).getAbsolutePath() );

        _defaultIp = createSock( "127.0.0.1" );
    }

    static long getSockAddr( String name ){
        Long addr = _ipToSockAddr.get( name );
        if ( addr != null )
            return addr;
        
        addr = createSock( name );
        _ipToSockAddr.put( name, addr );
        return addr;
    }

    private static native long createSock( String name );

    private static native String msg( long sock );
    private static native void insert( long sock , ByteBuffer buf , int position , int limit );
    private static native void doDelete( long sock , ByteBuffer buf , int position , int limit );
    private static native void doUpdate( long sock , ByteBuffer buf , int position , int limit );
    private static native int query( long sock , ByteBuffer buf , int position , int limit , ByteBuffer res );

    static final Map<String,Long> _ipToSockAddr = Collections.synchronizedMap( new HashMap<String,Long>() );
    static final List<JSObject> EMPTY = Collections.unmodifiableList( new LinkedList<JSObject>() );
    static final long _defaultIp;
    
    // ----- TESTING
    
    public static void main( String args[] ){
        
        MyCollection c = (new DBJni( "eliot" , "10.0.21.60" ) ).getCollection( "t1" );
        
        JSObject o = new JSObjectBase();
        o.set( "jumpy" , "yes" );
        o.set( "name"  , "ab" );
        c.save( o );

        o = new JSObjectBase();
        o.set( "jumpyasd" , "no" );
        o.set( "name"  , "ce" );
        c.save( o );
     
        JSObject q = new JSObjectBase();
        System.out.println( c.find( q ) );

        c.update( o , o , true );

        System.out.println( c.find( q ) );

        JSObjectBase d = new JSObjectBase();
        d.set( "name" , "ab" );
        c.remove( d );
        System.out.println( c.find( new JSObjectBase() ) );

        
    }
    
}
