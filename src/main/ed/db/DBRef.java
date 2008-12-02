// DBRef.java

package ed.db;

import java.util.*;

import ed.*;
import ed.js.*;
import ed.log.*;
import ed.appserver.*;

public class DBRef extends JSObjectBase {

    static final boolean D = Boolean.getBoolean( "DEBUG.DBREF" );

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

    public boolean isNull(){
	if ( _db == null )
	    return false;
	return _getPointedTo() == null;
    }

    public JSObject getRealObject(){
        final JSObject o = _getPointedTo();
        if ( JS.isBaseObject( o ) )
            return this;
        return o;

    }
    
    public Object prefunc(){
        return doLoad();
    }

    public Object doLoad(){
        if ( ! _inited )
            return _actual;
        
        if ( _loaded )
            return _actual;

	JSObject o = _getPointedTo();

        if ( o == null ){
            _parent.set( _fieldName , null );
            return null;
        }

        MyAsserts.assertEquals( _id , o.get( "_id" ) );
        MyAsserts.assertEquals( _ns.toString() , o.get( "_ns" ).toString() );
        
        _loaded = true; // this technically makes a race condition...
        
        Object ret = this;

        if ( ! JS.isBaseObject( o ) ){
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
    
    private JSObject _getPointedTo(){
	if ( _loadedPointedTo )
	    return _pointedTo;

        if ( _db == null )
            return this;

	if ( D ){ 
            System.out.println( "following dbref.  ns:" + _ns );
            Throwable t = new Throwable();
            t.fillInStackTrace();
            t.printStackTrace();
        }
        
        final RefCache rc = getRefCache();
        final DBCollection coll = _db.getCollectionFromString( _ns );
        
        JSObject o = rc == null ? null : rc.get( _id );
        
        if ( o == null ){
            o = coll.find( _id );
            if ( o != null && rc != null )
                rc.put( _id , o );
        }
	else {
	    coll.apply( o );
	}

        if ( o == null )
            Logger.getRoot().getChild( "missingref" ).info( "ns:" + _ns + " id:" + _id );


	_pointedTo = o;
	_loadedPointedTo = true;
	return _pointedTo;
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

    private boolean _loadedPointedTo = false;
    private JSObject _pointedTo;
    

    static void objectSaved( ObjectId id ){
        if ( id == null )
            return;
        
        RefCache rc = getRefCache();
        if ( rc == null )
            return;

        rc.remove( id );
    }
    
    private static RefCache getRefCache(){
        AppRequest r = AppRequest.getThreadLocal();
        if ( r == null )
            return null;
        
        RefCache c = (RefCache)r.getAttribute( "refCache" );
        if ( c != null )
            return c;
        
        c = new RefCache();
        r.setAttribute( "refcache" , c );
        return c;
    }
    
    private static class RefCache extends HashMap<ObjectId,JSObject>{};
}
