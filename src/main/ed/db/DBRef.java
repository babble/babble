// DBRef.java

package ed.db;

import java.util.*;

import ed.*;
import ed.js.*;
import ed.appserver.*;

public class DBRef extends JSObjectBase {

    static final boolean D = Boolean.getBoolean( "DEBUG.DB" );

    DBRef( JSObject parent , String fieldName , DBBase db , String ns , ObjectId id ){
        _parent = parent;
        _fieldName = fieldName;
        
        _ns = ns;
        _id = id;
        _db = db;
        
        super.set( "_ns" , ns );
        super.set( "_id" , id );
        _inited = true;
        markClean();
    }
    
    public Object prefunc(){
        return doLoad();
    }

    public Object doLoad(){
        if ( ! _inited )
            return _actual;
        
        if ( _loaded )
            return _actual;

	if ( D ) System.out.println( "following dbref" );
        
        if ( _db == null )
            throw new RuntimeException( "db is null" );
        
        final RefCache rc = getRefCache();

        DBCollection coll = _db.getCollectionFromString( _ns );
        
        JSObject o = rc == null ? null : rc.get( _id );
        
        if ( o == null ){
            o = coll.find( _id );
            if ( o != null && rc != null )
                rc.put( _id , o );
        }
        
        if ( o == null ){
            System.out.println( "can't find ref.  ns:" + _ns + " id:" + _id );
            _parent.set( _fieldName , null );
            return null;
        }
        coll.apply( o );
        MyAsserts.assertEquals( _id , o.get( "_id" ) );
        MyAsserts.assertEquals( _ns.toString() , o.get( "_ns" ).toString() );

        _loaded = true; // this technically makes a race condition...
        
        Object ret = this;

        if ( ! o.getClass().equals( JSObjectBase.class ) ){
            _parent.set( _fieldName , o );
            ret = o;
        }
        
        if ( o instanceof JSObjectBase )
            setConstructor( ((JSObjectBase)o).getConstructor() );
        
        addAll( o );
        
        _doneLoading = true;
        markClean();

        _actual = ret;
        return ret;
    }
    
    public boolean isDirty(){
        return _doneLoading && super.isDirty();
    }
    
    final JSObject _parent;
    final String _fieldName;

    final ObjectId _id;
    final String _ns;
    final DBBase _db;
    
    boolean _inited = false;
    boolean _loaded = false;
    boolean _doneLoading = false;
    
    private Object _actual;

    private static RefCache getRefCache(){
        AppRequest r = AppRequest.getThreadLocal();
        if ( r == null )
            return null;
        
        RefCache c = _refCache.get( r );
        if ( c != null )
            return c;
        
        c = new RefCache();
        _refCache.put( r , c );
        return c;
    }
    
    private static class RefCache extends HashMap<ObjectId,JSObject>{};

    private static Map<AppRequest,RefCache> _refCache = Collections.synchronizedMap( new WeakHashMap<AppRequest,RefCache>() );
}
