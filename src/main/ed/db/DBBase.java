// DBBase.java

package ed.db;

import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;

public abstract class DBBase extends JSObjectLame {

    public DBBase( String name ){
	_name = name;
    }

    public abstract DBCollection getCollectionFromFull( String fullNameSpace );
    public abstract DBCollection getCollection( String name );
    public abstract Collection<String> getCollectionNames();

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

    public Object get( Object n ){
        if ( n == null )
            return null;
        
	if ( MethodHolder.isMethodName( this.getClass() , n.toString() ) )
	    return null;

        if ( n.toString().equals( "tojson" ) )
            return _tojson;
        
        if ( n.toString().equals( "readOnly" ) )
            return _readOnly;

        if ( n instanceof String || 
             n instanceof JSString ){
            String s = n.toString();
            if ( s.startsWith( "." ) )
                return getCollectionFromFull( s.substring(1) );
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
            return toString();
        }
    }
    tojson _tojson = new tojson();
    
    final String _name;
    protected boolean _readOnly = false;
}
