// DBBase.java

package ed.db;

import java.util.*;

import ed.appserver.JSFileLibrary;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

public abstract class DBBase extends JSObjectLame {

    public DBBase( String name ){
    	_name = name;
	
        /*
         *  augment the db object 
         */
        
    	Scope s = Scope.newGlobal();

        JSFileLibrary lib = JSFileLibrary.loadLibraryFromEd("ed/db/", null, s);
        
        ((JSFunction)(lib.get( "db" ))).call( s, this );
        ((JSFunction)(lib.get( "dbcollection" ))).call( s, this);	
    }
    
    public abstract DBCollection getCollectionFromFull( String fullNameSpace );
    public abstract DBCollection getCollection( String name );
    public abstract Collection<String> getCollectionNames();

    public abstract String getConnectPoint();

    public DBCollection getCollectionFromString( String s ){
        DBCollection foo = null;
        
        while ( s.contains( "." ) ){
            int idx = s.indexOf( "." );
            String b = s.substring( 0 , idx );
            s = s.substring( idx + 1 );
            foo = getCollection( b );
        }

        if ( foo != null )
            return foo.getCollection( s );
        return getCollection( s );
    }

    public String getName(){
	return _name;
    }

    public Object set( Object n , Object v ){
        _entries.put( n , v );
        return v;
    }

    public Object get( Object n ){
        if ( n == null )
            return null;
        
	if ( MethodHolder.isMethodName( this.getClass() , n.toString() ) )
	    return null;

        if ( n.toString().equals( "tojson" ) )
            return _tojson;
        
        if ( n.toString().equals( "readOnly" ) )
            return _readOnly;

        Object v = _entries.get( n );
        if ( v != null )
            return v;

        if ( n instanceof String || 
             n instanceof JSString ){
            String s = n.toString();
            if ( s.startsWith( "." ) ){
                if ( s.indexOf( "." , 1 ) > 0 ){
                    final String other = s.substring(1);
                    if ( ! allowedToAccess( other ) )
                        throw new JSException( "not allowed to access db from [" + other + "]" );
                    return getCollectionFromFull( other );
                }
                return DBProvider.get( s.substring(1) );
            }
            return getCollection( s );
        }

        return null;
    }

    public Collection<String> keySet(){
        return getCollectionNames();
    }

    public void setReadOnly( Boolean b ){
        _readOnly = b;
    }

    class tojson extends JSFunctionCalls0{
        public Object call( Scope s , Object foo[] ){
            return DBBase.this.toString();
        }
    }

    public JSObject getCollectionPrototype(){
        return _collectionPrototype;
    }

    public String toString(){
        return _name;
    }

    public boolean allowedToAccess( String other ){
        if ( ed.security.Security.isCoreJS() )
            return true;

        // if you're running not in production, you can do whatever you want
        if ( Cloud.getInstanceIfReal() == null )
            return true;

        return false;
    }

    final tojson _tojson = new tojson();
    final String _name;
    protected boolean _readOnly = false;
    final JSObjectBase _collectionPrototype = new JSObjectBase();
    final Map _entries = new HashMap();
}
