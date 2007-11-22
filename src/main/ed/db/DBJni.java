// DBJni.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;

public class DBJni extends DBBase {
    
    static final boolean D = false;

    public static DBJni get( String root ){
        return get( root , null );
    }
    
    public static DBJni get( String root , String ip ){
        DBJni db = _roots.get( root );
        if ( db != null )
            return db;
        return new DBJni( root , ip );
    }
    
    private DBJni( String root ){
        this( root , null );
    }
    
    private DBJni( String root , String ip ){
        
        if ( _roots.get( root ) != null )
            throw new RuntimeException( "already have an instance for : " + root );

        if ( ip == null || ip.length() == 0 ){
            ip = System.getenv( "db_ip" );
            if ( ip == null )
                ip = "127.0.0.1";
        }

        
        try {
            ip = InetAddress.getByName( ip ).getHostAddress();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "can't get ip for:" + ip );
        }
        
        _dbbase = this;
        _ip = ip;
        _root = root;
        _sock = getSockAddr( _ip );
        
        _roots.put( _root , this );

        if ( _roots.get( "system" ) == null )
            new DBJni( "system" , ip );
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

    public MyCollection getCollectionFromFull( String fullNameSpace ){
        // TOOD security
        
        if ( fullNameSpace.indexOf( "." ) < 0 ) {
            // assuming local
            return getCollection( fullNameSpace );
        }

        final int idx = fullNameSpace.indexOf( "." );        

        final String root = fullNameSpace.substring( 0 , idx );
        final String table = fullNameSpace.substring( idx + 1 );
        
        if ( _root.equals( root ) )
            return getCollection( table );
        
        DBJni base = _roots.get( root );
        if ( base != null )
            return base.getCollection( table );
        
        throw new RuntimeException( "don't know how to create things yet" );
    }
    
    public Collection<String> getCollectionNames(){
        throw new RuntimeException( "not implemented yet" );
    }
    
    class MyCollection extends DBCollection {
        MyCollection( String name ){
            super( DBJni.this , name );
            _fullNameSpace = _root + "." + name;
        }

        public ObjectId doapply( JSObject o ){
            ObjectId id = (ObjectId)o.get( "_id" );
            
            if ( id == null ){
                id = ObjectId.get();
                o.set( "_id" , id );
            }
            
            o.set( "_ns" , _fullNameSpace );

            return id;
        }

        public JSObject find( ObjectId id ){
            JSObject lookup = new JSObjectBase();
            lookup.set( "_id" , id );
            
            Iterator<JSObject> res = find( lookup );
            if ( res == null )
                return null;

            JSObject o = res.next();
            
            if ( res.hasNext() )
                throw new RuntimeException( "something is wrong" );
            
            return o;
        }

        public JSObject save( JSObject o ){
            return save( o , true );
        }
                
        public JSObject save( JSObject o , boolean shouldApply ){
            if ( shouldApply )
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

        public Iterator<JSObject> find( JSObject ref , JSObject fields , int numToReturn ){

            ByteEncoder encoder = new ByteEncoder();
            
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );
            
            encoder._buf.putInt( numToReturn ); // num to return
            encoder.putObject( null , ref ); // ref
            if ( fields != null )
                encoder.putObject( null , fields ); // fields to return
            encoder.flip();
            
            ByteDecoder decoder = ByteDecoder.get( _dbbase , _fullNameSpace );
            
            int len = query( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() , decoder._buf );
            decoder.doneReading( len );
            
            SingleResult res = new SingleResult( _fullNameSpace , decoder );
            
            if ( res._lst.size() == 0 )
                return null;
            
            return new Result( this , res , numToReturn );
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

        public void ensureIndex( JSObject keys , String name ){
            JSObject o = new JSObjectBase();
            o.set( "name" , name );
            o.set( "ns" , _fullNameSpace );
            o.set( "key" , keys );
            
            getCollectionFromFull( "system.indexes" ).save( o , false );
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
    final DBBase _dbbase;
    
    class SingleResult {

        SingleResult( String fullNameSpace , ByteDecoder decoder ){
            _fullNameSpace = fullNameSpace;
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
                    o.set( "_ns" , _fullNameSpace );
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
        
        final String _fullNameSpace;
        final int _reserved;
        final long _cursor;
        final int _startingFrom;
        final int _num;
        
        final List<JSObject> _lst;
    }

    class Result implements Iterator<JSObject> {
        
        Result( MyCollection coll , SingleResult res , int numToReturn ){
            init( res );
            _collection = coll;
            _numToReturn = numToReturn;
        }

        private void init( SingleResult res ){
            _curResult = res;
            _cur = res._lst.iterator();
            _all.add( res );
        }

        public JSObject next(){
            if ( _cur.hasNext() )
                return _cur.next();
            
            if ( _curResult._cursor > 0 ){
                System.out.println( "following cursor: " + _curResult._cursor );
                ByteEncoder encoder = new ByteEncoder();
                
                encoder._buf.putInt( 0 ); // reserved
                encoder._put( _curResult._fullNameSpace );
                encoder._buf.putInt( _numToReturn ); // num to return
                encoder._buf.putLong( _curResult._cursor );
                encoder.flip();
            
                ByteDecoder decoder = ByteDecoder.get( _dbbase , _collection._fullNameSpace );
                int len = getMore( _sock , encoder._buf , encoder._buf.position() , encoder._buf.limit() , decoder._buf );
                decoder.doneReading( len );
                
                SingleResult res = new SingleResult( _curResult._fullNameSpace , decoder );
                init( res );
                
                return next();
            }
            
            throw new RuntimeException( "no more" );
        }

        public boolean hasNext(){
            if ( _cur.hasNext() )
                return true;
            
            if ( _curResult._cursor > 0 )
                return true;
            
            return false;
        }

        public void remove(){
            throw new RuntimeException( "can't remove this way" );
        }

        public String toString(){
            return "DBCursor";
        }

        SingleResult _curResult;
        Iterator<JSObject> _cur;
        final MyCollection _collection;
        final List<SingleResult> _all = new LinkedList<SingleResult>();
        final int _numToReturn;
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
    private static native int getMore( long sock , ByteBuffer buf , int position , int limit , ByteBuffer res );
    
    static final Map<String,DBJni> _roots = new HashMap<String,DBJni>();
    static final Map<String,Long> _ipToSockAddr = Collections.synchronizedMap( new HashMap<String,Long>() );
    static final List<JSObject> EMPTY = Collections.unmodifiableList( new LinkedList<JSObject>() );
    static final long _defaultIp;
    
    // ----- TESTING
    
    public static void main( String args[] ){
        
        MyCollection c = (new DBJni( "eliot" ) ).getCollection( "t1" );
        
        JSObject o = new JSObjectBase();
        o.set( "jumpy" , "yes" );
        o.set( "name"  , "ab" );
        c.save( o );

        for ( int i=0; i<100; i++ ){
            System.out.println( i );
            o = new JSObjectBase();
            o.set( "jumpyasd" , "no" );
            o.set( "name"  , "asd" + Math.random() );
            c.save( o );
        }
        
        System.out.println( "done saving" );
     
        JSObject q = new JSObjectBase();
        System.out.println( "2 things : " + c.find( q ) );

        c.update( o , o , true );

        System.out.println( c.find( q ) );

        JSObjectBase d = new JSObjectBase();
        d.set( "name" , "ab" );
        c.remove( d );
        System.out.println( c.find( new JSObjectBase() ) );

        
    }
    
}
