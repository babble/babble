// DBRef.java

package ed.db;

import ed.*;
import ed.js.*;

public class DBRef extends JSObjectBase {

    DBRef( JSObject parent , String fieldName , DBBase db , String ns , ObjectId id ){
        _parent = parent;
        _fieldName = fieldName;
        
        _ns = ns;
        _id = id;
        _db = db;
        
        super.set( "_ns" , ns );
        super.set( "_id" , id );
        _inited = true;
    }
    
    public void prefunc(){
        if ( ! _inited )
            return;
        
        if ( _loaded )
            return;
        
        if ( _db == null )
            throw new RuntimeException( "db is null" );
        
        DBCollection coll = _db.getCollectionFromString( _ns );
        JSObject o = coll.find( _id );
        if ( o == null ){
            System.out.println( "can't find ref.  ns:" + _ns + " id:" + _id );
            _parent.set( _fieldName , null );
            return;
        }
        coll.apply( o );
        MyAsserts.assertEquals( _id , o.get( "_id" ) );
        MyAsserts.assertEquals( _ns.toString() , o.get( "_ns" ).toString() );

        _loaded = true; // this technically makes a race condition...

        if ( ! o.getClass().equals( JSObjectBase.class ) ){
            _parent.set( _fieldName , o );
        }
        
        addAll( o );
        //throw new RuntimeException( "need to load" );
    }

    final JSObject _parent;
    final String _fieldName;

    final ObjectId _id;
    final String _ns;
    final DBBase _db;
    
    boolean _inited = false;
    boolean _loaded = false;
}
