// DBRef.java

package ed.db;

import ed.js.*;

public class DBRef extends JSObjectBase {

    DBRef( ObjectId oid ){
        _oid = oid;
        super.set( "_oid" , oid );
        _inited = true;
    }
    
    public void prefunc(){
        if ( ! _inited )
            return;
        
        if ( _loaded )
            return;

        throw new RuntimeException( "need to load" );
    }

    final ObjectId _oid;
    boolean _inited = false;
    boolean _loaded = false;
}
