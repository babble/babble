// DBRef.java

package ed.db;

import ed.*;
import ed.js.*;

public class DBRef extends JSObjectBase {

    DBRef( DBBase db , String ns , ObjectId oid ){
        _ns = ns;
        _oid = oid;
        _db = db;
        
        super.set( "_ns" , ns );
        super.set( "_oid" , oid );
        _inited = true;
    }
    
    public void prefunc(){
        if ( ! _inited )
            return;
        
        if ( _loaded )
            return;
        
        if ( _db == null )
            throw new RuntimeException( "db is null" );
        
        DBCollection coll = _db.getCollectionFromFull( _ns );
        JSObject o = coll.find( _oid );
        if ( o == null )
            throw new RuntimeException( "null reference, what should we do?" );
        
        MyAsserts.assertEquals( _oid , o.get( "_oid" ) );
        MyAsserts.assertEquals( _ns , o.get( "_ns" ) );
        
        addAll( o );
        //throw new RuntimeException( "need to load" );
    }

    final ObjectId _oid;
    final String _ns;
    final DBBase _db;
    
    boolean _inited = false;
    boolean _loaded = false;
}
