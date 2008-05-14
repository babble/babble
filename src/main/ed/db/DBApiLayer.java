// DBApiLayer.java

package ed.db;

import java.io.*;
import java.nio.*;
import java.util.*;

import ed.js.*;

public abstract class DBApiLayer extends DBBase {

    static final boolean D = Boolean.getBoolean( "DEBUG.DB" );

    protected DBApiLayer( String root ){
        super( root );
        
        _root = root;
    }


    protected abstract void doInsert( ByteBuffer buf );
    protected abstract void doDelete( ByteBuffer buf );
    protected abstract void doUpdate( ByteBuffer buf );
    protected abstract void doKillCursors( ByteBuffer buf );
    
    protected abstract int doQuery( ByteBuffer out , ByteBuffer in );
    protected abstract int doGetMore( ByteBuffer out , ByteBuffer in );

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

    String _removeRoot( String ns ){
        if ( ! ns.startsWith( _root + "." ) )
            return ns;
        return ns.substring( _root.length() + 1 );
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
        
        return DBProvider.get( root ).getCollection( table );
    }
    
    public Collection<String> getCollectionNames(){
        List<String> tables = new ArrayList<String>();
        
        DBCollection namespaces = getCollection( "system.namespaces" );
        if ( namespaces == null )
            throw new RuntimeException( "this is impossible" );
	
	Iterator<JSObject> i = namespaces.find( new JSObjectBase() , null , 0 , 0 );
	if ( i == null )
	    return tables;

        for (  ; i.hasNext() ;  ){
            JSObject o = i.next();
            String n = o.get( "name" ).toString();
            int idx = n.indexOf( "." );
            
            String root = n.substring( 0 , idx );
            if ( ! root.equals( _root ) )
                continue;
            
	    if ( n.indexOf( "$" ) >= 0 )
		continue;

            String table = n.substring( idx + 1 );

            tables.add( table );
        }

        return tables;
    }
    
    public static Collection<String> getRootNamespacesLocal(){
	List<String> lst = new ArrayList<String>();
	
	File dir = new File( "/data/db/" );
	if ( ! dir.exists() )
	    return lst;
	
	for ( String s : dir.list() ){
	    if ( ! s.endsWith( ".ns" ) )
		continue;
	    if ( s.startsWith( "sys." ) )
		continue;
	    lst.add( s.substring( 0 , s.length() - 3 ) );
	}

	return lst;
    }

    public static Collection<String> getRootNamespaces( String ip ){
	if ( ip.equals( "127.0.0.1" ) )
	    return getRootNamespacesLocal();
	
	throw new RuntimeException( "getRootNamespaces isn't working remotely right now" );
    }

    class MyCollection extends DBCollection {
        MyCollection( String name ){
            super( DBApiLayer.this , name );
            _fullNameSpace = _root + "." + name;
        }

        public void doapply( JSObject o ){
            o.set( "_ns" , _removeRoot( _fullNameSpace ) );
        }

        public JSObject dofind( ObjectId id ){
            JSObject lookup = new JSObjectBase();
            lookup.set( "_id" , id );
            
            Iterator<JSObject> res = find( lookup );
            if ( res == null )
                return null;

            JSObject o = res.next();
            
            if ( res.hasNext() ){
		System.out.println( "multiple entries with same _id" );
                //throw new RuntimeException( "something is wrong" );
	    }
            
            if ( _constructor != null && o instanceof JSObjectBase )
                ((JSObjectBase)o).setConstructor( _constructor );

            return o;
        }

        public JSObject doSave( JSObject o ){
            return save( o , true );
        }
                
        public JSObject save( JSObject o , boolean shouldApply ){
            if ( shouldApply ){
                apply( o );
                ((ObjectId)o.get( "_id" ) )._new = false;
            }

            ByteEncoder encoder = ByteEncoder.get();
            
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );
            
            encoder.putObject( null , o );
            encoder.flip();
            
            doInsert( encoder._buf );
            
            encoder.done();
            
            return o;
        }
        
        public int remove( JSObject o ){
            ByteEncoder encoder = ByteEncoder.get();
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );            
            
            if ( o.keySet().size() == 1 && 
                 o.get( o.keySet().iterator().next() ) instanceof ObjectId )
                encoder._buf.putInt( 1 );
            else
                encoder._buf.putInt( 0 );
            
            encoder.putObject( null , o );
            encoder.flip();
            
            doDelete( encoder._buf );
            encoder.done();
            
            return -1;
        }
        
        void _cleanCursors(){
            if ( _deadCursorIds.size() == 0 )
                return;
            
            if ( _deadCursorIds.size() % 20 != 0 && _deadCursorIds.size() < 500 )
                return;
            
            List<Long> l = _deadCursorIds;
            _deadCursorIds = new Vector<Long>();
            
            System.out.println( "trying to kill cursors : " + l.size() );
            
            try {
                killCursors( l );
            }
            catch ( Throwable t ){
                t.printStackTrace();
                _deadCursorIds.addAll( l );
            }
        }
        
        void killCursors( List<Long> all ){
            if ( all == null || all.size() == 0 )
                return;
            
            ByteEncoder encoder = ByteEncoder.get();
            encoder._buf.putInt( 0 ); // reserved
            
            encoder._buf.putInt( all.size() );
            for ( int i=0; i<all.size(); i++ )
                encoder._buf.putLong( all.get( i  ) );
            
            doKillCursors( encoder._buf );

            encoder.done();
        }

        public Iterator<JSObject> find( JSObject ref , JSObject fields , int numToSkip , int numToReturn ){
            _cleanCursors();

            ByteEncoder encoder = ByteEncoder.get();
            
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );
            
            encoder._buf.putInt( numToSkip ); 
            encoder._buf.putInt( numToReturn );
            encoder.putObject( null , ref ); // ref
            if ( fields != null )
                encoder.putObject( null , fields ); // fields to return
            encoder.flip();

            ByteDecoder decoder = ByteDecoder.get( DBApiLayer.this , _fullNameSpace , _constructor );

            int len = doQuery( encoder._buf , decoder._buf );
            decoder.doneReading( len );
            
            SingleResult res = new SingleResult( _fullNameSpace , decoder , null );
            
            decoder.done();
            encoder.done();
            
            if ( res._lst.size() == 0 )
                return null;
            
            return new Result( this , res , numToReturn );
        }

        public JSObject update( JSObject query , JSObject o , boolean upsert , boolean apply ){
            if ( apply ){
                apply( o );
                ((ObjectId)o.get( "_id" ) )._new = false;
            }
            
            ByteEncoder encoder = ByteEncoder.get();
            encoder._buf.putInt( 0 ); // reserved
            encoder._put( _fullNameSpace );            
            
            encoder._buf.putInt( upsert ? 1 : 0 );
            
            encoder.putObject( null , query );
            encoder.putObject( null , o );
            
            encoder.flip();
            
            doUpdate( encoder._buf );
            
            encoder.done();
            
            return o;
        }

        public void ensureIndex( JSObject keys , String name ){
            JSObject o = new JSObjectBase();
            o.set( "name" , name );
            o.set( "ns" , _fullNameSpace );
            o.set( "key" , keys );
            
	    //dm-system isnow in our database 
	    DBApiLayer.this.getCollection( "system.indexes" ).save( o , false );
        }

        final String _fullNameSpace;
    }

    class SingleResult {

        SingleResult( String fullNameSpace , ByteDecoder decoder , Set<ObjectId> seen ){
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
                _lst = new ArrayList<JSObject>( _num );
            
            if ( _num > 0 ){    
                int num = 0;
                
                while( decoder.more() && num < _num ){
                    final JSObject o = decoder.readObject();
                    
                    if ( seen != null ){
                        ObjectId id = (ObjectId)o.get( "_id" );
                        if ( id != null ){
                            if ( seen.contains( id ) ) continue;
                            seen.add( id );
                        }
                    }

                    o.set( "_ns" , _removeRoot( _fullNameSpace ) );
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
            for ( JSObject o : res._lst ){
                ObjectId id = (ObjectId)o.get( "_id" );
                if ( id != null )
                    _seen.add( id );
            }
            _cur = res._lst.iterator();
        }

        public JSObject next(){
            if ( _cur.hasNext() )
                return _cur.next();
            
            if ( _curResult._cursor <= 0 )
		throw new RuntimeException( "no more" );
	    
	    _advance();
	    return next();
        }

        public boolean hasNext(){
            if ( _cur.hasNext() )
                return true;
	    
            if ( _curResult._cursor <= 0 )
		return false;
	    
	    _advance();
	    return hasNext();
        }

	private void _advance(){
	    if ( _curResult._cursor <= 0 )
		throw new RuntimeException( "can't advance a cursor <= 0" );
	    
	    ByteEncoder encoder = ByteEncoder.get();
            
	    encoder._buf.putInt( 0 ); // reserved
	    encoder._put( _curResult._fullNameSpace );
	    encoder._buf.putInt( _numToReturn ); // num to return
	    encoder._buf.putLong( _curResult._cursor );
	    encoder.flip();
            
	    ByteDecoder decoder = ByteDecoder.get( DBApiLayer.this , _collection._fullNameSpace , _collection._constructor );
	    int len = doGetMore( encoder._buf , decoder._buf );
	    decoder.doneReading( len );
            
	    SingleResult res = new SingleResult( _curResult._fullNameSpace , decoder , _seen );
	    init( res );
	    
	    decoder.done();
	    encoder.done();
	    
	}

        public void remove(){
            throw new RuntimeException( "can't remove this way" );
        }

        public String toString(){
            return "DBCursor";
        }
        
        protected void finalize(){
            if ( _curResult != null && _curResult._cursor > 0 )
                _deadCursorIds.add( _curResult._cursor );
        }

        SingleResult _curResult;
        Iterator<JSObject> _cur;
        final Set<ObjectId> _seen = new HashSet<ObjectId>();
        final MyCollection _collection;
        final int _numToReturn;
    }

    public String toString(){
        return _root;
    }

    final String _root;
    final Map<String,MyCollection> _collections = Collections.synchronizedMap( new HashMap<String,MyCollection>() );
    List<Long> _deadCursorIds = new Vector<Long>();

    static final List<JSObject> EMPTY = Collections.unmodifiableList( new LinkedList<JSObject>() );


}
