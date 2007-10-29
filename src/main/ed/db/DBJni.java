// DBJni.java

package ed.db;

import java.nio.*;
import java.util.*;

import ed.js.*;

public class DBJni extends DBBase {
    
    
    public DBJni( String root ){
        _root = root;
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

        public JSObject save( JSObject o ){
            apply( o );
            insert( _fullNameSpace , o );
            return o;
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
        
        public List<JSObject> find( JSObject ref ){
            Result res = query( _fullNameSpace , ref );
            if ( res._lst.size() == 0 )
                return null;
            
            return res._lst;
        }

        final String _fullNameSpace;
    }
    
    final Map<String,MyCollection> _collections = Collections.synchronizedMap( new HashMap<String,MyCollection>() );
    final String _root;

    // ----------------------------------
    

    
    private static native String msg();

    // ----- INSERT    

    private static void insert( String collection , JSObject o ){
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        ByteEncoder encoder = new ByteEncoder();
        
        buf.putInt( 0 ); // reserved

        encoder._put( buf , collection );

        encoder.putObject( buf , null , o );
        buf.flip();
        
        insert( buf , buf.position() , buf.limit() );
    }

    private static native void insert( ByteBuffer buf , int position , int limit );

    // ----- QUERY

    static class Result {

        Result( ByteBuffer buf ){
            _reserved = buf.getInt();
            _cursor = buf.getLong();
            _startingFrom = buf.getInt();
            _num = buf.getInt();

            ByteDecoder decoder = new ByteDecoder();
            
            if ( _num == 0 )
                _lst = EMPTY;
            else if ( _num < 3 )
                _lst = new LinkedList<JSObject>();
            else 
                _lst = new ArrayList<JSObject>();
            
            if ( _num > 0 ){    
                int num = 0;
                
                while( buf.position() < buf.limit() && num < _num ){
                    JSObject o = decoder.readObject( buf );
                    num++;
                    
                    System.out.println( "-- : " + o.keySet().size() );
                    for ( String s : o.keySet() )
                        System.out.println( "\t " + s + " : " + o.get( s ) );
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
    

    private static Result query( String collection , JSObject o ){
        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        ByteEncoder encoder = new ByteEncoder();
        
        buf.putInt( 0 ); // reserved

        encoder._put( buf , collection );

        buf.putInt( 0 ); // num to return

        encoder.putObject( buf , null , o );
        buf.flip();
        
        ByteBuffer res = ByteBuffer.allocateDirect( 1024 * 1024 );
        res.order( ByteOrder.LITTLE_ENDIAN );
        
        int len = query( buf , buf.position() , buf.limit() , res );
        res.position( len );
        res.flip();
        
        return new Result( res );
    }

    private static native int query( ByteBuffer buf , int position , int limit , ByteBuffer res );

    // library init

    static {
        String ext = "so";
        if ( System.getenv( "OSTYPE" ).equals( "darwin" ) )
            ext = "jnilib";
        System.load( ( new java.io.File( "build/libdb." + ext ) ).getAbsolutePath() );
    }

    static final List<JSObject> EMPTY = Collections.unmodifiableList( new LinkedList<JSObject>() );
    
    // ----- TESTING
    
    public static void main( String args[] ){
        
        DBCollection c = (new DBJni( "eliot" ) ).getCollection( "t1" );
        
        JSObject o = new JSObjectBase();
        o.set( "jumpy" , "yes" );
        o.set( "name"  , "ab" );
        c.save( o );

        o = new JSObjectBase();
        o.set( "jumpyasd" , "no" );
        o.set( "name"  , "ce" );
        c.save( o );
     
        JSObject q = new JSObjectBase();
        q.set( "name" , "ab" );
        c.find( q );
    }
    
}
